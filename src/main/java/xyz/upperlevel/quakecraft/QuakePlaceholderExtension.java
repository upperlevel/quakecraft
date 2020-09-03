package xyz.upperlevel.quakecraft;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
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
        switch (identifier) {
            case "player_kills":
                return String.valueOf(Quake.getProfile(player).getKills());

            case "player_deaths":
                return String.valueOf(Quake.getProfile(player).getDeaths());

            case "player_won_matches":
                return String.valueOf(Quake.getProfile(player).getWonMatches());

            case "player_played_matches":
                return String.valueOf(Quake.getProfile(player).getPlayedMatches());

            default:
                return null;
        }
    }
}
