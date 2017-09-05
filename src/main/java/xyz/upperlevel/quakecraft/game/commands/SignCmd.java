package xyz.upperlevel.quakecraft.game.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.game.QuakePhase;
import xyz.upperlevel.uppercore.command.*;

import java.util.Set;

@WithPermission(value = "sign", desc = "Allows to use \"sign\" command")
public class SignCmd extends NodeCommand {
    public SignCmd() {
        super("sign");
        setDescription("Sign commands");
        register(new SignAddCmd());
        register(new SignRemoveCmd());
    }

    @WithPermission(value = "add", desc = "Allows to add a sign")
    private static class SignAddCmd extends Command {
        public SignAddCmd() {
            super("add");
            setDescription("Adds a sign");
            setSender(Sender.PLAYER);
        }

        @Executor
        public void onRun(CommandSender sender, @Argument("game") Game game) {
            Block b = ((Player) sender).getTargetBlock((Set<Material>) null, 100);
            if (b != null && b.getState() instanceof Sign) {
                if (game.addSign((Sign) b.getState())) {
                    ((QuakePhase) game.getPhaseManager().getPhase()).updateSigns();
                    sender.sendMessage(ChatColor.GREEN + "Sign added.");
                } else
                    sender.sendMessage(ChatColor.RED + "This sign has been already added.");
            } else {
                sender.sendMessage(ChatColor.RED + "You are not targeting a sign.");
            }
        }
    }

    @WithPermission(value = "remove", desc = "Allows to remove a sign")
    private static class SignRemoveCmd extends Command {
        public SignRemoveCmd() {
            super("remove");
            setDescription("Removes a sign");
            setSender(Sender.PLAYER);
        }

        @Executor
        public void onRun(CommandSender sender, @Argument("game") Game game) {
            Block b = ((Player) sender).getTargetBlock((Set<Material>) null, 100);
            if (b != null && b.getState() instanceof Sign) {
                if (game.removeSign((Sign) b.getState())) {
                    b.breakNaturally(null);
                    sender.sendMessage(ChatColor.GREEN + "Sign removed.");
                } else {
                    sender.sendMessage(ChatColor.RED + "This sign does not appertain to this game.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You are not targeting a sign.");
            }
        }
    }
}
