package xyz.upperlevel.quakecraft.arena.arguments;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.argument.ArgumentParser;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.command.argument.exceptions.ParseException;

import java.util.List;

import static java.util.Collections.singletonList;

public class ArenaArgumentParser implements ArgumentParser {

    @Override
    public List<Class<?>> getParsable() {
        return singletonList(Arena.class);
    }

    @Override
    public int getArgumentsCount() {
        return 1;
    }

    @Override
    public Object parse(Class<?> type, List<String> args) throws ParseException {
        Arena arena = Quakecraft.get().getArenaManager().getArena(args.get(0));
        if (arena == null)
            throw new ParseException(args.get(0), "arena");
        return arena;
    }

    @Override
    public List<String> onTabCompletion(CommandSender sender, Class<?> type, List<String> args) {
        return ArgumentParserSystem.tabComplete(Quakecraft.get().getArenaManager().getArenasById().keySet(), args);
    }
}
