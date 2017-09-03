package xyz.upperlevel.quakecraft.settings;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "tracking", desc = "Allows you to toggle the tracking device")
public class ToggleTrakingCommand extends Command {
    private static Message ENABLED;
    private static Message DISABLED;

    public ToggleTrakingCommand() {
        super("tracking");
        setDescription("Toggles the tracking device");
        addAlias("trackingdevice");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Optional @Argument("enabled") Boolean enabled) {
        Player player = (Player)sender;
        QuakePlayer qp = Quakecraft.get().getPlayerManager().getPlayer(player);
        if(enabled == null) {
            enabled = !qp.isTrackingActive();
        } else if(enabled == qp.isTrackingActive()) {
            return;
        }
        qp.setTrackingActive(enabled);
        (enabled ? ENABLED : DISABLED).send(player);
    }


    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.settings.tracking");
        ENABLED = manager.get("enabled");
        DISABLED = manager.get("disabled");
    }
}
