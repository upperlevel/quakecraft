package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.events.LaserHitEvent;
import xyz.upperlevel.quakecraft.game.playing.Dash;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.task.UpdaterTask;

import java.util.List;

import static xyz.upperlevel.uppercore.Uppercore.hotbars;

public class PlayingPhase implements Phase, Listener {
    private static String defaultKillMessage;
    private static Message shotMessage;
    private static Message headshotMessage;
    private static boolean sneakDisabled;
    private static Message sneakDisabledMessage;
    private static int killsToWin;
    private static PlayingHotbar hotbar;

    private static int gunSlot;

    @Getter
    private final QuakeArena arena;

    @Getter
    private final GamePhase gamePhase;

    private final UpdaterTask compassUpdater;

    private Player getNearbyPlayer(Player player) {
        Player res = null;
        double dist = 0;
        for (Player other : arena.getPlayers()) {
            if (!player.equals(other)) {
                double curr = player.getLocation().distanceSquared(other.getLocation());
                if (res == null || dist > curr) {
                    res = other;
                    dist = curr;
                }
            }
        }
        return res;
    }

    public PlayingPhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
        this.arena = gamePhase.getArena();
        this.compassUpdater = new UpdaterTask(20 * 5, () -> {
            for (Player player : arena.getPlayers()) {
                Player target = getNearbyPlayer(player);
                if (target != null)
                    player.setCompassTarget(target.getLocation());
            }
        });
    }

    private void setupPlayer(Player player) {
        hotbars().view(player).addHotbar(hotbar);

        // set account's selected armor to the player
        QuakeAccount account = Quake.getAccount(player);
        player.getInventory().setArmorContents(new ItemStack[]{
                account.getSelectedBoot().getItem().resolve(player),
                account.getSelectedLegging().getItem().resolve(player),
                account.getSelectedChestplate().getItem().resolve(player),
                account.getSelectedHat().getItem().resolve(player)
        });
    }

    public void clearPlayer(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, Quake.get());

        // spawns powerups
        for (Powerup box : arena.getPowerups()) {
            box.onGameBegin(gamePhase);
        }

        // setup the gamers
        List<Location> spawns = arena.getSpawns();
        List<Gamer> gamers = gamePhase.getGamers();
        for (int i = 0; i < gamers.size(); i++) {
            Player p = gamers.get(i).getPlayer();
            setupPlayer(p);
            p.teleport(spawns.get(i % spawns.size()));
        }

        // shouldn't be any spectator here

        gamePhase.getCountdown().start();
        compassUpdater.start();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);

        gamePhase.getCountdown().stop();
        compassUpdater.stop();

        // despawns powerups
        for (Powerup powerup : arena.getPowerups()) {
            powerup.onGameEnd();
        }

        // clears powerup effects from gamers
        arena.getPowerups()
                .stream()
                .map(Powerup::getEffect)
                .distinct()
                .forEach(e -> e.clear(gamePhase.getGamers()));
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            // spectators handled by GamePhase
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer()); // removes hotbar
            List<Gamer> gamers = gamePhase.getGamers();
            switch (gamers.size()) {
                case 1:
                    // if one gamer is left, he won
                    gamePhase.setPhase(new EndingPhase(gamePhase, gamers.get(0)));
                    break;
                case 0:
                    // already handled by GamePhase
                    break;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (arena.equals(Quake.getArena(p))) {
            if (p.getInventory().getHeldItemSlot() == gunSlot) { // if the player is holding the gun item (checked by slot)
                // right click: shoot
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Laser.shoot(this, p);
                }
                // left click: dash
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Dash.swish(p);
                }
            }
        }
    }

    @EventHandler
    public void onLaserHit(LaserHitEvent e) {
        Gamer shooter = e.getShooter();
        Gamer hit = e.getHit();

        Railgun gun = Quake.getAccount(shooter).getGun();

        Message message = e.isHeadshot() ? headshotMessage : shotMessage;
        message = message.filter(
                "killer", shooter.getName(),
                "killed", hit.getName(),
                "kill_message", (gun == null || gun.getKillMessage() == null) ? defaultKillMessage : gun.getKillMessage()
        );
        arena.broadcast(message);

        // update game statistics
        hit.onDeath();
        hit.respawn();

        shooter.onKill(e.isHeadshot());

        // if the shooter has won
        if (shooter.getKills() >= killsToWin) {
            gamePhase.setPhase(new EndingPhase(gamePhase, shooter));
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (arena.equals(Quake.getArena(e.getPlayer())) && sneakDisabled) {
            e.setCancelled(true);
            sneakDisabledMessage.send(e.getPlayer());
        }
    }

    public static void loadConfig(Config config) {
        defaultKillMessage = config.getMessageRequired("default-kill-message");
        shotMessage = config.getMessageRequired("shot");
        headshotMessage = config.getMessageRequired("headshot");

        sneakDisabled = config.getBoolRequired("sneak-disabled");
        sneakDisabledMessage = config.getMessageRequired("sneak-disabled-message");

        hotbar = config.get("playing-hotbar", PlayingHotbar.class, null);
        // todo gunSlot =

        sampleBoard = GameBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "playing_board.yml"
        )));
    }
}
