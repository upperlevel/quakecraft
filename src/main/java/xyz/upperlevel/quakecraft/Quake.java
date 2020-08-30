package xyz.upperlevel.quakecraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.commands.DebugCommand;
import xyz.upperlevel.quakecraft.commands.QuakeCommand;
import xyz.upperlevel.quakecraft.commands.QuakeParameterHandler;
import xyz.upperlevel.quakecraft.phases.game.*;
import xyz.upperlevel.quakecraft.phases.lobby.CountdownPhase;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.quakecraft.phases.lobby.WaitingPhase;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.profile.*;
import xyz.upperlevel.quakecraft.shop.ShopCategory;
import xyz.upperlevel.quakecraft.shop.purchase.ConfirmPurchaseGui;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseGui;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.quakecraft.shop.railgun.RailgunSelectGui;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.arena.ArenaManager;
import xyz.upperlevel.uppercore.command.CommandRegistry;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.registry.Registry;
import xyz.upperlevel.uppercore.storage.Storage;
import xyz.upperlevel.uppercore.update.SpigotUpdateChecker;
import xyz.upperlevel.uppercore.update.UpdateChecker;
import xyz.upperlevel.uppercore.util.CrashUtil;
import xyz.upperlevel.uppercore.util.DynLib;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static xyz.upperlevel.uppercore.Uppercore.guis;

@Getter
public class Quake extends JavaPlugin implements Listener {
    public static final String SPIGOT_ID = "quakecraft.45928";
    //public static final long SPIGET_ID = 45928;
    public static final int BSTATS_ID = 7706;
    private static Quake instance;

    private ShopCategory shop;

    private Registry pluginRegistry;
    private Registry guis;

    private Storage remoteDatabase;

    private Config customConfig;

    private UpdateChecker updater;

    // confirm gui
    private ConfirmPurchaseGui.Options defConfirmOptions;

    @Getter
    private DbConnection connection;

    @Getter
    private PlaceholderRegistry<?> placeholders;

    @Override
    public void onEnable() {
        instance = this;

        // This will initialize Uppercore.
        Uppercore.hook(this, BSTATS_ID);

        // *** Critical part ***
        // This part is susceptible to errors that may disable the plugin.
        try {
            // Quake configuration should be present in the plugin folder.
            // The plugin will not take care of saving its JAR included resources.
            loadConfig();

            this.pluginRegistry = Uppercore.registry();
            this.guis = pluginRegistry.registerFolder("guis");

            loadDb();
            getProfileController().registerPlaceholders(placeholders);

            shop = new ShopCategory();
            Bukkit.getScheduler().runTask(this, () -> {
                if (EconomyManager.isEnabled()) {
                    shop.load();
                } else {
                    throw new IllegalStateException("Can't find economy plugin installed");
                }
            });

            defConfirmOptions = ConfirmPurchaseGui.load(customConfig);
            updater = new SpigotUpdateChecker(this, SPIGOT_ID);

        } catch (Throwable t) {
            CrashUtil.saveCrash(this, t);
            setEnabled(false);
            return;
        }

        // *** Safe part ***
        // If all went well, the plugin will finally reach this point.
        registerCommands();
        Quake.get().getLogger().info("The plugin has been fully loaded.");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void registerCommands() {
        QuakeParameterHandler.register();

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

    // ------------------------------------------------------------------------------------------------ Database

    private void setupDbDrivers(String type) {
        // TODO put this part of code in Uppercore if possible?
        Map<String, List<Predicate<File>>> byName = new HashMap<String, List<Predicate<File>>>() {{
            put("nitritedb", Arrays.asList(
                    DynLib.reqAndCheck("https://oss.sonatype.org/content/repositories/releases/org/dizitart/nitrite/3.4.1/nitrite-3.4.1.jar", "org.dizitart.no2.Nitrite"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/com/h2database/h2-mvstore/1.4.200/h2-mvstore-1.4.200.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/org/objenesis/objenesis/2.6/objenesis-2.6.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/com/fasterxml/jackson/core/jackson-databind/2.10.1/jackson-databind-2.10.1.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/com/fasterxml/jackson/core/jackson-annotations/2.10.1/jackson-annotations-2.10.1.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/com/squareup/okhttp3/okhttp/4.3.1/okhttp-4.3.1.jar"),
                    DynLib.req("https://oss.sonatype.org/content/repositories/releases/uk/co/jemos/podam/podam/7.2.3.RELEASE/podam-7.2.3.RELEASE.jar")
            ));
            put("mongodb", Collections.singletonList(
                    DynLib.reqAndCheck("https://oss.sonatype.org/content/repositories/releases/org/mongodb/mongo-java-driver/3.12.4/mongo-java-driver-3.12.4.jar", "com.mongodb.MongoClient")
            ));
            put("mysql", Collections.singletonList(
                    DynLib.check("org.mysql.jdbc.Driver")
            ));
        }};
        File folder = new File(getDataFolder(), "dyn_libs");
        folder.mkdirs();
        for (Predicate<File> lib : byName.get(type))
            lib.test(folder);
    }

    private void loadDb() {
        Map<String, Function<Config, DbConnection>> byName = new HashMap<String, Function<Config, DbConnection>>() {{
            put("nitritedb", cfg -> NitriteDbConnection.create(
                    new File(getDataFolder(), "quake.db")
            ));
            put("mongodb", cfg -> MongoDbConnection.create(
                    cfg.getStringRequired("host"),
                    cfg.getIntRequired("port"),
                    cfg.getString("database"),
                    cfg.getString("username"),
                    cfg.getString("password")
            ));
            put("mysql", cfg -> MySqlConnection.create(
                    cfg.getStringRequired("host"),
                    cfg.getIntRequired("port"),
                    cfg.getStringRequired("database"),
                    cfg.getStringRequired("username"),
                    cfg.getStringRequired("password")
            ));
        }};
        Config cfg = Config.fromYaml(new File(getDataFolder(), "db.yml"));

        String type = cfg.getStringRequired("type");
        if (!byName.containsKey(type))
            throw new IllegalArgumentException("Invalid db type: " + type);

        Uppercore.logger().info(String.format("DB found: %s", type));
        setupDbDrivers(type);

        Function<Config, DbConnection> connector = byName.get(type);
        this.connection = connector.apply(cfg);
    }

    @EventHandler
    private void tryCreateDbProfile(PlayerJoinEvent e) {
        getProfileController().createProfileCached(e.getPlayer(), new Profile());
    }

    public void openConfirmPurchase(Player player, Purchase<?> purchase, Link onAccept, Link onDecline) {
        guis().open(player, new ConfirmPurchaseGui(purchase, defConfirmOptions, onAccept, onDecline));
    }

    @Override
    public void onDisable() {
        getProfileController().flushCache();

        HandlerList.unregisterAll((Listener) this);

        if (connection != null) connection.close();

        ArenaManager.get().unload();

        if (remoteDatabase != null)
            remoteDatabase.close();
    }

    public static Quake get() {
        return instance;
    }

    public static ProfileController getProfileController() {
        return instance.getConnection().getProfileController();
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
