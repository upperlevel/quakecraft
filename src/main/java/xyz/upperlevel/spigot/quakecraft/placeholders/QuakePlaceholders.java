package xyz.upperlevel.spigot.quakecraft.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.core.Phase;
import xyz.upperlevel.spigot.quakecraft.game.CountdownPhase;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.spigot.quakecraft.game.LobbyPhase;
import xyz.upperlevel.spigot.quakecraft.game.WaitingPhase;
import xyz.upperlevel.spigot.quakecraft.shop.gun.CaseManager;
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
        // plugin
        PluginDescriptionFile desc = get().getDescription();
        switch (id) {
            case "name":
                return desc.getName();
            case "version":
                return desc.getVersion();
            case "description":
                return desc.getDescription();
        }
        // game
        Game game = get().getGameManager().getGame(player);
        if (game != null) {
            switch (id) {
                case "game_id":
                    return game.getId();
                case "game_name":
                    return game.getName();
                case "game_players":
                    return valueOf(game.getPlayers().size());
                case "game_min_players":
                    return valueOf(game.getMinPlayers());
                case "game_max_players":
                    return valueOf(game.getMaxPlayers());
            }
        }
        // phases
        if (game != null) {
            Phase phase = game.getPhaseManager().getPhase();
            // lobby phase
            if (phase instanceof LobbyPhase) {
                // countdown phase
                if (((LobbyPhase) phase).getPhase() instanceof CountdownPhase) {
                    CountdownPhase countdown = (CountdownPhase) ((LobbyPhase) phase).getPhase();
                    switch (id) {
                        case "lobby_countdown":
                            return valueOf(countdown.getTimer());
                    }
                }
            }
        }
        return null;
    }
}
