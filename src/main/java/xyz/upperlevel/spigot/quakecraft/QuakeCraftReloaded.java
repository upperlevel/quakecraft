package xyz.upperlevel.spigot.quakecraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.spigot.quakecraft.arena.ArenaManager;
import xyz.upperlevel.spigot.quakecraft.arguments.ArenaArgumentParser;
import xyz.upperlevel.spigot.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.spigot.quakecraft.game.GameManager;
import xyz.upperlevel.spigot.quakecraft.placeholders.QuakePlaceholders;
import xyz.upperlevel.spigot.quakecraft.shop.PurchasesGui;
import xyz.upperlevel.spigot.quakecraft.shop.ShopCategory;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.GuiRegistry;
import xyz.upperlevel.uppercore.gui.GuiSystem;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarRegistry;
import xyz.upperlevel.uppercore.gui.hotbar.HotbarSystem;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardRegistry;
import xyz.upperlevel.uppercore.scoreboard.ScoreboardSystem;

import java.io.IOException;

@Getter
public class QuakeCraftReloaded extends JavaPlugin {

    private static QuakeCraftReloaded instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;
    private ShopCategory shop;

    // core
    private GuiRegistry guis;
    private HotbarRegistry hotbars;
    private ScoreboardRegistry scoreboards;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        arenaManager = new ArenaManager();
        gameManager = new GameManager();
        playerManager = new QuakePlayerManager();
        shop = new ShopCategory();

        PlaceholderUtil.register(this, new QuakePlaceholders());
        ArgumentParserSystem.register(new ArenaArgumentParser());

        new QuakeCommand().subscribe();

        guis = GuiSystem.subscribe(this);

        try {
            hotbars = HotbarSystem.subscribe(this);
            hotbars.loadDefaultFolder();

            scoreboards = ScoreboardSystem.subscribe(this);
            scoreboards.loadDefaultFolder();

            arenaManager.load();
            gameManager.load();
            try {
                PurchasesGui.load(Config.wrap(getConfig()).getConfigRequired("purchase-gui"));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("while parsing purchase-gui");
                throw e;
            }
            shop.load();
        } catch (InvalidConfigurationException e) {
            //QuakeCraftReloaded.get().getLogger().severe(e.getErrorMessage("Error while enabling QuakeCraft"));
            e.printStackTrace();
            setEnabled(false);
        }
        playerManager.registerAll();
    }

    @Override
    public void onDisable() {
        try {
            gameManager.save();
            arenaManager.save();
        } catch (IOException e) {
            System.err.println("ERR"); // todo error system
        }
        gameManager.stop();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static QuakeCraftReloaded get() {
        return instance;
    }
}
