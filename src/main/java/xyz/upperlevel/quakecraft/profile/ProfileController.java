package xyz.upperlevel.quakecraft.profile;

import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ProfileController {
    // ---------------------------------------------------------------- DB

    protected abstract Profile getProfile(UUID id);

    protected abstract Profile getProfile(String name);

    protected Profile getProfile(Player player) {
        return getProfileCached(player.getUniqueId());
    }

    protected abstract boolean createProfile(UUID id, String name, Profile profile);

    protected boolean createProfile(Player player, Profile profile) {
        return createProfileCached(player.getUniqueId(), player.getName(), profile);
    }

    protected abstract boolean updateProfile(UUID id, Profile profile);

    protected void updateProfiles(Map<UUID, Profile> profiles) {
        for (Map.Entry<UUID, Profile> entry : profiles.entrySet()) {
            UUID id = entry.getKey();
            Profile profile = entry.getValue();

            boolean result = false;
            if (!result) result = updateProfile(id, profile);
            if (!result) result = createProfile(id, profile.getName(), profile);

            if (!result) {
                Uppercore.logger().warning(
                        String.format("The following profile couldn't be updated neither inserted: %s", new Yaml().dump(profile))
                );
            }
        }
    }

    protected abstract boolean deleteProfile(UUID id);

    // ---------------------------------------------------------------- Caching

    private final Map<UUID, Profile> byId = new HashMap<>();
    private final Map<String, Profile> byName = new HashMap<>();

    public Profile getProfileCached(UUID id) {
        Profile profile = byId.get(id);
        if (profile == null && (profile = getProfile(id)) != null) {
            byId.put(id, profile);
            byName.put(profile.getName(), profile);
        }
        return profile;
    }

    public Profile getProfileCached(String name) {
        Profile profile = byName.get(name);
        if (profile == null && (profile = getProfile(name)) != null) {
            byId.put(profile.getId(), profile);
            byName.put(name, profile);
        }
        return profile;
    }

    public Profile getProfileCached(Player player) {
        return getProfileCached(player.getUniqueId());
    }

    public boolean createProfileCached(UUID id, String name, Profile profile) {
        profile.setId(id);
        profile.setName(name);
        if (!byId.containsKey(id)) {
            byId.put(id, profile);
            byName.put(name, profile);
            return createProfile(id, name, profile);
        }
        return false;
    }

    public boolean createProfileCached(Player player, Profile profile) {
        return createProfileCached(player.getUniqueId(), player.getName(), profile);
    }

    public boolean deleteProfileCached(UUID id) {
        Profile profile = byId.remove(id);
        if (profile != null)
            byName.remove(profile.getName());
        return deleteProfile(id);
    }

    public void flushCache() {
        updateProfiles(byId);
    }

    // ----------------------------------------------------------------

    public void registerPlaceholders(PlaceholderRegistry<?> placeholders) {
        placeholders
                .set("quake_player_kills", player -> String.valueOf(getProfileCached(player).getKills()))
                .set("quake_player_deaths", player -> String.valueOf(getProfileCached(player).getDeaths()))
                .set("quake_player_played_matches", player -> String.valueOf(getProfileCached(player).getPlayedMatches()))
                .set("quake_player_won_matches", player -> String.valueOf(getProfileCached(player).getWonMatches()));
    }
}
