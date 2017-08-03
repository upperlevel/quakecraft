package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.uppercore.game.PhaseManager;
import xyz.upperlevel.uppercore.util.PlayerUtil;
import xyz.upperlevel.uppercore.game.Phase;
import xyz.upperlevel.spigot.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.HashMap;
import java.util.Map;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

public class LobbyPhase extends PhaseManager implements Phase, Listener {
    private static Message joinMsg;
    private static Message quitMsg;
    private static Map<String, Message> countdownMsg = new HashMap<>();

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
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent e) {
        if (game.equals(e.getGame())) {
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
        if (game.equals(e.getGame())) {
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
        MessageManager msg = QuakeCraftReloaded.get().getMessages().getSection("lobby");
        joinMsg = msg.get("join");
        quitMsg = msg.get("quit");
    }
}
