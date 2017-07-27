package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class ArenaCreateCommand extends Command {

    public ArenaCreateCommand() {
        super("create");
        setDescription( "Creates a new arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") String arenaId) {
        if (QuakeCraftReloaded.get().getArenaManager().getArena(arenaId) != null) {
            sender.sendMessage(RED + "An arena with this name already exist.");
            return;
        }
        if (!Arena.isValidName(arenaId)) {
            sender.sendMessage(RED + "Arena name insert is invalid. It must contains only alphabetic or digit characters.");
            return;
        }
        QuakeCraftReloaded.get().getArenaManager().addArena(new Arena(arenaId));
        sender.sendMessage(GREEN + "Arena created successfully, setup it!");
    }
}
