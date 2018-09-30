package xyz.upperlevel.quakecraft.game.playing;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.KillStreakReachEvent;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.quakecraft.game.gains.GainType;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class KillStreak {
    private static List<KillStreak> streaks = new ArrayList<>();

    @Getter
    private int index;
    @Getter
    private final String id;
    @Getter
    private final int kills;
    @Getter
    private final Message message;
    @Getter
    private final GainType gain;

    public KillStreak(String id, int kills, Message message, GainType gain) {
        this.id = id;
        this.kills = kills;
        this.message = message;
        this.gain = gain;
    }

    public KillStreak(String id, Config config) {
        this.id = id;
        this.kills = config.getIntRequired("kills");
        this.message = config.getMessageRequired("message");
        this.gain = GainType.create();
        gain.setAmount(config.getFloatRequired("gain"));
    }

    public KillStreak reach(GamePhase phase, Participant p) {
        KillStreakReachEvent event = new KillStreakReachEvent(phase, p, this, message);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return this;
        else {
            event.getMessage().broadcast(phase.getGame().getPlayers(), "killer_name", p.getName());
            gain.grant(p);
            return get(index + 1);
        }
    }

    public static KillStreak get(int index) {
        return index < streaks.size() ? streaks.get(index) : null;
    }

    public static void loadConfig(Map<String, Config> config) {
        streaks.clear();
        for(Map.Entry<String, Config> kill : config.entrySet()) {
            try {
                streaks.add(new KillStreak(kill.getKey(), kill.getValue()));
            } catch (InvalidConfigException e) {
                e.addLocation("in killstreak " + kill.getKey());
                throw e;
            }
        }
        streaks.sort(Comparator.comparingInt(x -> x.kills));
        int index = 0;
        for(KillStreak s : streaks) {
            s.index = index++;
        }
    }

    public static void loadConfig() {
        loadConfig(Config.fromYaml(new File(
                Quakecraft.get().getDataFolder(),
                "game/playing/killstreak.yml"
        )).asConfigMap());
    }
}
