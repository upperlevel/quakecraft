package xyz.upperlevel.quakecraft.game.args;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.argument.ArgumentParser;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.command.argument.exceptions.ParseException;

import java.util.Collections;
import java.util.List;

public class GameArgParser implements ArgumentParser {
    @Override
    public List<Class<?>> getParsable() {
        return Collections.singletonList(Game.class);
    }

    @Override
    public int getArgumentsCount() {
        return 1;
    }

    @Override
    public Object parse(Class<?> clazz, List<String> args) throws ParseException {
        Game g = Quakecraft.get().getGameManager().getGame(args.get(0));
        if (g == null)
            throw new ParseException(args.get(0), "game");
        return g;
    }

    @Override
    public List<String> onTabCompletion(CommandSender sender, Class<?> type, List<String> args) {
        return ArgumentParserSystem.tabComplete(
                Quakecraft.get().getGameManager().getGamesById().keySet(),
                args);
    }
}
