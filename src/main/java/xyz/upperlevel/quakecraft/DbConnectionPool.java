package xyz.upperlevel.quakecraft;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.util.Dbg;
import xyz.upperlevel.uppercore.util.DynLib;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.function.Consumer;

public class DbConnectionPool {
    private HikariDataSource dataSource;

    private void init(Plugin plugin) {
        Config cfg = Config.fromYaml(new File(plugin.getDataFolder(), "db.yml"));
        String type = cfg.getStringRequired("type");

        Uppercore.logger().info("Initializing DB connection pool for: " + type);

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

        if (getDriver != null) {
            getDriver.run();
        }


        HikariConfig config = new HikariConfig();
        config.setPoolName("QuakeDbConnectionPool");

        /*
        config.setDataSourceClassName(new HashMap<String, String>() {{
            put("sqlite", "org.sqlite.SQLiteDataSource");
            put("mysql", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            put("mariadb", "org.mariadb.jdbc.MariaDbDataSource");
        }}.get(type));
*/

        if ("sqlite".equals(type)) {
            File file = new File(plugin.getDataFolder(), "quake.db");
            config.setJdbcUrl(String.format("jdbc:sqlite:%s", file.getPath()));
        } else {
            config.setJdbcUrl(String.format("jdbc:%s://%s:%d/%s",
                    type,
                    cfg.getStringRequired("host"),
                    cfg.getIntRequired("port"),
                    cfg.getStringRequired("database")
            ));
        }

        if (cfg.has("user")) config.setUsername(cfg.getString("user"));
        if (cfg.has("password")) config.setPassword(cfg.getString("password"));

        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000); // 60 Sec
        config.setIdleTimeout(45000); // 45 Sec

        dataSource = new HikariDataSource(config);
    }

    public DbConnectionPool(Plugin plugin) {
        init(plugin);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
