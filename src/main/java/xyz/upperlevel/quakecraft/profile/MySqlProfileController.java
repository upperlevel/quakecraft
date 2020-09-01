package xyz.upperlevel.quakecraft.profile;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MySqlProfileController extends ProfileController {
    private final Connection connection;

    public static final Map<String, String> COLUMNS = new HashMap<String, String>() {{
        put("id", "varchar(16)");
        put("name", "varchar(256)");

        put("kills", "int");
        put("deaths", "int");
        put("won_matches", "int");
        put("played_matches", "int");

        put("selected_barrel", "varchar(1024)");
        put("selected_case", "varchar(1024)");
        put("selected_laser", "varchar(1024)");
        put("selected_muzzle", "varchar(1024)");
        put("selected_trigger", "varchar(1024)");

        put("selected_hat", "varchar(1024)");
        put("selected_chestplate", "varchar(1024)");
        put("selected_leggings", "varchar(1024)");
        put("selected_boots", "varchar(1024)");

        put("selected_kill_sound", "varchar(1024)");

        put("selected_dash_power", "varchar(1024)");
        put("selected_dash_cooldown", "varchar(1024)");

        put("purchases", "json");
    }};

    public MySqlProfileController(MySqlConnection connection) {
        this.connection = connection.getHandle();
        createTable();
    }

    public Profile toProfile(ResultSet result) throws SQLException {
        if (!result.next())
            return null;

        ResultSetMetaData metaData = result.getMetaData();
        int columns = metaData.getColumnCount();

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < columns; i++)
            data.put(metaData.getColumnName(i), result.getObject(i));

        return new Profile(data);
    }

    private void createTable() {
        try {
            Map<String, String> columns = new HashMap<>(COLUMNS);
            columns.remove("id");
            columns.remove("name");

            String fields = columns.entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue()).collect(Collectors.joining(", "));
            String query = String.format("CREATE TABLE `profiles` (id varchar(16) PRIMARY KEY, name varchar(256) UNIQUE, %s)", fields);

            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException ignored) {
            // The table was already created and the query failed.
        }
    }

    @SuppressWarnings("unchecked")
    private void stringifyPurchases(Map<String, Object> profileData) {
        List<String> purchases = (List<String>) profileData.get("purchases");
        if (purchases != null) { // If purchases are found then they are stringified in order to be inserted in MySQL.
            JSONArray json = new JSONArray();
            json.addAll(purchases);
            profileData.put("purchases", json.toJSONString());
        }
    }

    @Override
    public boolean createProfile0(UUID id, String name, Profile profile) {
        try {
            Map<String, Object> data = new HashMap<>(profile.data);
            data.put("id", id.toString());
            data.put("name", name);

            String attribs = COLUMNS.keySet().stream().map(column -> "`" + column + "`").collect(Collectors.joining(", "));
            String values = COLUMNS.keySet().stream().map(column -> "?").collect(Collectors.joining(", "));

            String query = "INSERT INTO `profiles` (" + attribs + ") VALUES (" + values + ")";
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < COLUMNS.size(); i++) {
                statement.setObject(i + 1, data.get(COLUMNS.get(i)));
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException ignored) {
            return false;
        }
    }

    @Override
    public Profile getProfile0(UUID id) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `profiles` WHERE `id`=?");
            statement.setString(1, id.toString());

            return toProfile(statement.executeQuery());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Profile getProfile0(String name) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `profiles` WHERE `name`=?");
            statement.setString(1, name);

            return toProfile(statement.executeQuery());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean updateProfile0(UUID id, Profile profile) {
        try {
            Map<String, Object> data = new HashMap<>(profile.data);
            data.remove("id");
            stringifyPurchases(profile.data);

            String updateQuery = data.keySet().stream().map(key -> key + "=?").collect(Collectors.joining(", "));
            List<Object> values = new ArrayList<>(data.values());

            String query = "UPDATE `profiles` SET " + updateQuery + " WHERE `id`=?";
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 0; i < values.size(); i++)
                statement.setString(i + 1, (String) values.get(i));
            statement.setString(values.size() + 1, id.toString());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean deleteProfile0(UUID id) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM `profiles` WHERE `id`=?");
            statement.setString(1, id.toString());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
