package xyz.upperlevel.quakecraft.commands;

import org.bukkit.util.StringUtil;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.arena.Arena;
import xyz.upperlevel.uppercore.command.functional.parameter.ParameterHandler;

import java.util.Collections;
import java.util.stream.Collectors;

public final class QuakeParameterHandler {
    private QuakeParameterHandler() {
    }

    public static void register() {
        // QuakeArena
        ParameterHandler.register(
                Collections.singletonList(QuakeArena.class),
                args -> {
                    String arg = args.take();
                    QuakeArena arena = (QuakeArena) Quake.get().getArenaManager().get(arg);
                    if (arena == null) {
                        throw args.areWrong();
                    }
                    return arena;
                },
                args -> {
                    if (args.remaining() > 1)
                        return Collections.emptyList();
                    String arg = args.take();
                    return Quake.get().getArenaManager().getArenas()
                            .stream()
                            .map(Arena::getName)
                            .filter(name -> StringUtil.startsWithIgnoreCase(name, arg))
                            .collect(Collectors.toList());
                });

        // PowerEffect
        ParameterHandler.register(
                Collections.singletonList(PowerupEffect.class),
                args -> {
                    String arg = args.take();
                    PowerupEffect effect = PowerupEffectManager.fromId(arg);
                    if (effect == null)
                        throw args.areWrong();
                    return effect;
                },
                args -> {
                    if (args.remaining() > 1)
                        return Collections.emptyList();
                    String arg = args.take();
                    return PowerupEffectManager.getById().keySet()
                            .stream()
                            .filter(id -> StringUtil.startsWithIgnoreCase(id, arg))
                            .collect(Collectors.toList());
                });
    }
}
