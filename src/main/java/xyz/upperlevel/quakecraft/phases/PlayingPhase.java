package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.game.Dash;
import xyz.upperlevel.quakecraft.game.Shot;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.task.Countdown;
import xyz.upperlevel.uppercore.task.UpdaterTask;
import xyz.upperlevel.uppercore.util.Dbg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.upperlevel.uppercore.Uppercore.hotbars;

public class PlayingPhase extends Phase {
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

    private final Map<Player, Shot> shootings = new HashMap<>(); // the players that have shot

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
        super("playing");

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
        super.onEnable(previous);

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
        super.onDisable(next);

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

        shootings.values().forEach(Shot::cancel);
        shootings.clear();
    }

    public void onPlayerQuit(Player player) {
        clearPlayer(player); // removes hotbar
        List<Gamer> gamers = gamePhase.getGamers();
        switch (gamers.size()) {
            case 1:
                // if one gamer is left, he won
                gamePhase.setPhase(new EndingPhase(gamePhase, gamers.get(0).getPlayer()));
                break;
            case 0:
                // already handled by GamePhase
                break;
        }
    }

    private void onKill(Player shooter, Player hit, boolean headshot) {
        Gamer s = gamePhase.getGamer(shooter);
        s.onKill(headshot);

        gamePhase.getGamer(hit).die();

        if (s.getKills() >= killsToWin) {
            gamePhase.setPhase(new EndingPhase(gamePhase, shooter));
        }
    }

    public boolean goOnIfHasWon(Player player) {
        if (gamePhase.getGamer(player).getKills() >= killsToWin) {
            Dbg.p(String.format("Game ended, %s has > %d kills", player.getName(), killsToWin));
            gamePhase.setPhase(new EndingPhase(gamePhase, player));
            return true;
        }
        return false;
    }

    private void onShoot(Player player) {
        if (!shootings.containsKey(player)) {
            Dbg.p(String.format("%s is shooting", player.getName()));

            Shot shot = new Shot(this, player);
            shootings.put(player, shot);
            shot.start();

            player.setExp(1.0f);
            long from = (long) Quake.getAccount(player).getSelectedTrigger().getFiringSpeed();
            new Countdown(Quake.get(), from, 1,
                    tick -> player.setExp((float) (tick / from)),
                    () -> {
                        player.setExp(0.0f); // to be sure
                        shootings.remove(player);
                        Dbg.p(String.format("%s can re-shoot", player.getName()));
                    }
            ).start();
        } else {
            Dbg.p(String.format("%s can't shoot, he's still recharging!", player.getName()));
        }
    }

    private void onDash(Player player) {
        Dbg.p(String.format("%s is dashing", player));
        Dash.swish(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (arena.equals(Quake.getArena(p))) {
            if (p.getInventory().getHeldItemSlot() == gunSlot) { // if the player is holding the gun item (checked by slot)
                // right click: shoot
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    onShoot(p);
                }
                // left click: dash
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    onDash(p);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (arena.equals(Quake.getArena(e.getPlayer())) && sneakDisabled) {
            e.setCancelled(true);
            sneakDisabledMessage.send(e.getPlayer());
        }
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("game");
        sneakDisabled = config.getBoolRequired("sneak-disabled");
        sneakDisabledMessage = config.getMessageRequired("sneak-disabled-message");
        killsToWin = config.getIntRequired("kills-to-win");
        hotbar = config.getRequired("playing-hotbar", PlayingHotbar.class);
    }
}
