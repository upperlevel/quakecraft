package xyz.upperlevel.quakecraft.arena;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.PermissionUser;
import xyz.upperlevel.uppercore.command.SenderType;
import xyz.upperlevel.uppercore.command.functional.AsCommand;
import xyz.upperlevel.uppercore.command.functional.WithPermission;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.List;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class QuakeArenaCommands {
    public QuakeArenaCommands() {
    }

    @AsCommand(
            description = "Set min and max players limit.",
            aliases = {"setlimit", "setplayers"}
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void setlimits(CommandSender sender, QuakeArena arena, int min, int max) {
        if (min > max) {
            sender.sendMessage(RED + "Minimum number of players is higher than max (" + min + " > " + max + ").");
            return;
        }
        arena.setLimits(min, max);
        sender.sendMessage(GREEN + "Arena '" + arena.getId() + "' limits changed.");
    }

    @AsCommand(
            description = "Add a spawn to the arena.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void addspawn(CommandSender sender, QuakeArena arena) {
        arena.addSpawn(((Player) sender).getLocation());
        sender.sendMessage("Arena '" + arena.getId() + "' spawn added.");
    }

    @AsCommand(
            description = "List a spawn to the arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void spawnslist(CommandSender sender, QuakeArena arena) {
        List<Location> spawns = arena.getSpawns();
        if (spawns.size() > 0) {
            sender.sendMessage(GREEN + "Showing " + spawns.size() + " spawns for '" + arena.getId() + "':");
            for (int i = 0; i < spawns.size(); i++) {
                sender.sendMessage(GREEN + "" + (i + 1) + ": " + LocUtil.format(spawns.get(i), true));
            }
        } else {
            sender.sendMessage(GREEN + "No spawn for '" + arena.getId() + "'.");
        }
    }

    @AsCommand(
            description = "Remove a spawn from an arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void removespawn(CommandSender sender, QuakeArena arena, int which) {
        if (which <= 1 || which > arena.getSpawns().size()) {
            sender.sendMessage(RED + "'" + arena.getId() + "' spawn index can't be negative or higher than " + arena.getSpawns().size() + ".");
            return;
        }
        arena.getSpawns().remove(which - 1);
        sender.sendMessage(GREEN + "Spawn " + which + " removed from '" + arena.getId() + "'.");
    }

    @AsCommand(
            description = "Add arena powerup to player's position.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void addpowerup(CommandSender sender, QuakeArena arena, PowerupEffect effect, int respawnTicks) {
        arena.getPowerups().add(new Powerup(arena, ((Player) sender).getLocation(), effect, respawnTicks));
        sender.sendMessage(GREEN + "Add powerup '" + effect.getId() + "' with respawn rate of " + respawnTicks + " ticks to arena '" + arena.getId() + "'.");
    }

    @AsCommand(
            description = "List powerups created for that arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void powerups(CommandSender sender, QuakeArena arena) {
        List<Powerup> powerups = arena.getPowerups();
        if (powerups.size() > 0) {
            sender.sendMessage(GREEN + "Showing " + powerups.size() + " powerups for '" + arena.getId() + "':");
            for (int i = 0; i < powerups.size(); i++) {
                Powerup powerup = powerups.get(i);
                sender.sendMessage(GREEN + "" + (i + 1) + ": " +
                        powerup.getEffect().getId() +
                        " at " + LocUtil.format(powerup.getLocation(), true) +
                        " with respawn of " + powerup.getRespawnTicks() + " ticks.");
            }
        } else {
            sender.sendMessage(GREEN + "No powerups created for '" + arena.getId() + "'.");
        }
    }

    @AsCommand(
            description = "Remove a powerup from an arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void removepowerup(CommandSender sender, QuakeArena arena, int which) {
        if (which <= 1 || which > arena.getSpawns().size()) {
            sender.sendMessage(RED + "'" + arena.getId() + "' powerup index can't be negative or higher than " + arena.getSpawns().size() + ".");
            return;
        }
        arena.getSpawns().remove(which - 1);
        sender.sendMessage(GREEN + "Powerup " + which + " removed from '" + arena.getId() + "'.");
    }
}
