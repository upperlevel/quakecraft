package xyz.upperlevel.quakecraft.profile;

import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.Dbg;

import java.util.UUID;

import static org.bukkit.ChatColor.*;

public abstract class ProfileController {
    protected abstract Profile getProfile0(UUID id);

    protected abstract Profile getProfile0(String name);

    protected abstract boolean createProfile0(UUID id, String name, Profile profile);

    protected abstract boolean updateProfile0(UUID id, Profile profile);

    protected abstract boolean deleteProfile0(UUID id);

    public Profile getProfile(UUID id) {
        long t = System.currentTimeMillis();
        Profile profile = getProfile0(id);
        Dbg.pf("Retrieved profile by ID in %d ms", System.currentTimeMillis() - t);

        return profile;
    }

    public Profile getProfile(String name) {
        long t = System.currentTimeMillis();
        Profile profile = getProfile0(name);
        Dbg.pf("Retrieved profile by name in %d ms", System.currentTimeMillis() - t);

        return profile;
    }

    public Profile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public boolean createProfile(UUID id, String name, Profile profile) {
        long t = System.currentTimeMillis();
        boolean result = createProfile0(id, name, profile);
        Dbg.pf("Created profile in %d ms", System.currentTimeMillis() - t);

        return result;
    }

    public boolean createProfile(Player player, Profile profile) {
        return createProfile(player.getUniqueId(), player.getName(), profile);
    }

    public boolean updateProfile(UUID id, Profile profile) {
        long t = System.currentTimeMillis();
        boolean result = updateProfile0(id, profile);
        Dbg.pf("Updated profile in %d ms", System.currentTimeMillis() - t);

        return result;
    }

    public boolean deleteProfile(UUID id) {
        long t = System.currentTimeMillis();
        boolean result = deleteProfile0(id);
        Dbg.pf("Deleting profile in %d ms", System.currentTimeMillis() - t);

        return result;
    }

    public void registerPlaceholders(PlaceholderRegistry<?> placeholders) {
        placeholders
                .set("quake_player_kills", player -> String.valueOf(getProfile(player).getKills()))
                .set("quake_player_deaths", player -> String.valueOf(getProfile(player).getDeaths()))
                .set("quake_player_played_matches", player -> String.valueOf(getProfile(player).getPlayedMatches()))
                .set("quake_player_won_matches", player -> String.valueOf(getProfile(player).getWonMatches()));
    }
}
