package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.game.play.KillStreak;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;

@Data
public class Participant {
    private final GamePhase phase;
    private final Player player;
    public int kills;
    public int deaths;

    public int killsSinceDeath;
    private KillStreak nextKillStreak;

    public String getName() {
        return player.getName();
    }

    public void onKill() {
        kills++;
        if(++killsSinceDeath > nextKillStreak.getKills()) {
            nextKillStreak = nextKillStreak.reach(phase, this);
        }
    }

    public void onDeath() {
        killsSinceDeath = 0;
    }
}
