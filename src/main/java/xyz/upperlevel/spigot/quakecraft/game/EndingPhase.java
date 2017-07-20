package xyz.upperlevel.spigot.quakecraft.game;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.uppercore.gui.hotbar.Hotbar;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

public class EndingPhase implements Phase, Listener {

    private final Game game;
    private final GamePhase parent;

    private final Hotbar hotbar;

    private final BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
            game.getPhaseManager().setPhase(new LobbyPhase(game));
        }
    };

    public EndingPhase(GamePhase parent) {
        this.game = parent.getGame();
        this.parent = parent;

        hotbar = get().getHotbars().get("solo_quake_ending_hotbar");
    }

    @Override
    public void onEnable(Phase previous) {
        Participant winner = parent.getWinner();
        game.setWinner(winner.getPlayer());
        for (Player player : game.getPlayers())
            player.sendMessage(winner.getName() + " win the match!");
        task.runTaskLater(get(), 20 * 10);
    }

    @Override
    public void onDisable(Phase next) {
        task.cancel();
    }
}
