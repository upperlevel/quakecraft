package xyz.upperlevel.quakecraft.profile;

import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.util.SqlTableHelper;
import xyz.upperlevel.quakecraft.profile.util.SqlTableHelper.WhereClause;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.sql.*;
import java.util.*;

import static org.bukkit.Bukkit.getScheduler;

public class ProfileController {
    private final Connection connection;
    private final SqlTableHelper<Profile> table;

    public ProfileController(Connection connection) {
        this.connection = connection;

        this.table = new SqlTableHelper<>(connection, "profiles");
        trySetup();
    }

    private void trySetup() {
        try {
            this.table.create(
                    "id varchar(32) PRIMARY KEY",
                    "name varchar(256) NOT NULL UNIQUE",

                    "kills int",
                    "deaths int",
                    "won_matches int",
                    "played_matches int",

                    "selected_barrel varchar(1024)",
                    "selected_case varchar(1024)",
                    "selected_laser varchar(1024)",
                    "selected_muzzle varchar(1024)",
                    "selected_trigger varchar(1024)",

                    "selected_hat varchar(1024)",
                    "selected_chestplate varchar(1024)",
                    "selected_leggings varchar(1024)",
                    "selected_boots varchar(1024)",

                    "selected_kill_sound varchar(1024)",

                    "selected_dash_power varchar(1024)",
                    "selected_dash_cooldown varchar(1024)",

                    "purchases json"
            );
        } catch (SQLException ignored) {
            // An exception could be thrown if the table already exists, ignores it.
        }
    }

    public Profile getProfile(UUID id) {
        try {
            List<Map<String, Object>> result = table.select(WhereClause.of("id=?", id.toString()), "*", null, null);
            if (result.isEmpty())
                return null;
            return new Profile(result.get(0)); // The data is wrapped by Profile that permits to cast every field.
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Profile getProfile(String name) {
        try {
            List<Map<String, Object>> result = table.select(WhereClause.of("name=?", name), "*", null, null);
            if (result.isEmpty())
                return null;
            return new Profile(result.get(0));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Profile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public void createProfileAsync(UUID id, String name, Profile profile) {
        getScheduler().runTaskAsynchronously(Quake.get(), () -> {
            try {
                profile.put("id", id.toString());
                profile.put("name", name);

                table.insert(profile);
            } catch (SQLException ignored) {
            }
        });
    }

    public void createProfileAsync(Player player, Profile profile) {
        createProfileAsync(player.getUniqueId(), player.getName(), profile);
    }

    public void updateProfile(UUID id, Profile profile) {
        try {
            table.update(WhereClause.of("id=?", id.toString()), profile);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void updateProfile(String name, Profile profile) {
        try {
            table.update(WhereClause.of("name=?", name), profile);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteProfileAsync(UUID id) {
        getScheduler().runTaskAsynchronously(Quake.get(), () -> {
            try {
                table.delete(WhereClause.of("id=?", id.toString()));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public void deleteProfileAsync(String name) {
        getScheduler().runTaskAsynchronously(Quake.get(), () -> {
            try {
                table.delete(WhereClause.of("name=?", name));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public void registerPlaceholders(PlaceholderRegistry<?> placeholders) {
        placeholders
                .set("quake_player_kills", player -> String.valueOf(getProfile(player).getKills()))
                .set("quake_player_deaths", player -> String.valueOf(getProfile(player).getDeaths()))
                .set("quake_player_played_matches", player -> String.valueOf(getProfile(player).getPlayedMatches()))
                .set("quake_player_won_matches", player -> String.valueOf(getProfile(player).getWonMatches()));
    }
}
