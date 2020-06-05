package xyz.upperlevel.quakecraft.phases.game;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent.ArenaQuitReason;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.TypeUtil;

import java.util.*;

public class EndingPhase extends Phase {
    private static Message endGainMessage;
    private static Message endRankingHeader;
    private static NavigableMap<Integer, Message> endRankingBody;
    private static Message endRankingFooter;

    private static GainType baseGain;
    private static GainType firstGain;
    private static GainType secondGain;
    private static GainType thirdGain;

    private static Message rejoinMessage;

    @Getter
    private final QuakeArena arena;

    @Getter
    private Player winner;

    @Getter
    private final GamePhase gamePhase;

    private final WinnerCelebration winnerCelebration;
    private final BukkitRunnable endingTask;

    public EndingPhase(GamePhase gamePhase, Player winner) {
        super("ending");
        this.gamePhase = gamePhase;
        this.arena = gamePhase.getArena();
        this.winner = winner;

        this.winnerCelebration = new WinnerCelebration(winner);
        this.endingTask = new BukkitRunnable() {
            @Override
            public void run() {
                winnerCelebration.cancel();
                reward();
                resetArena();
            }
        };
    }

    private void resetArena() {
        new ArrayList<>(arena.getPlayers()).forEach(p -> arena.quit(p, ArenaQuitReason.ARENA_END));
        arena.getPhaseManager().setPhase(new LobbyPhase(arena));
    }

    private void reward() {
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
        PlaceholderRegistry<?> reg = gamePhase.getPlaceholders();

        List<PlaceholderValue<String>> lines = new ArrayList<>(endRankingHeader.filter(reg).getLines());
        int playerCount = gamePhase.getGamers().size();
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

    @Override
    public void onEnable(Phase prev) {
        super.onEnable(prev);
        printRanking();
        winnerCelebration.start();
        endingTask.runTaskLater(Quake.get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        super.onDisable(next);
        winnerCelebration.cancel();
        endingTask.cancel();
        gamePhase.clearPlayers();
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            e.setCancelled(true); // Players can't join when the game ends.
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            gamePhase.clearPlayer(p);
            if (p == winner.getPlayer()) { // If the winner exits its celebration stops.
                winnerCelebration.cancel();
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
        endRankingBody = endRanking.getRequired("body", TypeUtil.typeOf(NavigableMap.class, Integer.class, Message.class));

        endRankingFooter = endRanking.getMessageRequired("footer");

        rejoinMessage = Quake.getConfigSection("game").getMessageRequired("rejoin-message");

        //signLines = manager.getConfig().getMessageStrList("ending-sign");
    }
}
