package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class SaveArenaCommand extends Command {

    public SaveArenaCommand() {
        super("save");
        setDescription("Saves an arena.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena) {
        if (!arena.isReady()) {
            sender.sendMessage(RED + "Something is not setup in the arena you are editing. Try issue: /quake arenaInfo to see what is missing.");
            return;
        }
        QuakeCraftReloaded.get().getArenaManager().addArena(arena);
        sender.sendMessage(GREEN + "Arena '" + arena.getName() + "' saved successfully.");
    }
}
