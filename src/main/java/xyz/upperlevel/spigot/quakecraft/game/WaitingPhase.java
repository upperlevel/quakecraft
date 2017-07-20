package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
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
    private final Board board;

    public WaitingPhase(LobbyPhase parent) {
        this.game = parent.getGame();
        this.parent = parent;

        hotbar = get().getHotbars().get("waiting_solo");
        board = get().getScoreboards().get("waiting_solo");
    }

    private void setup(Player player) {
        //-------------------------hotbar
        if (hotbar != null)
            HotbarSystem.view(player).addHotbar(hotbar);
        else
            get().getLogger().warning("Hotbar not found: \"waiting_solo\"");
        //-------------------------scoreboard
        if (board != null)
            ScoreboardSystem.view(player).setBoard(board);
        else
            get().getLogger().warning("Scoreboard not found: \"waiting_solo\"");
    }

    private void clear(Player player) {
        //-------------------------hotbar
        HotbarSystem.view(player).removeHotbar(hotbar);
        //-------------------------scoreboard
        ScoreboardSystem.view(player).clear();
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
