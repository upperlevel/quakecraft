package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.game.gains.GainType;
import xyz.upperlevel.spigot.quakecraft.game.play.KillStreak;
import xyz.upperlevel.spigot.quakecraft.game.play.PlayingPhase;

@Data
public class Participant {
    private static GainType killGain;
    private static GainType headshotGain;//TODO: use
    private final GamePhase phase;
    private final Player player;
    public int kills;
    public int deaths;

    public int killsSinceDeath;
    private KillStreak nextKillStreak = KillStreak.get(0);

    private float gunCooldownBase = 1.0f;

    public float coins = 0f;

    public String getName() {
        return player.getName();
    }

    public void onKill(boolean headshot) {
        kills++;
        if(headshot)
            headshotGain.grant(this);
        killGain.grant(this);
        if(++killsSinceDeath >= nextKillStreak.getKills()) {
            nextKillStreak = nextKillStreak.reach(phase, this);
        }
    }

    public void onDeath() {
        killsSinceDeath = 0;
        nextKillStreak = KillStreak.get(0);
    }

    public static void loadGains() {
        killGain = GainType.create("kill");
        headshotGain = GainType.create("headshot");
    }
}
