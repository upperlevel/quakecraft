package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.events.ParticipantGainMoneyEvent;
import xyz.upperlevel.quakecraft.phases.EndingPhase;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.ArrayList;
import java.util.List;

public class GainType {
    private static Message message;
    private static List<GainType> gains = new ArrayList<>();
    private static boolean initialized = false;

    @Getter
    private final String id;

    @Getter
    @Setter
    private PlaceholderValue<String> name;

    @Getter
    @Setter
    private float amount;

    public GainType(String id, PlaceholderValue<String> name, float amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    protected GainType(String id, Config config) {
        this.id = id;
        load(config);
    }

    protected GainType(String id) {
        this.id = id;
    }

    protected GainType() {
        this.id = null;
    }

    public void load(Config config) {
        name = config.getMessageStr("name");
        amount = config.getIntRequired("amount");
    }

    public void grant(Gamer player) {
        ParticipantGainMoneyEvent event = new ParticipantGainMoneyEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            player.coins += amount;
            if (name != null)
                message.send(player.getPlayer(), "gain_amount", EconomyManager.format(event.getGain()), "gain_name", name.resolve(player.getPlayer()));
        }
    }

    public static void register(GainType type) {
        if (initialized) {
            throw new IllegalStateException("Cannot register a GainType after the config loafing!");
        }
        gains.add(type);
    }

    public static GainType create(String id) {
        if (id == null) return new GainType();
        GainType type = new GainType(id);
        register(type);
        return type;
    }

    public static GainType create() {
        return new GainType();
    }

    public static void loadChildren() {
        Gamer.loadGains();
        EndingPhase.loadGains();
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("messages.game");
        message = config.getMessageRequired("gain-message");
        loadChildren();
        for (GainType type : gains) {
            if (type.id == null)
                continue;
            try {
                type.load(Quake.getConfigSection("game.gain." + type.id));
            } catch (InvalidConfigException e) {
                e.addLocation("in gain " + type.id);
                throw e;
            }
        }
        initialized = true;
    }
}
