package xyz.upperlevel.quakecraft;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.quakecraft.arena.ArenaManager;
import xyz.upperlevel.quakecraft.arena.arguments.ArenaArgumentParser;
import xyz.upperlevel.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.quakecraft.game.GameManager;
import xyz.upperlevel.quakecraft.game.LobbyPhase;
import xyz.upperlevel.quakecraft.game.countdown.CountdownPhase;
import xyz.upperlevel.quakecraft.game.ending.EndingPhase;
import xyz.upperlevel.quakecraft.game.gains.GainType;
import xyz.upperlevel.quakecraft.game.playing.Bullet;
import xyz.upperlevel.quakecraft.game.playing.Dash;
import xyz.upperlevel.quakecraft.game.playing.KillStreak;
import xyz.upperlevel.quakecraft.game.playing.PlayingPhase;
import xyz.upperlevel.quakecraft.game.waiting.WaitingPhase;
import xyz.upperlevel.quakecraft.placeholders.QuakePlaceholders;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.powerup.arguments.PowerupEffectArgumentParser;
import xyz.upperlevel.quakecraft.shop.ShopCategory;
import xyz.upperlevel.quakecraft.shop.purchase.ConfirmPurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.quakecraft.shop.railgun.RailgunSelectGui;
import xyz.upperlevel.uppercore.board.BoardRegistry;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.database.Store;
import xyz.upperlevel.uppercore.gui.GuiRegistry;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.hotbar.HotbarRegistry;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.util.CrashUtil;

import java.io.IOException;

import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@Getter
public class Quakecraft extends JavaPlugin {

    private static Quakecraft instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;
    private ShopCategory shop;

    // core
    private BoardRegistry boards;
    private GuiRegistry guis;
    private HotbarRegistry hotbars;
    private Store store;

    private MessageManager messages;

    private Config customConfig;

    // confirm gui
    private ConfirmPurchaseGui.Options defConfirmOptions;

    @Override
    public void onEnable() {
        instance = this;

        try {
            saveDefaultConfig();

            //Load command messages
            arenaManager = new ArenaManager();
            gameManager = new GameManager();

            guis = new GuiRegistry(this);
            hotbars = new HotbarRegistry(this);
            boards =  new BoardRegistry(this);
            store = new Store(this);

            arenaManager.load();
            gameManager.load();

            loadConfig();

            shop = new ShopCategory();
            shop.load();

            defConfirmOptions = ConfirmPurchaseGui.load();

            PlaceholderUtil.register(this, new QuakePlaceholders());
            ArgumentParserSystem.register(new ArenaArgumentParser());
            ArgumentParserSystem.register(new PowerupEffectArgumentParser());

            new QuakeCommand().subscribe();

            playerManager = new QuakePlayerManager();
        } catch (Throwable t) {
            CrashUtil.saveCrash(this, t);
            setEnabled(false);
        }
    }

    public void loadConfig() {
        customConfig = Config.wrap(getConfig());
        messages = MessageManager.load(this);

        loadSafe("commands", QuakeCommand::loadConfig);
        loadSafe("killstreak", KillStreak::loadConfig);
        loadSafe("dash", Dash::loadConfig);
        loadSafe("purchase-gui", PurchaseGui::loadConfig);
        loadSafe("railgun", RailgunSelectGui::loadConfig);
        loadSafe("gain", GainType::loadConfig);
        loadSafe("bullet", Bullet::loadConfig);
        loadSafe("railgun", Railgun::loadConfig);
        //---boards---
        loadSafe("waiting phase", WaitingPhase::loadConfig);
        loadSafe("playing phase", PlayingPhase::loadConfig);
        loadSafe("countdown phase", CountdownPhase::loadConfig);
        loadSafe("ending phase", EndingPhase::loadConfig);
        loadSafe("lobby phase", LobbyPhase::loadConfig);

        PowerupEffectManager.load(customConfig.getConfigRequired("powerups"));
    }

    public void openConfirmPurchase(Player player, Purchase<?> purchase, Link onAccept, Link onDecline) {
        guis().open(player, new ConfirmPurchaseGui(purchase, defConfirmOptions, onAccept, onDecline));
    }

    @Override
    public void onDisable() {
        try {
            playerManager.close();
            gameManager.save();
            arenaManager.save();
        } catch (IOException e) {
            getLogger().severe("Cannot save game/arena settings: " + e);
        }
        gameManager.stop();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static Quakecraft get() {
        return instance;
    }
}
