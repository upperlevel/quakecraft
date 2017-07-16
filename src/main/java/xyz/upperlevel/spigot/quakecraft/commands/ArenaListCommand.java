package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;

import java.util.StringJoiner;

import static org.bukkit.ChatColor.*;

public class ArenaListCommand extends Command {

    public ArenaListCommand() {
        super("list");
        setDescription("Shows arenas list.");
    }

    @Executor
    public void run(CommandSender sender) {
        StringJoiner list = new StringJoiner(GRAY + ", ");
        for (Arena arena : QuakeCraftReloaded.get().getArenaManager().getArenas())
            list.add((QuakeCraftReloaded.get().getGameManager().getGame(arena) != null ? GREEN : RED) + "" + (arena.isReady() ? "" : STRIKETHROUGH) + arena.getName());
        if (list.length() > 0)
            sender.sendMessage(GOLD + "List of all arenas created: " + list.toString());
        else
            sender.sendMessage(RED + "No arena found.");
    }
}
