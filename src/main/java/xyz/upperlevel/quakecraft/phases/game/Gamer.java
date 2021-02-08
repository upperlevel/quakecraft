package xyz.upperlevel.quakecraft.phases.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.util.Dbg;

import java.util.List;
import java.util.Random;

import static xyz.upperlevel.quakecraft.Quake.getProfileController;

public class Gamer {
    private static GainType killGain;
    private static GainType headshotGain;//TODO: use

    public static long RESPAWN_COOLDOWN; // In ticks

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

    @Getter
    @Setter
    private float dashBoostBase = 1.0f;

    @Getter
    private final GainNotifier gainNotifier = new GainNotifier(this);

    public float coins = 0f;

    @Getter
    private long lastDeathAt = 0L;

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

        Profile profile = getProfileController().getProfile(player);
        getProfileController().updateProfile(player.getUniqueId(), new Profile().setKills(profile.getKills() + 1));

        if (headshot)
            headshotGain.grant(this);

        killGain.grant(this);
        if (++killsSinceDeath >= nextKillStreak.getKills()) {
            nextKillStreak = nextKillStreak.reach(gamePhase, this);
        }
        gamePhase.updateRanking();
        gamePhase.updateBoards();
    }

    public boolean canDie() {
        return this.lastDeathAt + RESPAWN_COOLDOWN * (1000 / 20) < System.currentTimeMillis();
    }

    public boolean die() {
        // The FALL damage is completely disabled within the arena.

        if (!canDie()) { // You're too young to die!
            Dbg.pf("%s wanna die but he's too young so can't.", getName());
            return false;
        }
        this.lastDeathAt = System.currentTimeMillis();

        List<Location> s = gamePhase.getArena().getSpawns();
        boolean respawned = player.teleport(s.get(new Random().nextInt(s.size())));

        if (respawned) {
            deaths++;

            Profile profile = getProfileController().getProfile(player);
            getProfileController().updateProfile(player.getUniqueId(), new Profile().setDeaths(profile.getDeaths() + 1));

            killsSinceDeath = 0;
            nextKillStreak = KillStreak.get(0);
        } else {
            Uppercore.logger().severe(String.format("%s teleport after death blocked?", player.getName()));
            return false;
        }

        return true;
    }

    public static void loadGains() {
        killGain = GainType.create("kill-gain");
        headshotGain = GainType.create("headshot-gain");
    }

    public static void loadConfig() {
        Config cfg = Quake.getConfigSection("game");
        RESPAWN_COOLDOWN = cfg.getLong("respawn-cooldown");
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
