package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.event.*;
import xyz.upperlevel.spigot.quakecraft.event.LobbyCountdownEndEvent.Reason;
import xyz.upperlevel.uppercore.scoreboard.BoardView;

import static org.bukkit.ChatColor.*;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;
import static xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem.view;

@Data
public class LobbyPhase implements Phase, Listener {

    private final Game game;
    private int timer;

    public LobbyPhase(Game game) {
        this.game = game;
    }

    public void updateScoreboard() {
        for (Player p : game.getPlayers()) view(p).update();
    }

    private final BukkitRunnable countdown = new BukkitRunnable() {

        @Override
        public void run() {
            for (Player player : getGame().getPlayers())
                player.sendMessage(timer + "");

            Bukkit.getPluginManager().callEvent(new LobbyCountdownTickEvent(getGame(), LobbyPhase.this, timer));

            if (timer > 0)
                timer--;
            else {
                game.getPhaseManager().setPhase(new GamePhase(game));
            }
        }
    };

    @Getter
    private boolean counting;

    public void setupPlayer(Player p) {
        p.teleport(game.getArena().getLobby());
        p.setGameMode(GameMode.ADVENTURE);

        view(p).setBoard(get().getScoreboards().get("solo_quake_lobby"));
    }

    private final BukkitRunnable scoreboardUpdater = new BukkitRunnable() {
        @Override
        public void run() {
            updateScoreboard();
        }
    };

    @Override
    public void onEnable(Phase previous) {
        Bukkit.getPluginManager().registerEvents(this, get());

        for (Player p : game.getPlayers())
            setupPlayer(p);

        if (!counting && game.getPlayers().size() >= game.getArena().getMinPlayers())
            startCountdown();

        scoreboardUpdater.runTaskTimer(get(), 0, 20 * 3);
    }

    @Override
    public void onDisable(Phase next) {
        HandlerList.unregisterAll(this);

        scoreboardUpdater.cancel();
        for (Player p : Bukkit.getOnlinePlayers())
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        if (counting)
            stopCountdown(Reason.INTERRUPT_PHASE);
    }

    private void startCountdown() {
        if (counting)
            return;
        timer = 10;
        countdown.runTaskTimer(get(), 0, 20);
        counting = true;
        Bukkit.getPluginManager().callEvent(new LobbyCountdownStartEvent(getGame(), this));
    }

    private void stopCountdown(Reason reason) {
        if (!counting)
            return;

        countdown.cancel();
        counting = false;

        Bukkit.getPluginManager().callEvent(new LobbyCountdownEndEvent(getGame(), this, reason));
    }

    @EventHandler
    public void onJoin(GameJoinEvent e) {
        Game game = getGame();

        if (!game.equals(e.getGame()))
            return;

        setupPlayer(e.getPlayer());

        int size = game.getPlayers().size();

        if (size > game.getArena().getMaxPlayers()) {
            e.getPlayer().sendMessage(RED + "The match is full.");
            e.setCancelled(true);
            return;
        }

        if (size >= getGame().getArena().getMinPlayers()) {
            startCountdown();
            getGame().broadcast(GREEN + "Countdown started!");
        }
    }

    @EventHandler
    public void onQuit(GameQuitEvent e) {
        Game game = getGame();
        if (!game.equals(e.getGame()))
            return;

        if (counting && game.getPlayers().size() < game.getArena().getMinPlayers()) {
            stopCountdown(Reason.FEW_PLAYERS);
            getGame().broadcast("countdown stopped");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (getGame().isPlaying(e.getPlayer()))
            e.setRespawnLocation(getGame().getArena().getLobby());
    }
}
