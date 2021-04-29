package xyz.upperlevel.quakecraft.profile.util;

import lombok.Getter;
import lombok.NonNull;
import xyz.upperlevel.quakecraft.DbConnectionPool;
import xyz.upperlevel.uppercore.util.Dbg;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SqlTableHelper<T extends Map<String, Object>> {
    public static class WhereClause {
        private final String sql;
        private final List<Object> params;

        private WhereClause(String sql, Object... params) {
            this.sql = sql;
            this.params = Arrays.asList(params);
        }

        public static WhereClause of(String sql, Object... params) {
            return new WhereClause(sql, params);
        }
    }

    public static class OrderByClause {
        private final String field;
        private final String order; // ASC or DESC

        private OrderByClause(String field, String order) {
            this.field = field;
            this.order = order;
        }

        public static OrderByClause of(String field, String order) {
            return new OrderByClause(field, order);
        }
    }

    @Getter
    private final DbConnectionPool pool;

    @Getter
    protected final String name;

    public SqlTableHelper(DbConnectionPool pool, String name) {
        this.pool = pool;
        this.name = name;
    }

    public void create(String... schema) throws SQLException {
        String sql = String.format("CREATE TABLE `%s` (%s)", name, String.join(", ", schema));

        //Dbg.pf("Create table query: %s", sql);

        try (Connection conn = this.pool.getConnection()) {
            int res = conn.prepareStatement(sql).executeUpdate();
            Dbg.pf("Table `%s` creation result: %d", name, res);
        }
    }

    protected List<Map<String, Object>> gatherResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columns = metaData.getColumnCount();

        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                Object object = resultSet.getObject(i);
                if (object != null) // If the column is null, doesn't even add it to the map.
                    row.put(metaData.getColumnName(i), object);
            }
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> select(@NonNull WhereClause where, @NonNull String projection, Integer limit, OrderByClause orderBy) throws SQLException {
        String sql = String.format("SELECT %s FROM `%s` WHERE %s", projection, name, where.sql);
        if (limit != null) sql += " LIMIT " + limit;
        if (orderBy != null) sql += String.format(" ORDER BY `%s` %s", orderBy.field, orderBy.order);

        List<Object> params = where.params;

        //Dbg.pf("Select query: %s (%s)", sql, params.stream().map(Object::toString).collect(Collectors.toList()));

        try (Connection conn = this.pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++)
                statement.setObject(i + 1, params.get(i));
            ResultSet resultSet = statement.executeQuery();
            return gatherResultSet(resultSet);
        }
    }

    public boolean insert(T data) throws SQLException {
        String sql = String.format("INSERT INTO `%s` (%s) VALUES (%s)", name,
                String.join(", ", data.keySet()),
                String.join(", ", Collections.nCopies(data.size(), "?")));

        List<Object> params = new ArrayList<>(data.values());

        //Dbg.pf("Insert query: %s (%s)", sql, params.stream().map(Object::toString).collect(Collectors.toList()));

        try (Connection conn = this.pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(WhereClause where, T data) throws SQLException {
        String fields = data.entrySet().stream()
                .map(entry -> String.format("`%s`=?", entry.getKey()))
                .collect(Collectors.joining(", "));
        String sql = String.format("UPDATE `%s` SET %s WHERE %s", name, fields, where.sql);

        List<Object> params = new ArrayList<>(data.values());
        params.addAll(where.params);

        //Dbg.pf("Update query: %s (%s)", sql, params.stream().map(Object::toString).collect(Collectors.toList()));

        try (Connection conn = this.pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(WhereClause where) throws SQLException {
        String sql = String.format("DELETE FROM `%s` WHERE %s", name, where.sql);

        List<Object> params = where.params;

        //Dbg.pf("Delete query: %s (%s)", sql, params.stream().map(Object::toString).collect(Collectors.toList()));

        try (Connection conn = this.pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++)
                statement.setObject(i + 1, params.get(i));
            return statement.executeUpdate() > 0;
        }
    }

    public void drop() throws SQLException {
        String sql = String.format("DROP TABLE `%s`", name);

        //Dbg.pf("Drop query: %s", sql);

        try (Connection conn = this.pool.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.executeUpdate();
        }
    }
}
