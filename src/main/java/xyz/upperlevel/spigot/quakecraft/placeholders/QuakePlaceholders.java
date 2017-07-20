package xyz.upperlevel.spigot.quakecraft.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.game.CountdownPhase;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.LobbyPhase;
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
        // ------------------------quake
        PluginDescriptionFile desc = get().getDescription();
        if (subId.equals("name"))
            return desc.getName();
        if (subId.equals("version"))
            return desc.getVersion();
        if (subId.equals("description"))
            return desc.getDescription();
        // ------------------------game
        Game game = get().getGameManager().getGame(player);
        if (game != null) {
            if (subId.equals("game_min_players"))
                return valueOf(game.getMinPlayers());
            if (subId.equals("game_max_players"))
                return valueOf(game.getMaxPlayers());
            if (subId.equals("game_id"))
                return valueOf(game.getId());
            if (subId.equals("game_name"))
                return valueOf(game.getName());
            if (subId.equals("game_players"))
                return valueOf(game.getPlayers().size());
            if (subId.equals("game_winner"))
                return game.getWinner() != null ? game.getWinner().getName() : null;
            // ------------------------phases
            Phase phase = game.getPhaseManager().getPhase();
            // ------------------------countdown phase
            if (subId.equals("lobby_countdown"))
                return phase instanceof CountdownPhase ? valueOf(((CountdownPhase) phase).getTimer()) : null;
        }

        return null;
    }
}
