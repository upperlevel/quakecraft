package xyz.upperlevel.spigot.quakecraft.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.game.CountdownPhase;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.LobbyPhase;
import xyz.upperlevel.spigot.quakecraft.game.WaitingPhase;
import xyz.upperlevel.uppercore.placeholder.Placeholder;

import java.util.Locale;

import static java.lang.String.valueOf;
import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

public class QuakePlaceholders implements Placeholder {

    @Override
    public String getId() {
        return "quake";
    }

    @Override
    public String resolve(Player player, String id) {
        // ------------------------quake
        PluginDescriptionFile desc = get().getDescription();
        if (id.equals("name"))
            return desc.getName();
        if (id.equals("version"))
            return desc.getVersion();
        if (id.equals("description"))
            return desc.getDescription();
        // ------------------------game
        Game game = get().getGameManager().getGame(player);
        if (game != null) {
            if (id.equals("game_min_players"))
                return valueOf(game.getMinPlayers());
            if (id.equals("game_max_players"))
                return valueOf(game.getMaxPlayers());
            if (id.equals("game_id"))
                return valueOf(game.getId());
            if (id.equals("game_name"))
                return valueOf(game.getName());
            if (id.equals("game_players"))
                return valueOf(game.getPlayers().size());
            if (id.equals("game_winner"))
                return game.getWinner() != null ? game.getWinner().getName() : null;
            // ------------------------phases
            Phase phase = game.getPhaseManager().getPhase();
            // ------------------------lobby phase
            if (phase instanceof LobbyPhase) {
                switch (id) {
                    case "lobby_countdown":
                        phase = ((LobbyPhase) phase).getPhase();
                        return phase instanceof CountdownPhase ? valueOf(((CountdownPhase) phase).getTimer()) : null;
                }
            }
        }

        return null;
    }
}
