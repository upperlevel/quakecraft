package xyz.upperlevel.quakecraft.phases.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.arena.QuakeArena;

import java.util.List;
import java.util.Random;

public class Gamer {
    private static GainType killGain;
    private static GainType headshotGain;//TODO: use

    @Getter
    private final GamePhase gamePhase;

    @Getter
    private final QuakeArena arena;

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
    @Setter
    private float gunCooldownBase = 1.0f;

    public float coins = 0f;

    public Gamer(GamePhase gamePhase, Player player) {
        this.gamePhase = gamePhase;
        this.arena = gamePhase.getArena();
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
        gamePhase.updateRanking();
        gamePhase.updateBoards();
    }

    public void die() {
        killsSinceDeath = 0;
        nextKillStreak = KillStreak.get(0);

        List<Location> s = gamePhase.getArena().getSpawns();
        player.teleport(s.get(new Random().nextInt(s.size())));
    }

    public static void loadGains() {
        killGain = GainType.create("kill-gain");
        headshotGain = GainType.create("headshot-gain");
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
