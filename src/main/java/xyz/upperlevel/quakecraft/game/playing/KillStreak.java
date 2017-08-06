package xyz.upperlevel.spigot.quakecraft.game.play;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.events.KillStreakReachEvent;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.game.gains.GainType;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.message.Message;

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
            event.getMessage().broadcast("killer_name", p.getName());
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
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("in killstreak " + kill.getKey());
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
        loadConfig(ConfigUtils.loadConfigMap(
                QuakeCraftReloaded.get(),
                "killstreak.yml",
                "killstreak"
        ));
    }
}
