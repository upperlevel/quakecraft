package xyz.upperlevel.quakecraft.profile;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class ProfileController {
    public abstract Profile getProfile(UUID id);

    public abstract Profile getProfile(String name);

    public Profile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public abstract boolean createProfile(UUID id, String name, Profile profile);

    public boolean createProfile(Player player, Profile profile) {
        return createProfile(player.getUniqueId(), player.getName(), profile);
    }

    public abstract boolean updateProfile(UUID id, Profile profile);

    public abstract boolean deleteProfile(UUID id);

    // TODO caching?
}
