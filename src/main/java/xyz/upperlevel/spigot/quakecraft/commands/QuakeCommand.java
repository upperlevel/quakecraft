package xyz.upperlevel.spigot.quakecraft.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.upperlevel.spigot.quakecraft.commands.arguments.ArenaArgumentParser;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.arguments.ArgumentParserManager;
import xyz.upperlevel.uppercore.command.exceptions.CommandSyntaxException;
import xyz.upperlevel.uppercore.command.exceptions.InternalCommandException;
import xyz.upperlevel.uppercore.command.exceptions.NoCommandFoundException;

import java.util.Arrays;

public class QuakeCommand extends NodeCommand implements CommandExecutor {

    public QuakeCommand() {
        super("quake");

        register(new CreateArenaCommand());
        register(new ArenaInfoCommand());
        register(new DeleteArenaCommand());
        register(new JoinGameCommand());
        register(new LeaveGameCommand());
        register(new SetDisplayNameCommand());
        register(new ArenaListCommand());
        register(new SetLobbyCommand());
        register(new SetLimitsCommand());
        register(new SaveArenaCommand());
        register(new AddSpawnCommand());
        register(new EnableArenaCommand());
        register(new DisableArenaCommand());

        setDescription("Main commands of QuakeReloaded plugin.");
        addAliases("quakecraft", "quakecraftreloaded");
    }

    // TODO fix up this shit
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        ArgumentParserManager p = new ArgumentParserManager();
        p.registerDefaults();
        p.register(new ArenaArgumentParser());
        try {
            super.execute(p, sender, Arrays.asList(args));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            sender.sendMessage("syntax err");
        } catch (NoCommandFoundException e) {
            e.printStackTrace();
            sender.sendMessage("no cmd found");
        } catch (InternalCommandException e) {
            e.printStackTrace();
            sender.sendMessage("internal err");
        }
        return true;
    }
}
