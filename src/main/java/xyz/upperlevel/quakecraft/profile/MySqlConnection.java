package xyz.upperlevel.quakecraft.profile;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlConnection implements DbConnection {
    @Getter
    private final Connection handle;

    @Getter
    private final MySqlProfileController profileController;

    public MySqlConnection(Connection handle) {
        this.handle = handle;

        this.profileController = new MySqlProfileController(this);
    }

    @Override
    public void close() {
        try {
            this.handle.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MySqlConnection create(String host, int port, String database, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        String url = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, database, username, password);
        try {
            Connection connection = DriverManager.getConnection(url);
            return new MySqlConnection(connection);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
