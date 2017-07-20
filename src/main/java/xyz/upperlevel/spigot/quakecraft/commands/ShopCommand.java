package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.gui.GuiSystem;

public class ShopCommand extends Command {
    public ShopCommand() {
        super("shop");
        setDescription("Opens the shop GUI.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        GuiSystem.open((Player)sender, QuakeCraftReloaded.get().getGuis().get("shop"));
    }
}
