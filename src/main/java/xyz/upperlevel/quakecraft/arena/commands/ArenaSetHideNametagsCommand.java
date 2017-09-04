package xyz.upperlevel.quakecraft.arena.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;

@WithPermission(value = "sethidenametags", desc = "Allows you to toggle nametags visibility")
public class ArenaSetHideNametagsCommand extends Command {

    public ArenaSetHideNametagsCommand() {
        super("sethidenametags");
        setDescription("Sets arena nametags hidden or not.");
        addAliases("hidenametags", "sethidenames", "setnametags", "setnames");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Optional @Argument("enabled") Boolean enabled) {
        if(enabled == null) {
            enabled = !arena.isHideNametags();
        }
        arena.setHideNametags(enabled);
        sender.sendMessage(ChatColor.GREEN + arena.getId() + "'s nametags: " + (enabled ? ChatColor.RED + "Hidden" : ChatColor.GREEN + "Visible"));
    }
}