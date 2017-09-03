package xyz.upperlevel.quakecraft.game.ending;

import lombok.Data;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.game.GamePhase;
import xyz.upperlevel.quakecraft.game.LobbyPhase;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.quakecraft.game.gains.GainType;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.ConfigUtils;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quakecraft.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
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

    private static EndingHotbar sampleHotbar;
    private static EndingBoard sampleBoard;

    private Participant winner;

    private final Game game;
    private final GamePhase parent;

    private final EndingHotbar hotbar;
    private final EndingBoard board;

    private final BukkitRunnable fireworksTask = new BukkitRunnable() {
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

    private final BukkitRunnable endingTask = new BukkitRunnable() {
        @Override
        public void run() {
            fireworksTask.cancel();
            giveGains();
            game.getPhaseManager().setPhase(new LobbyPhase(game));
        }
    };

    public EndingPhase(GamePhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        this.hotbar = sampleHotbar;
        this.board = new EndingBoard(this, sampleBoard);
    }

    public void giveGains() {
        for (Participant p : parent.getParticipants())
            baseGain.grant(p);

        Iterator<Participant> ranking = parent.getRanking().iterator();
        if (ranking.hasNext()) {
            firstGain.grant(ranking.next());
            if (ranking.hasNext()) {
                secondGain.grant(ranking.next());
                if (ranking.hasNext())
                    thirdGain.grant(ranking.next());
            }
        }
        if (EconomyManager.isEnabled()) {
            for (Participant p : parent.getParticipants()) {
                EconomyManager.get(p.getPlayer()).give(p.coins);
                endGainMessage.send(p.getPlayer(), "money", EconomyManager.format(p.coins));
            }
        } else
            get().getLogger().warning("Vault not found, no money given!");
    }

    public void printRanking() {
        PlaceholderRegistry gameReg = game.getPlaceholders();
        PlaceholderRegistry gameRegParent = gameReg.getParent();
        gameReg.setParent(null);

        PlaceholderRegistry<?> reg = PlaceholderRegistry.create(gameReg);
        reg.set("ranking_name", (p, s) -> {
            if (s == null)
                return null;
            try {
                return getParent().getRanking().get(Integer.parseInt(s) - 1).getPlayer().getName();
            } catch (Exception e) {
                return null;
            }
        });

        reg.set("ranking_kills", (p, s) -> {
            try {
                return String.valueOf(getParent().getRanking().get(Integer.parseInt(s) - 1).kills);
            } catch (Exception e) {
                return null;
            }
        });

        reg.set("ranking_gun", (p, s) -> {
            try {
                QuakePlayer player = get().getPlayerManager().getPlayer(getParent().getRanking().get(Integer.parseInt(s) - 1).getPlayer());
                return player.getGun() == null ? Railgun.CUSTOM_NAME.resolve(p) : player.getGun().getName().resolve(p);
            } catch (Exception e) {
                return null;
            }
        });
        List<PlaceholderValue<String>> lines = new ArrayList<>();

        lines.addAll(endRankingHeader.filter(reg).getLines());
        Map.Entry<Integer, Message> bodyEntry = endRankingBody.floorEntry(getParent().getRanking().size());
        if(bodyEntry == null) {
            Quakecraft.get().getLogger().severe("ERROR: cannot find ending ranking body for: " + getParent().getRanking().size() + ", indexes " + endRankingBody.keySet());
        } else {
            lines.addAll(bodyEntry.getValue().filter(reg).getLines());
        }
        lines.addAll(endRankingFooter.filter(reg).getLines());

        Message filtered = new Message(lines);

        gameReg.setParent(gameRegParent);

        for (Player player : getParent().getGame().getPlayers())
            filtered.send(player);
    }

    public void setup(Player player) {
        hotbars().view(player).addHotbar(hotbar);
        boards().view(player).setBoard(board);
    }

    public void setup() {
        game.getPlayers().forEach(this::setup);
    }

    public void clear(Player player) {
        hotbars().view(player).removeHotbar(hotbar);
        boards().view(player).clear();
    }

    public void clear() {
        game.getPlayers().forEach(this::clear);
    }

    private static File getPhaseFolder() {
        return new File(get().getDataFolder(), "game/ending");
    }

    @Override
    public void onEnable(Phase previous) {
        winner = parent.getWinner();
        printRanking();

        setup();

        fireworksTask.runTaskTimer(get(), 0, 20);
        endingTask.runTaskLater(get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        endingTask.cancel();
        clear();
    }

    public static void loadGains() {
        baseGain = GainType.create("base");
        firstGain = GainType.create("1-place");
        secondGain = GainType.create("2-place");
        thirdGain = GainType.create("3-place");
    }

    public static void loadConfig() {
        MessageManager manager = get().getMessages().getSection("game");
        endGainMessage = manager.get("end-gain");
        MessageManager endRanking = manager.getSection("end-ranking");
        endRankingHeader = endRanking.get("header");
        endRankingBody =  endRanking.getConfig()
                .getSectionRequired("body")
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> Integer.parseInt(e.getKey()),
                        (Map.Entry<String, Object> e) -> Message.fromConfig(e.getValue()),
                        (a, b) -> b,
                        TreeMap::new)
                );
        endRankingFooter = endRanking.get("footer");

        sampleHotbar = EndingHotbar.deserialize(get(), Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "ending_hotbar.yml"
        )));

        sampleBoard = EndingBoard.deserialize(Config.wrap(ConfigUtils.loadConfig(
                getPhaseFolder(),
                "ending_board.yml"
        )));
    }
}
