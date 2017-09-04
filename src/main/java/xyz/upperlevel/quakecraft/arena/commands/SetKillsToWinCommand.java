package xyz.upperlevel.quakecraft.arena.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.command.WithPermission;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.message.MessageManager;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;

@WithPermission(value = "setkillstowin", desc = "Allows you to set an arena's kills to win")
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
            PlaceholderRegistry reg = PlaceholderRegistry.create(arena.getPlaceholders());
            reg.set("kills", killsToWin);
            NEGATIVE_KILLS.send(sender, reg);
            return;
        }
        arena.setKillsToWin(killsToWin);
        SUCCESS.send(sender, arena.getPlaceholders());
    }

    public static void loadConfig() {
        MessageManager manager = Quakecraft.get().getMessages().getSection("commands.arena.set-kills-to-win");
        NEGATIVE_KILLS = manager.get("negative-kills");
        SUCCESS = manager.get("success");
    }
}