package xyz.upperlevel.quakecraft.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.PermissionUser;
import xyz.upperlevel.uppercore.command.SenderType;
import xyz.upperlevel.uppercore.command.functional.AsCommand;
import xyz.upperlevel.uppercore.command.functional.FunctionalCommand;
import xyz.upperlevel.uppercore.command.functional.WithPermission;
import xyz.upperlevel.uppercore.registry.Registry;
import xyz.upperlevel.uppercore.registry.RegistryVisitor;

public class DebugCommand extends NodeCommand {
    public DebugCommand() {
        super("quack");

        description("Quak quak lets find that bug.");

        // Registers must used commands as locals: such as join, quit, shop...
        FunctionalCommand.inject(this, this);
    }

    @AsCommand(
            description = "Prints the registry tree",
            sender = SenderType.ALL
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void regtree(CommandSender sender) {
        Uppercore.registry().visit(new RegistryVisitor() {
            String spaces = "";

            @Override
            public VisitResult preVisitRegistry(Registry registry) {
                sender.sendMessage(spaces + "> " + registry.getName());
                spaces += "  ";
                return VisitResult.CONTINUE;
            }

            @Override
            public VisitResult visitEntry(String s, Object o) {
                sender.sendMessage(spaces + "'" + s + "' -> '" + o + "'");
                return VisitResult.CONTINUE;
            }

            @Override
            public VisitResult postVisitRegistry(Registry registry) {
                spaces = spaces.substring(2);
                sender.sendMessage(spaces + "< " + registry.getName());
                return VisitResult.CONTINUE;
            }
        });
    }
}
