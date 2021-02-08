package xyz.upperlevel.quakecraft;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.profile.ProfileController;
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
import xyz.upperlevel.uppercore.registry.Registry;
import xyz.upperlevel.uppercore.storage.Storage;
import xyz.upperlevel.uppercore.update.SpigotUpdateChecker;
import xyz.upperlevel.uppercore.update.UpdateChecker;
import xyz.upperlevel.uppercore.util.CrashUtil;
import xyz.upperlevel.uppercore.util.DynLib;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.YELLOW;
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
    private Connection connection;

    private ProfileController profileController;

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

            connectToDatabase();
            profileController = new ProfileController(connection);

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

        new QuakePlaceholderExtension(this).register();

        Bukkit.getConsoleSender().sendMessage(AQUA + "[Quake] Quake has been successfully loaded and it's enabled.");
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
        Gamer.loadConfig();
        CountdownPhase.loadConfig();
        EndingPhase.loadConfig();
        LobbyPhase.loadConfig();

        PowerupEffectManager.load(customConfig.getConfigRequired("powerups"));

        ArenaManager.get().load(QuakeArena.class);
    }

    @EventHandler
    private void notifyUnsetQuake(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        boolean unset = ArenaManager.get().getArenas().isEmpty();
        if (player.isOp() && unset) {
            Bukkit.getScheduler().runTask(this, () ->
                    player.sendMessage(AQUA + "Quake hasn't found any arena.\nConsider following the setup wiki: " + YELLOW + "https://upperlevel.github.io/quake"));
        }
    }

    // ------------------------------------------------------------------------------------------------ Database

    private void connectToDatabase() {
        Config cfg = Config.fromYaml(new File(getDataFolder(), "db.yml"));
        String type = cfg.getStringRequired("type");

        Runnable getDriver = new HashMap<String, Runnable>() {{
            // MariaDB
            put("mariadb", () -> {
                try {
                    DynLib.from("https://downloads.mariadb.com/Connectors/java/connector-java-2.6.2/mariadb-java-client-2.6.2.jar").install();
                    Class.forName("org.mariadb.jdbc.Driver");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        }}.get(type);

        if (getDriver != null)
            getDriver.run();

        try {
            if ("sqlite".equals(type)) {
                File file = new File(getDataFolder(), "quake.db");
                this.connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", file.getPath()));
            } else {
                String user = cfg.getString("user");
                String password = cfg.getString("password");
                if (user != null) user = "user=" + user;
                if (password != null) password = "password=" + password;

                String query = Stream.of(user, password).filter(Objects::nonNull).collect(Collectors.joining("&"));
                String url = "jdbc:%s://%s:%d/%s" + (query.isEmpty() ? "" : "?" + query);
                this.connection = DriverManager.getConnection(String.format(url,
                        type,
                        cfg.getStringRequired("host"),
                        cfg.getIntRequired("port"),
                        cfg.getStringRequired("database")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("Unsupported JDBC DB: '%s'", type), e);
        }
    }

    @EventHandler
    private void tryCreateDbProfile(PlayerJoinEvent e) {
        getProfileController().createProfileAsync(e.getPlayer(), new Profile());
    }

    public void openConfirmPurchase(Player player, Purchase<?> purchase, Link onAccept, Link onDecline) {
        guis().open(player, new ConfirmPurchaseGui(purchase, defConfirmOptions, onAccept, onDecline));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);

        try {
            if (connection != null)
                connection.close();
        } catch (SQLException ignored) {
        }

        ArenaManager.get().unload();

        if (remoteDatabase != null)
            remoteDatabase.close();
    }

    public static Quake get() {
        return instance;
    }

    public static ProfileController getProfileController() {
        return instance.profileController;
    }

    public static Profile getProfile(Player player) {
        return getProfileController().getProfile(player);
    }

    public static Profile getProfile(OfflinePlayer player) {
        return getProfileController().getProfile(player.getUniqueId());
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
