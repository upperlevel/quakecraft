package xyz.upperlevel.spigot.quakecraft.arena.arguments;

import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.argument.ArgumentParser;
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
        Arena arena = QuakeCraftReloaded.get().getArenaManager().getArena(args.get(0));
        if (arena == null)
            throw new ParseException(args.get(0), "arena");
        return arena;
    }
}
