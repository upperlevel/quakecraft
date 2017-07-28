package xyz.upperlevel.spigot.quakecraft.game.play;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.events.*;
import xyz.upperlevel.spigot.quakecraft.game.EndingPhase;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.GamePhase;
import xyz.upperlevel.uppercore.task.Timer;

import java.io.File;
import java.util.*;

import static org.bukkit.ChatColor.RED;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class PlayingPhase implements Phase, Listener {

    private final Game game;
    private final GamePhase parent;

    private final PlayingHotbar hotbar;
    private final PlayingBoard board;

    private final Timer timer = new Timer(get(), 10 * 60 * 20, 20) {
        @Override
        public void tick() {
            for (Player player : game.getPlayers())
                boards().view(player).render();
        }

        @Override
        public void end() {
            parent.setPhase(new EndingPhase(parent));
        }
    };

    public PlayingPhase(GamePhase parent) {
        this.parent = parent;
        this.game = parent.getGame();
        // HOTBAR
        {
            File file = new File(get().getHotbars().getFolder(), "playing-solo.yml");
            if (!file.exists())
                throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
            hotbar = PlayingHotbar.deserialize(get(), "playing-solo", YamlConfiguration.loadConfiguration(file)::get);
        }
        // BOARD
        {
            File file = new File(get().getBoards().getFolder(), "playing-solo.yml");
            if (!file.exists())
                throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
            board = PlayingBoard.deserialize(this, YamlConfiguration.loadConfiguration(file)::get);
        }
    }

    public void setup(Player player) {
        hotbars().view(player).addHotbar(hotbar);
        boards().view(player).setBoard(board);
    }

    public void update() {
        for (Player player : game.getPlayers())
            boards().view(player).render();
    }

    public void updateRanking() {
        parent.getRanking().sort((prev, next) -> (next.getKills() - prev.getKills()));
    }

    public void clear(Player player) {
        hotbars()
                .view(player).removeHotbar(hotbar);
        boards()
                .view(player).clear();
    }

    public void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        List<Player> pList = new ArrayList<>(game.getPlayers());
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = pList.get(i);
            setup(p);
            p.teleport(game.getArena().getSpawns().get(i % game.getArena().getSpawns().size()));
        }
        timer.start();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        clear();
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (game.equals(e.getGame())) {
            clear(e.getPlayer());
        }
    }

    private void kill(Player hit, Player shooter) {
        List<Location> spawns = game.getArena().getSpawns();
        hit.teleport(spawns.get(new Random().nextInt(spawns.size())));

        parent.getParticipant(hit).deaths++;
        parent.getParticipant(shooter).kills++;

        updateRanking();
        update();

        if (parent.getParticipant(shooter).kills >= game.getArena().getKillsToWin())
            parent.setPhase(new EndingPhase(parent));
    }

    @EventHandler
    public void onLaserHit(LaserHitEvent e) {
        if (equals(e.getPhase())) {
            kill(e.getHit(), e.getShooter());
            game.broadcast(e.getShooter().getName() + " shot " + e.getHit().getName()); // todo kill message
        }
    }

    @EventHandler
    public void onLaserSpread(LaserSpreadEvent e) {
        if (equals(e.getPhase())) {
            e.getLocation().getWorld().spawnParticle(Particle.DRIP_LAVA, e.getLocation(), 25);
        }
    }



    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!game.equals(get().getGameManager().getGame(p)))
            return;
        if (p.getInventory().getHeldItemSlot() == get().getConfig().getInt("playing-hotbar.gun.slot")) {//TODO parse before game :(
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                Bullet.shoot(this, p);
            else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
                Dash.dash(p);
        }
    }


}