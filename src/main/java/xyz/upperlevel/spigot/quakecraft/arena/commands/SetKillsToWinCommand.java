package xyz.upperlevel.spigot.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;

public class SetKillsToWinCommand extends Command {
    private static Message NEGATIVE_KILLS;
    private static Message SUCCESS;

    public SetKillsToWinCommand() {
        super("setkillstowin");
        setDescription("Sets arena kills to win.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("arena") Arena arena, @Argument("kills") int killsToWin) {
        if(killsToWin < 0) {
            NEGATIVE_KILLS.send(sender);
            return;
        }
        arena.setKillsToWin(killsToWin);
        SUCCESS.send(sender, "kills", String.valueOf(killsToWin), "arena", arena.getId());
    }

    public static void loadConfig() {
        MessageManager manager = QuakeCraftReloaded.get().getMessages().getSection("commands.arena.set-kills-to-win");
        NEGATIVE_KILLS = manager.get("negative-kills");
        SUCCESS = manager.get("success");
    }
}
