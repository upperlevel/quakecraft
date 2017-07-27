package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.hotbar.Hotbar;

import java.io.File;

import static sun.audio.AudioPlayer.player;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.Uppercore.boards;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;

@Data
public class EndingPhase implements Phase, Listener {
    private final Game game;
    private final GamePhase parent;

    private final EndingHotbar hotbar;
    private final EndingBoard board;

    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
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
        for (Player player : game.getPlayers())
            player.sendMessage(winner.getName() + " win the match!");

        setup();

        task.runTaskLater(get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        task.cancel();
        clear();
    }
}
