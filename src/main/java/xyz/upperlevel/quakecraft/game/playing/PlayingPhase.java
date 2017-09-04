package xyz.upperlevel.quakecraft.game.playing;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.events.LaserSpreadEvent;
import xyz.upperlevel.quakecraft.events.LaserStabEvent;
import xyz.upperlevel.quakecraft.game.*;
import xyz.upperlevel.quakecraft.game.ending.EndingPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.board.BoardView;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.particle.Particle;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.task.Timer;
import xyz.upperlevel.uppercore.task.UpdaterTask;
import xyz.upperlevel.uppercore.util.TextUtil;
import xyz.upperlevel.uppercore.util.nms.impl.entity.FireworkNms;

import java.io.File;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static xyz.upperlevel.quakecraft.Quakecraft.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class PlayingPhase implements QuakePhase, Listener {
    private static Message shotMessage;
    private static Message headshotMessage;
    private static Message snakeDisabledMessage;
    private static String defKillMessage;

    private static PlayingHotbar sampleHotbar;
    private static PlayingBoard sampleBoard;

    private static int gunSlot;

    private static List<PlaceholderValue<String>> signLines;

    private final Game game;
    private final GamePhase parent;

    private final PlayingHotbar hotbar;
    private final PlayingBoard board;
    private final Timer timer;
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
        this.timer = new Timer(get(), get().getConfig().getInt("game.countdown") * 20, 20,
                () -> {
                    for (Player player : game.getPlayers())
                        boards().view(player).render();
                    updateSigns();
                },
                () -> parent.setPhase(new EndingPhase(parent)));
        this.compassUpdater = new UpdaterTask(20 * 5, () -> {
            for (Player player : game.getPlayers()) {
                Player target = getNearbyPlayer(player);
                if (target != null)
                    player.setCompassTarget(target.getLocation());
            }
        });
    }

    public void setup(Player player) {
        hotbars().view(player).addHotbar(hotbar);
        BoardView view = boards().view(player);
        if (game.getArena().isHideNametags()) {
            Team team = view.getHandle().registerNewTeam("hide_nametags");
            for(Player other : game.getPlayers()) {
                view.getEntries().add(other.getName());// Occupy the name entry
                team.addEntry(other.getName());
            }
            //team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setNameTagVisibility(NameTagVisibility.NEVER);
        }
        view.setBoard(board);

        QuakePlayer qp = Quakecraft.get().getPlayerManager().getPlayer(player);
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
        BoardView view = boards().view(player);

        if (game.getArena().isHideNametags()) {
            view.getEntries().remove(player.getName());// Deoccupy the name entry
            view.getHandle().getTeam("hide_nametags").unregister();
        }

        view.clear();
    }

    public void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    private static File getPhaseFolder() {
        return new File(Quakecraft.get().getDataFolder(), "game/playing");
    }

    public void updateSigns() {
        for (int i = 0; i < signLines.size(); i++) {
            for (Sign sign : game.getSigns()) {
                sign.setLine(i, signLines.get(i).resolve(null, PlaceholderRegistry.create()
                        .set("min_players", game.getMinPlayers())
                        .set("max_players", game.getMaxPlayers())
                        .set("players", game.getPlayers().size())
                        .set("countdown", timer)
                ));
            }
        }
        game.getSigns().forEach(Sign::update);
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

        updateSigns();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);

        compassUpdater.stop();
        timer.stop();

        clear();

        for (Powerup powerup : getGame().getArena().getPowerups())
            powerup.onGameEnd();
        getGame().getArena().getPowerups()
                .stream()
                .map(Powerup::getEffect)
                .distinct()
                .forEach(e -> e.clear(getParent().getParticipants()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameQuit(GameQuitEvent e) {
        if (game.equals(e.getGame())) {
            clear(e.getPlayer());
            updateSigns();
            switch (getParent().getParticipants().size()) {
                case 1:
                    Quakecraft.get().getLogger().info("One player remained, setting phase to ending");
                    parent.setPhase(new EndingPhase(parent));
                    break;
                case 0:
                    Quakecraft.get().getLogger().info("No player remained, setting phase to lobby");
                    game.getPhaseManager().setPhase(new LobbyPhase(game));
                    break;
            }
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
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (e.isSneaking() && game.equals(get().getGameManager().getGame(e.getPlayer())) && !game.getArena().isSneakEnabled()) {
            e.setCancelled(true);
            snakeDisabledMessage.send(e.getPlayer());
        }
    }

    public static void loadConfig() {
        MessageManager messages = Quakecraft.get().getMessages().getSection("game");
        shotMessage = messages.get("shot");
        headshotMessage = messages.get("headshot");
        snakeDisabledMessage = messages.get("snake-disabled");
        defKillMessage = TextUtil.translatePlain(messages.getConfig().getStringRequired("def-kill-message"));

        gunSlot = Quakecraft.get().getCustomConfig().getIntRequired("playing-hotbar.gun.slot");

        sampleHotbar = PlayingHotbar.deserialize("playing-solo", Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "playing_hotbar.yml"
        )));

        sampleBoard = PlayingBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "playing_board.yml"
        )));

        signLines = messages.getConfig().getMessageStrList("playing-sign");
    }
}