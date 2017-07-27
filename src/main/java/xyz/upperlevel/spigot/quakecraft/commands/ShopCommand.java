package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.gui.GuiId;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import static xyz.upperlevel.uppercore.Uppercore.guis;

public class ShopCommand extends Command {
    private static Message NO_GUI;

    public ShopCommand() {
        super("shop");
        setDescription("Opens the shop GUI.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        GuiId gui =  QuakeCraftReloaded.get().getGuis().get("shop");
        if (gui == null) {
            NO_GUI.send((Player) sender);
            return;
        }
        guis().open((Player) sender, gui.get());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.shop");
        NO_GUI = manager.get("no-shop");
    }
}
