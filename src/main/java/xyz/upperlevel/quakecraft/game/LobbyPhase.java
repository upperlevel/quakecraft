package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.quakecraft.game.waiting.WaitingPhase;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.uppercore.game.PhaseManager;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.util.PlayerUtil;

import java.util.List;

import static xyz.upperlevel.quakecraft.Quakecraft.get;

public class LobbyPhase extends PhaseManager implements Phase, Listener {
    private static Message joinMsg;
    private static Message quitMsg;

    @Getter
    private final Game game;

    public LobbyPhase(Game game) {
        this.game = game;
    }

    private void setup(Player player) {
        PlayerUtil.clearInventory(player);
        PlayerUtil.restore(player);
        player.teleport(game.getArena().getLobby());
        player.setGameMode(GameMode.ADVENTURE);
    }

    private void clear(Player p) {
        PlayerUtil.restore(p);
    }

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());
        for (Player player : game.getPlayers())
            setup(player);
        setPhase(new WaitingPhase(this));
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);
        for (Player p : game.getPlayers())
            clear(p);
        setPhase(null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameJoin(GameJoinEvent e) {
        if (e.getGame() == game) {
            Player p = e.getPlayer();
            setup(p);
            game.broadcast(joinMsg, PlaceholderRegistry.create()
                    .set("player_name", p.getName())
                    .set("players", game.getPlayers().size())
                    .set("min_players", game.getMinPlayers())
                    .set("max_players", game.getMaxPlayers()));
        }
    }

    @EventHandler
    public void onGameQuit(GameQuitEvent e) {
        if (e.getGame() == game) {
            Player p = e.getPlayer();
            game.broadcast(quitMsg, PlaceholderRegistry.create()
                    .set("player_name", p.getName())
                    .set("players", game.getPlayers().size())
                    .set("min_players", game.getMinPlayers())
                    .set("max_players", game.getMaxPlayers()));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (game.isPlaying(e.getPlayer()))
            e.setRespawnLocation(game.getArena().getLobby());
    }

    public static void loadConfig() {
        MessageManager msg = Quakecraft.get().getMessages().getSection("lobby");
        joinMsg = msg.get("join");
        quitMsg = msg.get("quit");
    }
}
