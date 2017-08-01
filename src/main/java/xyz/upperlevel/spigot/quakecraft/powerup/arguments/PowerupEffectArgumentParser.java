package xyz.upperlevel.spigot.quakecraft.powerup.arguments;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.spigot.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.argument.ArgumentParser;
import xyz.upperlevel.uppercore.command.argument.ArgumentParserSystem;
import xyz.upperlevel.uppercore.command.argument.exceptions.ParseException;

import java.util.Collections;
import java.util.List;

public class PowerupEffectArgumentParser implements ArgumentParser {
    @Override
    public List<Class<?>> getParsable() {
        return Collections.singletonList(PowerupEffect.class);
    }

    @Override
    public int getArgumentsCount() {
        return 1;
    }

    @Override
    public Object parse(Class<?> aClass, List<String> args) throws ParseException {
        PowerupEffect effect = PowerupEffectManager.fromId(args.get(0));
        if (effect == null)
            throw new ParseException(args.get(0), "powerup effect");
        return effect;
    }

    @Override
    public List<String> onTabCompletion(CommandSender sender, Class<?> type, List<String> args) {
        return ArgumentParserSystem.tabComplete(PowerupEffectManager.getById().keySet(), args);
    }
}
