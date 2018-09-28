package xyz.upperlevel.quakecraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.quakecraft.arena.ArenaManager;
import xyz.upperlevel.quakecraft.commands.QuakecraftCommand;
import xyz.upperlevel.quakecraft.game.*;
import xyz.upperlevel.quakecraft.game.args.GameArgParser;
import xyz.upperlevel.quakecraft.game.ending.EndingPhase;
import xyz.upperlevel.quakecraft.game.gains.GainNotifier;
import xyz.upperlevel.quakecraft.game.gains.GainType;
import xyz.upperlevel.quakecraft.game.playing.Bullet;
import xyz.upperlevel.quakecraft.game.playing.Dash;
import xyz.upperlevel.quakecraft.game.playing.KillStreak;
import xyz.upperlevel.quakecraft.game.playing.PlayingPhase;
import xyz.upperlevel.quakecraft.placeholders.QuakePlaceholders;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.powerup.arguments.PowerupEffectArgumentParser;
import xyz.upperlevel.quakecraft.shop.ShopCategory;
import xyz.upperlevel.quakecraft.shop.purchase.ConfirmPurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.quakecraft.shop.railgun.RailgunSelectGui;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardRegistry;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.database.Store;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.Gui;
import xyz.upperlevel.uppercore.gui.GuiRegistry;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.hotbar.Hotbar;
import xyz.upperlevel.uppercore.hotbar.HotbarRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.placeholder.message.MessageManager;
import xyz.upperlevel.uppercore.registry.Registry;
import xyz.upperlevel.uppercore.update.SpigotUpdateChecker;
import xyz.upperlevel.uppercore.update.UpdateChecker;
import xyz.upperlevel.uppercore.util.CrashUtil;

import java.io.File;
import java.io.IOException;

import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.util.CrashUtil.loadSafe;

@Getter
public class Quakecraft extends JavaPlugin {
    public static final String SPIGOT_ID = "quakecraft.45928";
    //public static final long SPIGET_ID = 45928;
    private static Quakecraft instance;

    // impl
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private QuakePlayerManager playerManager;
    private ShopCategory shop;

    // core
    private Registry<?> registryRoot;
    private Registry<Board> boards;
    private Registry<Gui> guis;
    private Registry<Hotbar> hotbars;
    private Store store;

    private MessageManager messages;

    private Config customConfig;

    private UpdateChecker updater;

    // confirm gui
    private ConfirmPurchaseGui.Options defConfirmOptions;

    @Override
    public void onEnable() {
        instance = this;

        try {
            saveDefaultConfig();
            loadConfig();

            //Load command messages
            registryRoot = Uppercore.registry().register(this);
            guis = registryRoot.registerChild("guis", Gui.class);
            hotbars = registryRoot.registerChild("hotbars", Hotbar.class);
            boards = registryRoot.registerChild("boards", Board.class);
            store = new Store(this);

            arenaManager = new ArenaManager();
            arenaManager.load();
            gameManager = new GameManager();
            gameManager.load();

            shop = new ShopCategory();
            Bukkit.getScheduler().runTask(this, () -> {
                if (EconomyManager.isEnabled()) {
                    shop.load();//Requires Economy
                } else {
                    getLogger().severe("Cannot find any economy plugin installed!");
                    setEnabled(false);
                }
            });

            defConfirmOptions = ConfirmPurchaseGui.load();

            PlaceholderUtil.register(this, new QuakePlaceholders());
            ArgumentParserSystem.register(new ArenaArgumentParser());
            ArgumentParserSystem.register(new GameArgParser());
            ArgumentParserSystem.register(new PowerupEffectArgumentParser());

            new QuakecraftCommand().subscribe();

            GainNotifier.setup();

            playerManager = new QuakePlayerManager();

            updater = new SpigotUpdateChecker(this, SPIGOT_ID);
        } catch (Throwable t) {
            CrashUtil.saveCrash(this, t);
            setEnabled(false);
        }
    }

    public void loadConfig() {
        customConfig = Config.fromYaml(new File(getDataFolder(), "config.yml"));
        messages = MessageManager.load(this);

        loadSafe("commands", QuakecraftCommand::loadConfig);
        loadSafe("killstreak", KillStreak::loadConfig);
        loadSafe("dash", Dash::loadConfig);
        loadSafe("purchase-gui", PurchaseGui::loadConfig);
        loadSafe("railgun", RailgunSelectGui::loadConfig);
        loadSafe("gain", GainType::loadConfig);
        loadSafe("bullet", Bullet::loadConfig);
        loadSafe("railgun", Railgun::loadConfig);
        loadSafe("game", Game::loadConfig);
        //---phase configs---
        loadSafe("waiting phase", WaitingPhase::loadConfig);
        loadSafe("game phase", GamePhase::loadConfig);
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
            if(playerManager != null)
                playerManager.close();
            if(gameManager != null)
                gameManager.save();
            if(arenaManager != null)
                arenaManager.save();

            if(gameManager != null)
                gameManager.stop();
        } catch (IOException e) {
            getLogger().severe("Cannot save game/arena settings: " + e);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static Quakecraft get() {
        return instance;
    }
}
