package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.event.GameJoinEvent;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.scoreboard.Board;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@Data
public class WaitingPhase implements Phase, Listener {

    private final Game game;
    private final LobbyPhase parent;
    private final Hotbar hotbar;

    public WaitingPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;

        this.hotbar = get().getHotbars().get("solo_quake_lobby_hotbar");
    }

    private void setup(Player player) {
        HotbarSystem.view(player).addHotbar(hotbar);
        Board board = get().getScoreboards().get("solo_quake_lobby_scoreboard");
        if (board != null)
            ScoreboardSystem.view(player).setBoard(board);
    }

    private void update(Player player) {
        ScoreboardSystem.view(player).update();
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
        parent.setPhase(new CountdownPhase(parent));
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        for (Player player : game.getPlayers())
            ScoreboardSystem.view(player).clear();
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame().equals(game)) {
            setup(e.getPlayer());
            update();
        }
    }
}
