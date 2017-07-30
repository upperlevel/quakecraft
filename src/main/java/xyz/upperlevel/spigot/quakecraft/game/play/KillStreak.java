package xyz.upperlevel.spigot.quakecraft.game.play;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.events.KillStreakReachEvent;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class KillStreak {
    private static List<KillStreak> streaks = new ArrayList<>();

    @Getter
    private final int index;
    @Getter
    private final int kills;
    @Getter
    private final Message message;

    public KillStreak reach(GamePhase phase, Participant p) {
        KillStreakReachEvent event = new KillStreakReachEvent(phase, p, this, message);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return this;
        else
            return get(index + 1);
    }

    public static KillStreak get(int index) {
        return index < streaks.size() ? streaks.get(index) : null;
    }

    public static void loadConfig() {
        streaks.clear();
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("game");
        Map<String, Object> kills = manager.getConfig().getSectionRequired("killstreak");
        int index = 0;
        for(Map.Entry<String, Object> kill : kills.entrySet()) {
            int killNumber;
            try {
                killNumber = Integer.parseInt(kill.getKey());
            } catch (NumberFormatException exception) {
                throw new InvalidConfigurationException("Cannot parse '" + kill.getKey() + "' as number", "in killstreak messages");
            }
            streaks.add(new KillStreak(index++, killNumber, Message.fromConfig(kill.getValue())));
        }
    }
}
