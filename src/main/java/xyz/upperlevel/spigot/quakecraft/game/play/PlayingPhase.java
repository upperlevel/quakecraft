package xyz.upperlevel.spigot.quakecraft.game.play;

import lombok.Data;
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
import org.bukkit.inventory.PlayerInventory;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserSpreadEvent;
import xyz.upperlevel.spigot.quakecraft.events.LaserStabEvent;
import xyz.upperlevel.spigot.quakecraft.game.EndingPhase;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.spigot.quakecraft.powerup.Powerup;
import xyz.upperlevel.spigot.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.particle.Particle;
import xyz.upperlevel.uppercore.task.Timer;
import xyz.upperlevel.uppercore.task.UpdaterTask;
import xyz.upperlevel.uppercore.util.TextUtil;
import xyz.upperlevel.uppercore.util.nms.impl.entity.FireworkNms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;
import static xyz.upperlevel.uppercore.Uppercore.messages;

@Data
public class PlayingPhase implements Phase, Listener {
    private static Message shotMessage;
    private static Message headshotMessage;
    private static Message snakeDisabledMessage;
    private static String defKillMessage;
    private static PlayingHotbar sampleHotbar;
    private static PlayingBoard sampleBoard;

    private static int gunSlot;


    private final Game game;
    private final GamePhase parent;

    private final PlayingHotbar hotbar;
    private final PlayingBoard board;
    private final Timer timer = new Timer(get(), get().getConfig().getInt("game.countdown") * 20, 20) {
        @Override
        public void tick() {
            for (Player player : game.getPlayers()) {
                boards().view(player).render();
            }
        }

        @Override
        public void end() {
            parent.setPhase(new EndingPhase(parent));
        }
    };
    private final UpdaterTask compassUpdater;

    private Player getNearbyPlayer(Player player) {
        Player res = null;
        double dist = 0;
        for (Player other : game.getPlayers()) {
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

    public PlayingPhase(GamePhase parent) {
        this.parent = parent;
        this.game = parent.getGame();
        this.hotbar = sampleHotbar;
        this.board = new PlayingBoard(this, sampleBoard);
        compassUpdater = new UpdaterTask(20 * 5, () -> {
            for (Player player : game.getPlayers()) {
                Player target = getNearbyPlayer(player);
                if (target != null)
                    player.setCompassTarget(target.getLocation());
            }
        });
    }

    public void setup(Player player) {
        hotbars().view(player).addHotbar(hotbar);
        boards().view(player).setBoard(board);
        QuakePlayer qp = QuakeCraftReloaded.get().getPlayerManager().getPlayer(player);
        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(new ItemStack[]{
                qp.getSelectedBoot().getItem().resolve(player),
                qp.getSelectedLegging().getItem().resolve(player),
                qp.getSelectedChestplate().getItem().resolve(player),
                qp.getSelectedHat().getItem().resolve(player)
        });
    }

    public void update() {
        for (Player player : game.getPlayers())
            boards().view(player).render();
    }

    public void updateRanking() {
        parent.getRanking().sort((prev, next) -> (next.getKills() - prev.getKills()));
    }

    public void clear(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
        boards().view(player).clear();
    }

    public void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        List<Player> players = new ArrayList<>(game.getPlayers());
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = players.get(i);
            setup(p);
            p.teleport(game.getArena().getSpawns().get(i % game.getArena().getSpawns().size()));
        }

        for (Powerup box : getGame().getArena().getPowerups())
            box.onGameBegin(getParent());

        timer.start();
        compassUpdater.start();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);

        compassUpdater.stop();
        timer.stop();

        clear();
        for (Powerup powerup : getGame().getArena().getPowerups())
            powerup.onGameEnd();
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (game.equals(e.getGame())) {
            clear(e.getPlayer());
        }
    }

    private void kill(Participant hit, Participant shooter, boolean headshot) {
        List<Location> spawns = game.getArena().getSpawns();
        hit.getPlayer().teleport(spawns.get(new Random().nextInt(spawns.size())));

        hit.onDeath();
        shooter.onKill(headshot);

        updateRanking();
        update();

        if (shooter.kills >= game.getArena().getKillsToWin())
            parent.setPhase(new EndingPhase(parent));
    }

    public void explodeBarrel(Location location, QuakePlayer p) {
        FireworkEffect.Type type = p.getSelectedBarrel().getFireworkType();
        Color color = p.getSelectedLaser().getFireworkColor();

        FireworkNms.instantFirework(
                location,
                FireworkEffect.builder()
                        .with(type)
                        .withColor(color)
                        .build()
        );
    }

    @EventHandler
    public void onLaserStab(LaserStabEvent e) {
        if (equals(e.getPhase())) {
            Participant hit = parent.getParticipant(e.getHit());
            Participant shooter = parent.getParticipant(e.getShooter());

            QuakePlayer qshooter = e.getQShooter();
            qshooter.getSelectedKillSound().play(e.getLocation());
            explodeBarrel(e.getLocation(), e.getQShooter());

            //--- kill msg
            Railgun gun = qshooter.getGun();
            Message message = e.isHeadshot() ? headshotMessage : shotMessage;

            message = message.filter(
                    "killer", e.getShooter().getName(),
                    "killed", e.getHit().getName(),
                    "kill_message", (gun == null || gun.getKillMessage() == null) ? defKillMessage : gun.getKillMessage()
            );
            message.broadcast(game.getPlayers());

            kill(hit, shooter, e.isHeadshot());
        }
    }

    @EventHandler
    public void onLaserSpread(LaserSpreadEvent e) {
        if (this.equals(e.getPhase())) {
            Location loc = e.getLocation();
            Collection<Player> players = getGame().getPlayers();
            for (Particle p : e.getParticles())
                p.display(loc, players);
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!game.equals(get().getGameManager().getGame(p)))
            return;
        if (p.getInventory().getHeldItemSlot() == gunSlot) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                Bullet.shoot(this, p);
            else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
                Dash.dash(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSnake(PlayerToggleSneakEvent e) {
        if (!e.isSneaking() || !game.equals(get().getGameManager().getGame(e.getPlayer())))
            return;
        e.setCancelled(true);
        snakeDisabledMessage.send(e.getPlayer());
    }

    public static void loadConfig() {
        MessageManager messages = QuakeCraftReloaded.get().getMessages().getSection("game");
        shotMessage = messages.get("shot");
        headshotMessage = messages.get("headshot");
        snakeDisabledMessage = messages.get("snake-disabled");
        defKillMessage = TextUtil.translatePlain(messages.getConfig().getStringRequired("def-kill-message"));

        gunSlot = QuakeCraftReloaded.get().getCustomConfig().getIntRequired("playing-hotbar.gun.slot");

        sampleHotbar = PlayingHotbar.deserialize("playing-solo", Config.wrap(ConfigUtils.loadConfig(
                QuakeCraftReloaded.get().getHotbars().getFolder(),
                "playing-solo.yml"
        )));

        sampleBoard = PlayingBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                QuakeCraftReloaded.get().getBoards().getFolder(),
                "playing-solo.yml"
        )));
    }
}
