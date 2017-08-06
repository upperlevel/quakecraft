package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.Sender;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.gui.GuiId;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

import static xyz.upperlevel.uppercore.Uppercore.guis;

@WithPermission(value = "shop", desc = "Allows you to open the shop")
public class ShopCommand extends Command {
    private static Message NO_GUI;

    public ShopCommand() {
        super("shop");
        setDescription("Opens the shop GUI.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender) {
        GuiId gui =  Quakecraft.get().getGuis().get("shop_gui");
        if (gui == null) {
            NO_GUI.send((Player) sender);
            return;
        }
        guis().open((Player) sender, gui.get());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.shop");
        NO_GUI = manager.get("no-shop");
    }
}
