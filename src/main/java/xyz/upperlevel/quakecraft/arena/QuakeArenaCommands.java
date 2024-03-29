package xyz.upperlevel.quakecraft.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.arena.ArenaManager;
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

    // ================================================================================
    // Limits
    // ================================================================================

    @AsCommand(
            description = "Set min and max players limit.",
            aliases = {"setlimit", "setplayers"}
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void setLimits(Player player, int min, int max) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        if (min > max) {
            player.sendMessage(RED + "Minimum number of players is higher than max (" + min + " > " + max + ").");
            return;
        }
        arena.setLimits(min, max);
        player.sendMessage(GREEN + "Arena '" + arena.getId() + "' limits changed.");
    }

    // ================================================================================
    // Spawns
    // ================================================================================

    @AsCommand(
            description = "Adds a spawn to the arena.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    public void addSpawn(Player player) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        arena.addSpawn(player.getLocation());
        player.sendMessage(GREEN + "Spawn added.");
    }

    @AsCommand(
            description = "Removes the last spawn of the arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void rmSpawn(Player player, int which) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        if (which <= 0 || which > arena.getSpawns().size()) {
            player.sendMessage(RED + "'" + arena.getId() + "' spawn index can't be negative or higher than " + arena.getSpawns().size() + ".");
            return;
        }
        arena.removeSpawn(which - 1);
        player.sendMessage(GREEN + "Spawn " + which + " removed from '" + arena.getId() + "'.");
    }

    @AsCommand(
            description = "Lists a spawn to the arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void spawnsList(Player player) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        List<Location> spawns = arena.getSpawns();
        if (spawns.size() > 0) {
            player.sendMessage(GREEN + "Showing " + spawns.size() + " spawns:");
            for (int i = 0; i < spawns.size(); i++) {
                player.sendMessage(GREEN + "" + (i + 1) + ": " + LocUtil.format(spawns.get(i), true));
            }
        } else {
            player.sendMessage(GREEN + "No spawn for '" + arena.getId() + "'.");
        }
    }

    // ================================================================================
    // Powerups
    // ================================================================================

    @AsCommand(
            description = "Add arena powerup to player's position.",
            sender = SenderType.PLAYER
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void addPowerup(Player player, PowerupEffect effect, int respawnTicks) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        arena.addPowerup(new Powerup(player.getLocation(), effect, respawnTicks));
        player.sendMessage(GREEN + "Add powerup '" + effect.getId() + "' with respawn rate of " + respawnTicks + " ticks to arena '" + arena.getId() + "'.");
    }

    @AsCommand(
            description = "List powerups created for that arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void powerupsList(Player player) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        List<Powerup> powerups = arena.getPowerups();
        if (powerups.size() > 0) {
            player.sendMessage(GREEN + "Showing " + powerups.size() + " powerups for '" + arena.getId() + "':");
            for (int i = 0; i < powerups.size(); i++) {
                Powerup powerup = powerups.get(i);
                player.sendMessage(GREEN + "" + (i + 1) + ": " +
                        powerup.getEffect().getId() +
                        " at " + LocUtil.format(powerup.getLocation(), true) +
                        " with respawn of " + powerup.getRespawnTicks() + " ticks.");
            }
        } else {
            player.sendMessage(GREEN + "No powerups created for '" + arena.getId() + "'.");
        }
    }

    @AsCommand(
            description = "Remove a powerup from an arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void rmPowerup(Player player, int which) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        if (which <= 0 || which > arena.getPowerups().size()) {
            player.sendMessage(RED + "'" + arena.getId() + "' powerup index can't be negative or higher than " + arena.getPowerups().size() + ".");
            return;
        }
        arena.removePowerup(which - 1);
        player.sendMessage(GREEN + "Powerup " + which + " removed from '" + arena.getId() + "'.");
    }

    @AsCommand(
            description = "Sets the kills needed to win for an arena."
    )
    @WithPermission(
            user = PermissionUser.OP
    )
    protected void setKillsToWin(Player player, int killsToWin) {
        QuakeArena arena = (QuakeArena) ArenaManager.get().get(player.getWorld());
        if (arena == null) {
            player.sendMessage(RED + "This world doesn't hold any arena.");
            return;
        }
        if (killsToWin <= 0) {
            player.sendMessage(RED + "Kills to win must be higher (>) than 0.");
            return;
        }
        arena.setKillsToWin(killsToWin);
        player.sendMessage(GREEN + String.format("Kills to win set to %d for '%s'.", killsToWin, arena.getId()));
    }
}
