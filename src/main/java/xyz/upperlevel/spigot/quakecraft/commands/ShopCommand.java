package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.gui.GuiId;

import static org.bukkit.ChatColor.RED;
import static xyz.upperlevel.uppercore.Uppercore.guis;

public class ShopCommand extends Command {
    public ShopCommand() {
        super("shop");
        setDescription("Opens the shop GUI.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        GuiId gui =  QuakeCraftReloaded.get().getGuis().get("shop");
        if (gui == null) {
            sender.sendMessage(RED + "Cannot find shop gui.");
            return;
        }
        guis().open((Player) sender, gui.get());
    }
}
