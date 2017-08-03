package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.game.gains.GainType;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.io.File;
import java.util.Iterator;
import java.util.stream.Collectors;

import static sun.audio.AudioPlayer.player;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class EndingPhase implements Phase, Listener {
    private static Message endGainMessage;
    private static Message endRankingMessage;

    private static GainType baseGain;
    private static GainType firstGain;
    private static GainType secondGain;
    private static GainType thirdGain;

    private final Game game;
    private final GamePhase parent;

    private final EndingHotbar hotbar;
    private final EndingBoard board;

    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            giveGains();
            game.getPhaseManager().setPhase(new LobbyPhase(game));
        }
    };

    public EndingPhase(GamePhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        // HOTBAR
        {
            File file = new File(get().getBoards().getFolder(), "ending-solo.yml");
            if (!file.exists())
                throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
            hotbar = EndingHotbar.deserialize(get(), YamlConfiguration.loadConfiguration(file)::get);
        }
        // BOARD
        {
            File file = new File(get().getBoards().getFolder(), "ending-solo.yml");
            if (!file.exists())
                throw new IllegalArgumentException("Cannot find file: \"" + file.getPath() + "\"");
            board = EndingBoard.deserialize(this, YamlConfiguration.loadConfiguration(file)::get);
        }
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
            QuakeCraftReloaded.get().getLogger().warning("Vault not found, no money given!");
    }

    public void printRanking() {
        PlaceholderRegistry gameReg = game.getPlaceholders();
        PlaceholderRegistry gameRegParent = gameReg.getParent();
        gameReg.setParent(null);

        PlaceholderRegistry<?> reg = PlaceholderRegistry.create(gameReg);
        reg.set("ranking_name", (p, s) -> {
            if(s == null)
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
                return QuakeCraftReloaded.get().getPlayerManager().getPlayer(getParent().getRanking().get(Integer.parseInt(s) - 1).getPlayer()).getGun().getName().resolve(p);
            } catch (Exception e) {
                return null;
            }
        });

        Message filtered = new Message(
                endRankingMessage.getLines()
                        .stream()
                        .map(l -> PlaceholderValue.stringValue(l.resolve(null, reg)))
                        .collect(Collectors.toList())
        );

        gameReg.setParent(gameRegParent);

        for(Player player : getParent().getGame().getPlayers())
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
        Bukkit.getOnlinePlayers().forEach(this::clear);
    }

    @Override
    public void onEnable(Phase previous) {
        Participant winner = parent.getWinner();
        game.setWinner(winner.getPlayer());
        printRanking();

        setup();

        task.runTaskLater(get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        task.cancel();
        clear();
    }

    public static void loadGains() {
        baseGain = GainType.create("base");
        firstGain = GainType.create("1-place");
        secondGain = GainType.create("2-place");
        thirdGain = GainType.create("3-place");
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("game");
        endGainMessage = manager.get("end-gain");
        endRankingMessage = manager.get("end-ranking");
    }
}
