package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.game.gains.GainType;
import xyz.upperlevel.quakecraft.game.playing.KillStreak;

import java.util.List;
import java.util.Random;

public class Gamer {
    private static GainType killGain;
    private static GainType headshotGain;//TODO: use

    @Getter
    private final GamePhase gamePhase;

    @Getter
    private final Player player;

    @Getter
    @Setter
    private int kills;

    @Getter
    @Setter
    private int deaths;

    public int killsSinceDeath;
    private KillStreak nextKillStreak = KillStreak.get(0);

    @Getter
    private float gunCooldownBase = 1.0f;

    public float coins = 0f;

    public Gamer(GamePhase gamePhase, Player player) {
        this.gamePhase = gamePhase;
        this.player = player;
    }

    public String getName() {
        return player.getName();
    }

    /**
     * When a player does a kill its counter increments.
     * If the kill was an headshot receives an extra gain.
     */
    public void onKill(boolean headshot) {
        kills++;
        if (headshot) {
            headshotGain.grant(this);
        }
        killGain.grant(this);
        if (++killsSinceDeath >= nextKillStreak.getKills()) {
            nextKillStreak = nextKillStreak.reach(gamePhase, this);
        }
        gamePhase.updateRanking(); // that will update boards
    }

    /**
     * When the player die the kill-streak is reset.
     */
    public void onDeath() {
        killsSinceDeath = 0;
        nextKillStreak = KillStreak.get(0);
    }

    public void respawn() {
        List<Location> s = gamePhase.getArena().getSpawns();
        player.teleport(s.get(new Random().nextInt(s.size())));
    }

    public static void loadGains() {
        killGain = GainType.create("kill");
        headshotGain = GainType.create("headshot");
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
