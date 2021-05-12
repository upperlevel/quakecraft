package xyz.upperlevel.quakecraft;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.uppercore.util.Dbg;

public class QuakePlaceholderExtension extends PlaceholderExpansion {
    private final Quake quake;

    public QuakePlaceholderExtension(Quake quake) {
        this.quake = quake;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return quake.getDescription().getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return quake.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return quake.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        Profile profile = Quake.getProfileController().getProfile(player.getUniqueId());

        // The profile could be `null` whether the requested player isn't an actual player (could happen).
        // Then, in this cases, we just return a default value.

        switch (identifier) {
            case "player_kills":
                return profile != null ? String.valueOf(profile.getKills()) : "0";

            case "player_deaths":
                return profile != null ? String.valueOf(profile.getDeaths()) : "0";

            case "player_won_matches":
                return profile != null ? String.valueOf(profile.getWonMatches()) : "0";

            case "player_played_matches":
                return profile != null ? String.valueOf(profile.getPlayedMatches()) : "0";

            case "player_kd_ratio":
                return profile != null ? String.format("%.2f", profile.getKdRatio()) : "0";

            case "player_win_ratio":
                return profile != null ? String.format("%.2f", profile.getWinRatio()) : "0";

            default:
                return null;
        }
    }
}
