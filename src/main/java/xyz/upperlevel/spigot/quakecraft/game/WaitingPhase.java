package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.hotbar.HotbarManager;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardManager;

import java.io.File;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class WaitingPhase implements Phase, Listener {

    private final Game game;
    private final LobbyPhase parent;

    private final Hotbar hotbar;
    private final Board board;

    public WaitingPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;
        // hotbar
        hotbar = get().getHotbars().get("waiting_solo");
        // board
        File f = new File(get().getBoards().getFolder(), "waiting_solo.yml");
        if (f.exists())
            board = WaitingBoard.deserialize(this, Config.wrap(YamlConfiguration.loadConfiguration(f)));
        else {
            board = null;
            QuakeCraftReloaded.get().getLogger().info("Scoreboard not found: \"" + f.getPath() + "\"");
        }
    }

    private void setup(Player player) {
        //-------------------------hotbar
        if (hotbar != null)
            HotbarManager.view(player).addHotbar(hotbar);
        //-------------------------board
        if (board != null)
            BoardManager.view(player).setScoreboard(board);
    }

    private void clear(Player player) {
        //-------------------------hotbar
        HotbarManager.view(player).removeHotbar(hotbar);
        //-------------------------board
        BoardManager.view(player).clear();
    }

    private void clear() {
        for (Player p : game.getPlayers())
            clear(p);
    }

    private void tryStart() {
        if (game.getPlayers().size() >= game.getMinPlayers())
            parent.setPhase(new CountdownPhase(parent));
    }

    private void update(Player player) {
        BoardManager.view(player).update();
    }

    private void update() {
        for (Player player : game.getPlayers())
            update(player);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, QuakeCraftReloaded.get());
        for (Player player : game.getPlayers())
            setup(player);
        tryStart();
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        clear();
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame().equals(game)) {
            setup(e.getPlayer());
            update();
            tryStart();
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (e.getGame().equals(game)) {
            clear(e.getPlayer());
        }
    }
}
