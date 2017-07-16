package xyz.upperlevel.spigot.quakecraft;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.spigot.quakecraft.arena.*;
import xyz.upperlevel.spigot.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.spigot.quakecraft.game.GameManager;

import java.io.IOException;

@Getter
public class QuakeCraftReloaded extends JavaPlugin {

    private static QuakeCraftReloaded instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;

        arenaManager = new ArenaManager();
        gameManager = new GameManager();
        playerManager = new QuakePlayerManager();

        getCommand("qcr").setExecutor(new QuakeCommand());

        arenaManager.load();
        gameManager.load();
    }

    @Override
    public void onDisable() {
        try {
            gameManager.save();
            arenaManager.save();
        } catch (IOException e) {
            System.err.println("ERR"); // todo error system
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static QuakeCraftReloaded get() {
        return instance;
    }
}
