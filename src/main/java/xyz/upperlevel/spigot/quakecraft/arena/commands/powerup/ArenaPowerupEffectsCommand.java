package xyz.upperlevel.spigot.quakecraft.arena.commands.powerup;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.spigot.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import java.util.Collection;

public class ArenaPowerupEffectsCommand extends Command {
    private static Message HEADER;
    private static Message LINE;
    private static Message FOOTER;

    public ArenaPowerupEffectsCommand() {
        super("effects");
    }

    @Executor()
    public void run(CommandSender sender) {
        Collection<PowerupEffect> effects = PowerupEffectManager.get();
        int size = effects.size();
        HEADER.send(sender, "effects", String.valueOf(size));
        for(PowerupEffect effect : effects) {
            LINE.send(sender, "effect", effect.getId());
        }
        FOOTER.send(sender, "effects", String.valueOf(size));
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.powerup.effects");
        HEADER = manager.get("header");
        LINE = manager.get("line");
        FOOTER = manager.get("footer");
    }
}