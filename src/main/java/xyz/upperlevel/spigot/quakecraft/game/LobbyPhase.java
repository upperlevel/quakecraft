package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.core.PhaseManager;
import xyz.upperlevel.spigot.quakecraft.core.PlayerUtil;
import xyz.upperlevel.spigot.quakecraft.events.GameJoinEvent;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

public class LobbyPhase extends PhaseManager implements Phase, Listener {

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
    }

    @EventHandler
    public void onGameJoin(GameJoinEvent e) {
        setup(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (game.isPlaying(e.getPlayer()))
            e.setRespawnLocation(game.getArena().getLobby());
    }
}
