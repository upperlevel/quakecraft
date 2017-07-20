package xyz.upperlevel.spigot.quakecraft.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.placeholder.Placeholder;

import static java.lang.String.valueOf;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

public class QuakePlaceholders implements Placeholder {

    @Override
    public String getId() {
        return "quake";
    }

    @Override
    public String resolve(Player player, String subId) {
        // ------------------------ quake
        PluginDescriptionFile desc = get().getDescription();
        if (subId.equals("name"))
            return desc.getName();
        if (subId.equals("version"))
            return desc.getVersion();
        if (subId.equals("description"))
            return desc.getDescription();
        // ------------------------ game
        Game game = get().getGameManager().getGame(player);
        if (subId.equals("game_min_players"))
            return game != null ? valueOf(game.getMinPlayers()) : null;
        if (subId.equals("game_max_players"))
            return game != null ? valueOf(game.getMaxPlayers()) : null;
        if (subId.equals("game_name"))
            return game != null ? valueOf(game.getName()) : null;
        if (subId.equals("game_displayname"))
            return game != null ? valueOf(game.getDisplayName()) : null;
        if (subId.equals("game_players"))
            return game != null ? valueOf(game.getPlayers().size()) : null;
        if (subId.equals("game_winner"))
            return game != null && game.getWinner() != null ? game.getWinner().getName() : null;
        // ------------------------
        return null;
    }
}
