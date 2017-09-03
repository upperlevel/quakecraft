package xyz.upperlevel.quakecraft.settings;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

@WithPermission(value = "particles", desc = "Allows you to toggle the particles")
public class ToggleParticlesCommand extends Command {
    private static Message ENABLED;
    private static Message DISABLED;

    public ToggleParticlesCommand() {
        super("particles");
        setDescription("Toggles the particles");
        addAlias("particle");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Optional @Argument("enabled") Boolean enabled) {
        Player player = (Player)sender;
        QuakePlayer qp = Quakecraft.get().getPlayerManager().getPlayer(player);
        if(enabled == null) {
            enabled = !qp.isParticleActive();
        } else if(enabled == qp.isParticleActive()) {
            return;
        }
        qp.setParticleActive(enabled);
        (enabled ? ENABLED : DISABLED).send(player);
    }


    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.settings.particles");
        ENABLED = manager.get("enabled");
        DISABLED = manager.get("disabled");
    }
}