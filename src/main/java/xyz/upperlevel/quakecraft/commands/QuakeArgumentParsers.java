package xyz.upperlevel.quakecraft.commands;

import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.functional.parser.ArgumentParseException;
import xyz.upperlevel.uppercore.command.functional.parser.AsArgumentParser;

import java.util.List;

public final class QuakeArgumentParsers {
    /* QuakeArena */
    @AsArgumentParser(
            parsableTypes = {QuakeArena.class},
            consumeCount = 1
    )
    public QuakeArena parseArena(List<String> args) throws ArgumentParseException {
        QuakeArena arena = (QuakeArena) Quake.get().getArenaManager().get(args.get(0));
        if (arena == null) {
            throw new ArgumentParseException(QuakeArena.class, args);
        }
        return arena;
    }

    /* PowerupEffect */
    @AsArgumentParser(
            parsableTypes = {PowerupEffect.class},
            consumeCount = 1
    )
    public PowerupEffect parsePowerupEffect(List<String> args) throws ArgumentParseException {
        PowerupEffect effect = PowerupEffectManager.fromId(args.get(0));
        if (effect == null)
            throw new ArgumentParseException(PowerupEffect.class, args);
        return effect;
    }

    public QuakeArgumentParsers() {
    }
}
