package xyz.upperlevel.quakecraft.profile;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.functional.AsCommand;
import xyz.upperlevel.uppercore.command.functional.FunctionalCommand;
import xyz.upperlevel.uppercore.command.functional.WithOptional;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class ProfileCommands extends NodeCommand {
    public ProfileCommands() {
        super("profile");

        FunctionalCommand.inject(this, this);
    }

    @AsCommand(description = "Shows info about the specified profile.")
    public void show(CommandSender sender, @WithOptional String name) {
        if (name == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(RED + "Specify the player's name.");
                return;
            }
            name = sender.getName();
        }
        Profile profile = Quake.getProfileController().getProfileCached(name);
        if (profile == null) {
            sender.sendMessage(RED + String.format("Profile not found for '%s', has the user ever joined the server?", name));
            return;
        }
        sender.sendMessage(GREEN + profile.toString());
    }

    @AsCommand(description = "Deletes the specified profile.")
    public void delete(CommandSender sender, @WithOptional String name) {
        if (name == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(RED + "Specify the player's name.");
                return;
            }
            name = sender.getName();
        }
        ProfileController controller = Quake.getProfileController();
        Profile profile = controller.getProfileCached(name);
        if (profile == null || !controller.deleteProfileCached(profile.getId())) {
            sender.sendMessage(RED + String.format("Profile didn't found for: '%s'.", name));
            return;
        }
        sender.sendMessage(GREEN + String.format("Profile of '%s' deleted.", profile.getName()));
    }
}
