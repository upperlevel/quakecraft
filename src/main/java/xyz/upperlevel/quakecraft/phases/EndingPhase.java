package xyz.upperlevel.quakecraft.phases;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.game.GainType;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.nms.impl.MessageNms;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.placeholder.message.MessageManager;
import xyz.upperlevel.uppercore.util.TypeUtil;

import java.util.*;

import static xyz.upperlevel.uppercore.Uppercore.hotbars;

public class EndingPhase implements Phase, Listener {
    private static final Random random = new Random();

    private static Message endGainMessage;
    private static Message endRankingHeader;
    private static NavigableMap<Integer, Message> endRankingBody;
    private static Message endRankingFooter;

    private static GainType baseGain;
    private static GainType firstGain;
    private static GainType secondGain;
    private static GainType thirdGain;

    private static boolean autoJoin;
    private static Message rejoinMessage;

    private static Hotbar hotbar;

    @Getter
    private final QuakeArena arena;

    @Getter
    private Player winner;

    @Getter
    private final GamePhase gamePhase;

    private final BukkitRunnable fireworksTask, endingTask;

    public EndingPhase(GamePhase gamePhase, Player winner) {
        this.gamePhase = gamePhase;
        this.arena = gamePhase.getArena();
        this.winner = winner;

        this.fireworksTask = new BukkitRunnable() {
            @Override
            public void run() {
                Firework f = winner.getPlayer().getWorld().spawn(winner.getPlayer().getLocation(), Firework.class);
                FireworkMeta fm = f.getFireworkMeta();
                fm.addEffect(FireworkEffect.builder()
                        .withColor(Color.fromRGB(
                                random.nextInt(255),
                                random.nextInt(255),
                                random.nextInt(255)
                        ))
                        .build());
                fm.setPower(1);
                f.setFireworkMeta(fm);
            }
        };
        this.endingTask = new BukkitRunnable() {
            @Override
            public void run() {
                fireworksTask.cancel();
                giveGains();
                arena.getPhaseManager().setPhase(new LobbyPhase(arena));
            }
        };
    }

    public void giveGains() {
        for (Gamer p : gamePhase.getGamers()) {
            baseGain.grant(p);
        }
        Iterator<Gamer> ranking = gamePhase.getGamers().iterator();
        if (ranking.hasNext()) {
            firstGain.grant(ranking.next());
            if (ranking.hasNext()) {
                secondGain.grant(ranking.next());
                if (ranking.hasNext()) {
                    thirdGain.grant(ranking.next());
                }
            }
        }
        if (EconomyManager.isEnabled()) {
            for (Gamer p : gamePhase.getGamers()) {
                EconomyManager.get(p.getPlayer()).give(p.coins);
                endGainMessage.send(p.getPlayer(), "money", EconomyManager.format(p.coins));
            }
        } else {
            Quake.get().getLogger().warning("Vault not found, no money given!");
        }
    }

    public void printRanking() {
        PlaceholderRegistry<?> reg = gamePhase.getPlaceholderRegistry();
        List<PlaceholderValue<String>> lines = new ArrayList<>();

        lines.addAll(endRankingHeader.filter(reg).getLines());
        int playerCount = arena.getPlayers().size();
        Map.Entry<Integer, Message> bodyEntry = endRankingBody.floorEntry(playerCount);
        if (bodyEntry == null) {
            Quake.get().getLogger().severe("ERROR: cannot find ending ranking body for: " + playerCount + ", indexes " + endRankingBody.keySet());
        } else {
            lines.addAll(bodyEntry.getValue().filter(reg).getLines());
        }
        lines.addAll(endRankingFooter.filter(reg).getLines());

        Message filtered = new Message(lines);

        for (Player player : arena.getPlayers()) {
            filtered.send(player, reg);
        }
    }

    private void setupPlayer(Player player) {
        hotbars().view(player).addHotbar(hotbar);
    }

    private void clearPlayer(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
        // board now is on GamePhase
    }

    @Override
    public void onEnable(Phase prev) {
        Bukkit.getPluginManager().registerEvents(this, Quake.get());
        printRanking();

        gamePhase.getGamers().forEach(g -> setupPlayer(g.getPlayer()));

        fireworksTask.runTaskTimer(Quake.get(), 0, 20); // one firework per second
        endingTask.runTaskLater(Quake.get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        fireworksTask.cancel();
        endingTask.cancel();

        gamePhase.getGamers().forEach(g -> clearPlayer(g.getPlayer()));

        if (!autoJoin) {
            Location hub = Quake.get().getGame().getHub();
            if (hub != null) {
                for (Player player : arena.getPlayers()) {
                    arena.quit(player);
                    BaseComponent[] comps = TextComponent.fromLegacyText(String.join("\n", rejoinMessage.get(player)));
                    ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quake join " + arena.getId());
                    for (BaseComponent comp : comps)
                        comp.setClickEvent(event);
                    MessageNms.sendJson(player, comps);
                    player.teleport(hub);
                }
            } else {
                Quake.get().getLogger().severe("autoJoin disabled but hub location not set, use '/quake lobby set' to set the global lobby location");
            }
        }
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            // already handled in GamePhase
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer());
            if (e.getPlayer() == winner.getPlayer()) {
                fireworksTask.cancel(); // if the winner exits we have to stop the fireworks
            }
        }
    }

    public static void loadGains() {
        baseGain = GainType.create("base-gain");
        firstGain = GainType.create("1-place-gain");
        secondGain = GainType.create("2-place-gain");
        thirdGain = GainType.create("3-place-gain");
    }

    public static void loadConfig() {
        Config cfg = Quake.getConfigSection("messages.game");
        endGainMessage = cfg.getMessageRequired("end-gain");

        Config endRanking = cfg.getConfigRequired("end-ranking");
        endRankingHeader = endRanking.getMessageRequired("header");
        endRankingBody = endRanking.getRequired("body", TypeUtil.typeOf(NavigableMap.class, Integer.class, Message.class), null);

        endRankingFooter = endRanking.getMessageRequired("footer");

        autoJoin = Quake.get().getCustomConfig().getBoolRequired("auto-join");
        rejoinMessage = Quake.get().getCustomConfig().getMessageRequired("rejoin-message");

        hotbar = Quake.get().getCustomConfig().getRequired("game.ending-hotbar", Hotbar.class, Quake.get());

        //signLines = manager.getConfig().getMessageStrList("ending-sign");
    }
}
