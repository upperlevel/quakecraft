package xyz.upperlevel.spigot.quakecraft.arena.commands;

import net.wesjd.anvilgui.AnvilGUI.ClickHandler;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.gui.*;
import xyz.upperlevel.uppercore.gui.link.Link;

import java.util.List;
import java.util.function.BiConsumer;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.gui.InputFilters.filterInt;
import static xyz.upperlevel.uppercore.gui.InputFilters.plain;

public class ArenaSetupGuiCommand extends Command {
    public ArenaSetupGuiCommand() {
        super("setupgui");
        setDescription("Setups the arena with a simple GUI interface.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Optional @Argument("arena") Arena arena) {
        sender.sendMessage(GREEN + "Opening GUI.");
        if(arena == null) {
            AnvilGui gui = new AnvilGui();
            gui.setMessage("Arena name");
            gui.setListener(arenaFilter(this::execute));
            guis().open(((Player)sender), gui);
        } else execute((Player)sender, arena);

    }

    public void execute(Player player, Arena arena) {
        //enabled, name, min_player, max_player, spawn,
        Gui g = ChestGui.builder(9)
                .title(arena.getId() + "'s setup")
                .add(
                        () -> GuiUtil.wool(
                                arena.isEnabled() ? DyeColor.GREEN : DyeColor.RED,
                                arena.isEnabled() ? "Disable" : "Enable"),
                        p -> {
                            arena.setEnabled(!arena.isEnabled());
                            guis().reprint(p);
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.NAME_TAG,
                                "Name",
                                AQUA + arena.getName()
                        ),
                        p -> {
                            AnvilGui gui = new AnvilGui();
                            gui.setMessage("Arena name");
                            gui.setListener(plain((pl, n) -> {
                                arena.setName(n);
                                guis().back(p);
                            }));
                            guis().open(p, gui);
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.STONE_SLAB2,
                                "Min players",
                                AQUA + String.valueOf(arena.getMinPlayers())
                        ),
                        p -> {
                            AnvilGui gui = new AnvilGui();
                            gui.setMessage("Min players");
                            gui.setListener(filterInt(
                                    (pl, i) -> {
                                        arena.setMinPlayers(i);
                                        guis().back(pl);
                                    },
                                    i -> i >= 2
                            ));
                            guis().open(p, gui);
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.RED_SANDSTONE,
                                "Max players",
                                AQUA + String.valueOf(arena.getMaxPlayers())
                        ),
                        p -> {
                            AnvilGui gui = new AnvilGui();
                            gui.setMessage("Man players");
                            gui.setListener(filterInt(
                                    (pl, i) -> {
                                        arena.setMaxPlayers(i);
                                        guis().back(pl);
                                    },
                                    i -> i >= 2
                            ));
                            guis().open(p, gui);
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.MONSTER_EGG,
                                "Spawns",
                                AQUA + String.valueOf(arena.getSpawns().size()) + " Spawns"
                        ),
                        p -> {
                            Gui gui = editSpawnsGui(arena);
                            if(gui != null)
                                guis().open(p, gui);
                            else
                                player.sendMessage(RED + "Too many spawns for a gui, use command-based editing instead");
                        }
                ).build();
        guis().open(player, g);
    }

    public Gui editSpawnsGui(Arena arena) {//TODO page-based displaying
        List<Location> locs = arena.getSpawns();
        if(locs.size() > (GuiSize.DOUBLE.size() - 2))
            return null;
        ChestGui gui = new ChestGui(GuiSize.min(locs.size() + 2), arena.getId() + "'s Spawns");

        boolean sameWorld = true;
        if(locs.size() > 0){
            World w = locs.get(0).getWorld();
            for(int i = locs.size() - 1; i >= 1; i--) {
                if(w != locs.get(i).getWorld()) {
                    sameWorld = false;
                    break;
                }
            }
        }

        int i = 0;
        for(Location loc : locs) {
            String world = sameWorld ? "" : (loc.getWorld().getName() + ':');
            final int index = i;
            gui.setIcon(
                    i,
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.MONSTER_EGG,
                                    "Spawn " + i,
                                    String.valueOf(AQUA) + world + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ()
                            ),
                            p -> guis().change(p, editSpawnGui(locs, index, pl -> guis().change(pl, editSpawnsGui(arena))))
                    )
            );
            i++;
        }
        gui.setIcon(
                gui.getSize()  - 2,
                Icon.of(
                        GuiUtil.wool(DyeColor.GREEN, "Add"),
                        p -> {
                            locs.add(p.getLocation());
                            guis().change(p, editSpawnsGui(arena));
                        }
                )
        );
        gui.setIcon(gui.getSize() - 1, Icon.of(GuiUtil.itemStack(Material.ARROW, "Back"), GuiAction.back()));
        return gui;
    }

    public Gui editSpawnGui(List<Location> list, int index, Link previous) {//TODO rethink GUI system
        Location loc = list.get(index);
        return ChestGui.builder(9)
                .title("Spawn editor")
                .add(
                        GuiUtil.itemStack(
                                Material.SADDLE,
                                "Teleport",
                                AQUA + "To " + loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ()),
                        p -> {
                            guis().close(p);
                            p.sendMessage(AQUA + "Teleporting...");
                            p.teleport(loc);
                        }
                )
                .add(
                        GuiUtil.itemStack(
                                Material.STICK,
                                "Edit"
                        ),
                        p -> {//TODO add confirm
                            list.set(index, p.getLocation());
                            guis().change(p, editSpawnGui(list, index, previous));
                        }
                )
                .add(
                        GuiUtil.itemStack(Material.BARRIER, "Remove"),
                        p -> {//TODO add confirm
                            list.remove(index);
                            previous.run(p);
                        }
                )
                .set(
                        8,
                        GuiUtil.itemStack(Material.ARROW, "Back"),
                        previous
                )
                .build();
    }


    public static ClickHandler arenaFilter(BiConsumer<Player, Arena> listener) {
        return (player, name) -> {
            Arena arena = QuakeCraftReloaded.get().getArenaManager().getArena(name);
            if (arena == null)
                return "Invalid arena";
            listener.accept(player, arena);
            return null;
        };
    }
}
