package xyz.upperlevel.spigot.quakecraft;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.spigot.quakecraft.arena.*;
import xyz.upperlevel.spigot.quakecraft.arguments.ArenaArgumentParser;
import xyz.upperlevel.spigot.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.spigot.quakecraft.game.GameManager;
import xyz.upperlevel.spigot.quakecraft.placeholders.QuakePlaceholders;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.gui.GuiRegistry;
import xyz.upperlevel.uppercore.gui.GuiSystem;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarRegistry;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardRegistry;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import java.io.File;
import java.io.IOException;

@Getter
public class QuakeCraftReloaded extends JavaPlugin {

    private static QuakeCraftReloaded instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;

    // core
    private GuiRegistry guis;
    private HotbarRegistry hotbars;
    private ScoreboardRegistry scoreboards;

    @Override
    public void onEnable() {
        instance = this;

        arenaManager = new ArenaManager();
        gameManager = new GameManager();
        playerManager = new QuakePlayerManager();

        PlaceholderUtil.register(this, new QuakePlaceholders());
        ArgumentParserSystem.register(new ArenaArgumentParser());

        new QuakeCommand().subscribe();

        guis = GuiSystem.subscribe(this);

        hotbars = HotbarSystem.subscribe(this);

        scoreboards = ScoreboardSystem.subscribe(this);
        scoreboards.load(new File(scoreboards.getFolder(), "solo_quake_lobby.yml"));
        scoreboards.load(new File(scoreboards.getFolder(), "solo_quake_countdown.yml"));

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
