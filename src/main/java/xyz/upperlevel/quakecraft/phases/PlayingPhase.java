package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
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
import xyz.upperlevel.quakecraft.game.Dash;
import xyz.upperlevel.quakecraft.game.MultiStab;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.task.Countdown;
import xyz.upperlevel.uppercore.task.UpdaterTask;
import xyz.upperlevel.uppercore.util.FireworkUtil;
import xyz.upperlevel.uppercore.util.Laser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final Set<Player> shooters = new HashSet<>(); // the players that have shot

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
                    gamePhase.setPhase(new EndingPhase(gamePhase, gamers.get(0).getPlayer()));
                    break;
                case 0:
                    // already handled by GamePhase
                    break;
            }
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

    private void onShoot(Player player) {
        if (shooters.contains(player)) {
            QuakeAccount account = Quake.getAccount(player);
            shooters.add(player);

            new Laser(Quake.get(), player.getEyeLocation(), 150, 0.25,
                    (step, hits) -> {
                        List<Player> players = new ArrayList<>(gamePhase.getArena().getPlayers());
                        account.getSelectedMuzzle().getParticles()
                                .forEach(particle -> particle.display(step, players));

                        int killCount = 0;

                        for (Player hit : hits) {
                            if (gamePhase.isGamer(hit)) {
                                killCount++;
                                boolean headshot = step.getY() - hit.getLocation().getY() > 1.4; // head height
                                // TODO: make headshot height configurable

                                Railgun gun = account.getGun();
                                Message message = headshot ? headshotMessage : shotMessage;
                                message = message.filter(
                                        "killer", player.getName(),
                                        "killed", hit.getName(),
                                        "kill_message", (gun == null || gun.getKillMessage() == null) ? defaultKillMessage : gun.getKillMessage()
                                );
                                arena.broadcast(message);

                                onKill(player, hit, headshot);

                                account.getSelectedKillSound().play(step);
                                FireworkEffect.Type type = account.getSelectedBarrel().getFireworkType();
                                Color color = account.getSelectedLaser().getFireworkColor();
                                FireworkUtil.instantFirework(
                                        step,
                                        FireworkEffect.builder()
                                                .with(type)
                                                .withColor(color)
                                                .build());
                            }
                        }

                        MultiStab.tryReach(gamePhase, gamePhase.getGamer(player), killCount);
                    },
                    () -> {}
            ).shoot();

            player.setExp(1.0f);
            long from = (long) account.getSelectedTrigger().getFiringSpeed();
            new Countdown(Quake.get(), from, 1,
                    tick -> player.setExp((float) (tick / from)),
                    () -> {
                        player.setExp(0.0f); // to be sure
                        shooters.remove(player);
                    }
            ).start();
        }
    }

    private void onDash(Player player) {
        Dash.swish(player); // todo code clean up like above?
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
        defaultKillMessage = config.getString("default-kill-message");
        shotMessage = config.getMessageRequired("shot-message");
        headshotMessage = config.getMessageRequired("headshot-message");

        sneakDisabled = config.getBoolRequired("sneak-disabled");
        sneakDisabledMessage = config.getMessageRequired("sneak-disabled-message");

        killsToWin = config.getIntRequired("kills-to-win");

        hotbar = config.getRequired("playing-hotbar", PlayingHotbar.class, null);
    }
}
