package xyz.upperlevel.quakecraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.commands.DebugCommand;
import xyz.upperlevel.quakecraft.commands.QuakeArgumentParsers;
import xyz.upperlevel.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.quakecraft.phases.game.*;
import xyz.upperlevel.quakecraft.phases.lobby.CountdownPhase;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.quakecraft.phases.lobby.WaitingPhase;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.shop.ShopCategory;
import xyz.upperlevel.quakecraft.shop.purchase.ConfirmPurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.quakecraft.shop.railgun.RailgunSelectGui;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.arena.ArenaManager;
import xyz.upperlevel.uppercore.command.CommandRegistry;
import xyz.upperlevel.uppercore.command.functional.parser.ArgumentParserManager;
import xyz.upperlevel.uppercore.command.functional.parser.FunctionalArgumentParser;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.registry.Registry;
import xyz.upperlevel.uppercore.storage.Storage;
import xyz.upperlevel.uppercore.storage.StorageConnector;
import xyz.upperlevel.uppercore.update.SpigotUpdateChecker;
import xyz.upperlevel.uppercore.update.UpdateChecker;
import xyz.upperlevel.uppercore.util.CrashUtil;

import java.io.File;

import static xyz.upperlevel.uppercore.Uppercore.guis;

@Getter
public class Quake extends JavaPlugin {
    public static final String SPIGOT_ID = "quakecraft.45928";
    //public static final long SPIGET_ID = 45928;
    public static final int BSTATS_ID = 7706;
    private static Quake instance;

    private AccountManager playerManager;
    private ShopCategory shop;

    private Registry pluginRegistry;
    private Registry guis;

    private Storage remoteDatabase;

    private Config customConfig;

    private UpdateChecker updater;

    // confirm gui
    private ConfirmPurchaseGui.Options defConfirmOptions;

    @Override
    public void onEnable() {
        instance = this;

        // This will initialize Uppercore.
        Uppercore.hook(this, BSTATS_ID);

        try {
            // Quake configuration should be present in the plugin folder.
            // The plugin will not take care of saving its JAR included resources.
            loadConfig();

            this.pluginRegistry = Uppercore.registry();
            this.guis = pluginRegistry.registerFolder("guis");

            this.remoteDatabase = StorageConnector.read(this);
            QuakeAccount.loadTable();

            shop = new ShopCategory();
            Bukkit.getScheduler().runTask(this, () -> {
                if (EconomyManager.isEnabled()) {
                    shop.load();//Requires Economy
                } else {
                    getLogger().severe("Cannot find any economy plugin installed!");
                    setEnabled(false);
                }
            });

            defConfirmOptions = ConfirmPurchaseGui.load(customConfig);

            registerCommands();

            playerManager = new AccountManager();

            updater = new SpigotUpdateChecker(this, SPIGOT_ID);
        } catch (Throwable t) {
            CrashUtil.saveCrash(this, t);
            setEnabled(false);
        }
    }

    private void registerCommands() {
        ArgumentParserManager.register(FunctionalArgumentParser.load(new QuakeArgumentParsers()));
        CommandRegistry.register(new QuakeCommand());
        CommandRegistry.register(new DebugCommand());
    }

    public void loadConfig() {
        File folder = getDataFolder();

        saveResource("config.yml", false);
        customConfig = Config.fromYaml(new File(folder, "config.yml"));

        QuakeCommand.loadConfig();
        KillStreak.loadConfig();
        Dash.loadConfig();
        PurchaseGui.loadConfig();
        RailgunSelectGui.loadConfig();
        GainType.loadConfig();
        Railgun.loadConfig();
        GainNotifier.loadConfig();
        Shot.loadConfig();

        WaitingPhase.loadConfig();
        GamePhase.loadConfig();
        CountdownPhase.loadConfig();
        EndingPhase.loadConfig();
        LobbyPhase.loadConfig();

        PowerupEffectManager.load(customConfig.getConfigRequired("powerups"));

        ArenaManager.get().load(QuakeArena.class);
    }

    public void openConfirmPurchase(Player player, Purchase<?> purchase, Link onAccept, Link onDecline) {
        guis().open(player, new ConfirmPurchaseGui(purchase, defConfirmOptions, onAccept, onDecline));
    }

    @Override
    public void onDisable() {
        if (playerManager != null)
            playerManager.close();

        ArenaManager.get().unload();
    }

    public static Quake get() {
        return instance;
    }

    public static QuakeAccount getAccount(Player player) {
        return instance.playerManager.getAccount(player);
    }

    public static QuakeAccount getAccount(Gamer gamer) {
        return getAccount(gamer.getPlayer());
    }

    public static Gamer getGamer(Player player) {
        QuakeArena a = getArena(player);
        if (a != null && a.getPhaseManager().getPhase() instanceof GamePhase) {
            return ((GamePhase) a.getPhaseManager().getPhase()).getGamer(player);
        }
        return null;
    }

    public ArenaManager getArenaManager() {
        return ArenaManager.get(); // TODO ArenaManager is a singleton accessible through ArenaManager.get()
    }

    public static QuakeArena getArena(Player player) {
        return (QuakeArena) ArenaManager.get().get(player);
    }

    public static Config getConfigSection(String path) {
        return instance.customConfig.getConfigRequired(path);
    }
}
