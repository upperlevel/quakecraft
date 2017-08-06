package xyz.upperlevel.quakecraft.arena.commands.powerup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.util.LocUtil;


@WithPermission(value = "add", desc = "Allows you to add a powerup to an arena")
public class ArenaPowerupAddCommand extends Command {
    private static Message SUCCESS;

    public ArenaPowerupAddCommand() {
        super("add");
        setDescription("Adds arena powerup.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("effect") PowerupEffect effect, @Argument("respawn") int respawnTicks) {
        arena.getPowerups().add(new Powerup(arena, ((Player)sender).getLocation(), effect, respawnTicks));
        PlaceholderRegistry reg = PlaceholderRegistry.create(arena.getPlaceholders());
        reg.set("effect", effect.getId());
        reg.set("loc", LocUtil.format(((Player) sender).getLocation(), true));
        reg.set("respawn", respawnTicks);
        SUCCESS.send((Player) sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.powerup.add");
        SUCCESS = manager.get("success");
    }
}
