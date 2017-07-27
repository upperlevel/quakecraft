package xyz.upperlevel.spigot.quakecraft;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.spigot.quakecraft.arena.ArenaManager;
import xyz.upperlevel.spigot.quakecraft.arena.arguments.ArenaArgumentParser;
import xyz.upperlevel.spigot.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.spigot.quakecraft.game.GameManager;
import xyz.upperlevel.spigot.quakecraft.placeholders.QuakePlaceholders;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.ConfirmPurchaseGui;
import xyz.upperlevel.spigot.quakecraft.shop.ShopCategory;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.GuiRegistry;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.hotbar.HotbarRegistry;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.board.BoardRegistry;

import java.io.IOException;

import static xyz.upperlevel.uppercore.Uppercore.guis;

@Getter
public class QuakeCraftReloaded extends JavaPlugin {

    private static QuakeCraftReloaded instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;
    private ShopCategory shop;

    // core
    private BoardRegistry boards;
    private GuiRegistry guis;
    private HotbarRegistry hotbars;

    private MessageManager messages;

    // confirm gui
    private ConfirmPurchaseGui.Options defConfirmOptions;

    @Override
    public void onEnable() {
        instance = this;

        messages = MessageManager.load(this);

        saveDefaultConfig();

        Config config = Config.wrap(getConfig());

        arenaManager = new ArenaManager();
        gameManager = new GameManager();

        try {
            //Load command messages
            QuakeCommand.loadConfig();

            guis = new GuiRegistry(this);
            hotbars = new HotbarRegistry(this);
            boards =  new BoardRegistry(this);

            arenaManager.load();
            gameManager.load();
            try {
                PurchaseGui.load(config.getConfigRequired("purchase-gui"));
            } catch (InvalidConfigurationException e) {
                e.addLocalizer("while parsing purchase-gui");
                throw e;
            }
            shop = new ShopCategory();
            shop.load();

            defConfirmOptions = ConfirmPurchaseGui.load();
        } catch (InvalidConfigurationException e) {
            //QuakeCraftReloaded.get().getLogger().severe(e.getErrorMessage("Error while enabling QuakeCraft"));
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        PlaceholderUtil.register(this, new QuakePlaceholders());
        ArgumentParserSystem.register(new ArenaArgumentParser());

        new QuakeCommand().subscribe();

        playerManager = new QuakePlayerManager();
        playerManager.registerAll();
    }

    public void openConfirmPurchase(Player player, Purchase<?> purchase, Link onAccept, Link onDecline) {
        guis().open(player, new ConfirmPurchaseGui(purchase, defConfirmOptions, onAccept, onDecline));
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
