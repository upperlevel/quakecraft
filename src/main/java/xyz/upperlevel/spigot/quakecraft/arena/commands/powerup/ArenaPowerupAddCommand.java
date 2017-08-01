package xyz.upperlevel.spigot.quakecraft.arena.commands.powerup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.core.LocUtil;
import xyz.upperlevel.spigot.quakecraft.powerup.Powerup;
import xyz.upperlevel.spigot.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;


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
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.powerup.add");
        SUCCESS = manager.get("success");
    }
}
