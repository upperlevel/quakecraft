package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class ArenaSetLimitsCommand extends Command {

    public ArenaSetLimitsCommand() {
        super("setlimits");
        setDescription("Sets arena min and max players count.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("min") int min, @Argument("max") int max) {
        if (max < min) {
            sender.sendMessage(RED + "Max players limit cannot be higher than min players one.");
            return;
        }
        arena.setLimits(min, max);
        sender.sendMessage(GREEN + "Limits set successfully.");
    }
}
