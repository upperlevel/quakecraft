package xyz.upperlevel.quakecraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.util.LocUtil;

import static xyz.upperlevel.uppercore.command.DefaultPermission.OP;

@WithPermission(value = "lobby", def = OP)
@WithChildPermission(def = OP, desc = "Gives access to global lobby view/relocation")
public class LobbyCommand extends NodeCommand {
    public LobbyCommand() {
        super("lobby");

        register(new SetLobbyCommand());
        register(new PrintLobbyCommand());
        register(new TeleportLobbyCommand());


        setDescription("Commands to change or check the global lobby");
        addAliases("globby", "globallobby", "global_lobby");
    }


    public static class SetLobbyCommand extends Command {

        public SetLobbyCommand() {
            super("set");
            setDescription("Changes the lobby to the player's position");
        }

        @Executor(sender = Sender.PLAYER)
        public void run(CommandSender sender) {
            Quakecraft.get().getArenaManager().setLobby(((Player)sender).getLocation());
            sender.sendMessage(ChatColor.GREEN + "Global lobby changed succesfully!");
        }
    }

    public static class PrintLobbyCommand extends Command {

        public PrintLobbyCommand() {
            super("print");
            addAlias("view");
            setDescription("Prints the current global lobby");
        }

        @Executor(sender = Sender.PLAYER)
        public void run(CommandSender sender) {
            Location lobby = Quakecraft.get().getArenaManager().getLobby();
            if(lobby != null) {
                sender.sendMessage(ChatColor.GREEN + "Global lobby: " + ChatColor.AQUA + LocUtil.format(lobby, true));
            } else {
                sender.sendMessage(ChatColor.RED + "Lobby not set, use '/quake lobby set' to set the global lobby");
            }
        }
    }

    public static class TeleportLobbyCommand extends Command {

        public TeleportLobbyCommand() {
            super("tp");

            setDescription("Teleports the player to the global lobby");
            addAliases("teleport", "visit", "go");
        }

        @Executor
        public void run(CommandSender sender, @Optional(sender = Sender.PLAYER) @Argument("player") Player player) {
            if (player == null)
                player = (Player) sender;
            Location lobby = Quakecraft.get().getArenaManager().getLobby();
            if (lobby != null) {
                player.teleport(lobby);
                sender.sendMessage(ChatColor.GREEN + "Successfully teleported to " + ChatColor.AQUA + LocUtil.format(lobby, true));
            } else {
                sender.sendMessage(ChatColor.RED + "Lobby not set");
            }
        }
    }


}
