package xyz.upperlevel.spigot.quakecraft.arena.commands;

import net.wesjd.anvilgui.AnvilGUI.ClickHandler;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.spigot.quakecraft.arena.Arena;
import xyz.upperlevel.spigot.quakecraft.arena.ArenaManager;
import xyz.upperlevel.spigot.quakecraft.game.Game;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.gui.*;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.placeholder.PlaceholderUtil;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.List;
import java.util.function.BiConsumer;

import static org.bukkit.ChatColor.*;
import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.gui.InputFilters.filterInt;
import static xyz.upperlevel.uppercore.gui.InputFilters.filterPlayer;
import static xyz.upperlevel.uppercore.gui.InputFilters.plain;

public class ArenaSetupGuiCommand extends Command {
    public ArenaSetupGuiCommand() {
        super("setupgui");
        setDescription("Setups the arena with a simple GUI interface.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Optional @Argument("arena") Arena arena) {
        Player player = (Player) sender;
        sender.sendMessage(GREEN + "Opening GUI.");
        Gui gui = arena == null ? selectArena(player) : editArena(player, arena, GuiAction.close());
        if(gui != null)
            guis().open(player, gui);
    }

    public Gui selectArena(Player player) {//TODO page-based displaying
        List<Arena> arenas = QuakeCraftReloaded.get().getArenaManager().getArenas();
        if(arenas.size() > (GuiSize.DOUBLE.size() - 2)) {
            player.sendMessage(RED + "Too many arenas! specify the arena name in the command!");
            return null;
        }
        ChestGui gui = new ChestGui(GuiSize.min(arenas.size() + 2), PlaceholderValue.fake("Select arena"));
        int i = 0;
        for(Arena arena : arenas) {
            gui.setIcon(
                    i,
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.MONSTER_EGG,
                                    arena.getId(),
                                    GRAY + "Id: " + AQUA + arena.getId(),
                                    GRAY + "Name:" + AQUA + arena.getName(),
                                    GRAY + "Spawns: " + AQUA + arena.getSpawns().size()

                            ),
                            p -> guis().change(p, editArena(p, arena, pr -> guis().change(pr, selectArena(pr))))
                    )
            );
            i++;
        }
        gui.setIcon(
                gui.getSize()  - 2,
                Icon.of(
                        GuiUtil.wool(DyeColor.GREEN, "Add"),
                        p -> {
                            AnvilGui anvil = new AnvilGui();
                            anvil.setMessage("Arena Id");
                            anvil.setListener(nonArenaFilter(
                                    (pl, id) -> {
                                        Arena arena = new Arena(id);
                                        QuakeCraftReloaded.get().getArenaManager().addArena(arena);
                                        p.sendMessage(GREEN + "Spawn added to arena!");
                                        guis().change(pl, editArena(pl, arena, pr -> guis().change(pr, selectArena(pr))));
                                    }
                            ));
                            guis().open(p, anvil);
                        }
                )
        );
        gui.setIcon(gui.getSize() - 1, Icon.of(GuiUtil.itemStack(Material.ARROW, "Back"), GuiAction.back()));
        return gui;
    }

    public Gui editArena(Player player, Arena arena, Link previous) {//TODO: OMG kill me I want the fucking classes!
        //enabled, name, min_player, max_player, spawn, lobby
        Location lobbyLoc = arena.getLobby();
        return ChestGui.builder(9)
                .title(arena.getId() + "'s setup")
                .add(
                        () -> GuiUtil.wool(
                                arena.isEnabled() ? DyeColor.GREEN : DyeColor.RED,
                                arena.isEnabled() ? "Disable" : "Enable"),
                        p -> {
                            boolean changed = arena.isEnabled() ? disable(player, arena) : enable(player, arena);
                            if(changed)
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
                                pl.sendMessage(GREEN + "New arena name: '" + n + "'!");
                            }));
                            guis().open(p, gui);
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.STONE_SLAB2,
                                "Min players",
                                AQUA + (arena.getMinPlayers() > 0 ? String.valueOf(arena.getMinPlayers()) : (RED + "Not set"))
                        ),
                        p -> {
                            AnvilGui gui = new AnvilGui();
                            gui.setMessage("Min players");
                            gui.setListener(filterInt(
                                    (pl, i) -> {
                                        arena.setMinPlayers(i);
                                        pl.sendMessage(GREEN + "Arena's min players: '" + i + "'!");
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
                                AQUA + (arena.getMaxPlayers() > 0 ? String.valueOf(arena.getMaxPlayers()) : (RED + "Not set"))
                        ),
                        p -> {
                            AnvilGui gui = new AnvilGui();
                            gui.setMessage("Max players");
                            gui.setListener(filterInt(
                                    (pl, i) -> {
                                        arena.setMaxPlayers(i);
                                        pl.sendMessage(GREEN + "Arena's max players: '" + i + "'!");
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
                            if (gui != null)
                                guis().open(p, gui);
                            else
                                player.sendMessage(RED + "Too many spawns for a gui, use command-based editing instead");
                        }
                )
                .add(
                        () -> GuiUtil.itemStack(
                                Material.WATCH,
                                "Lobby",
                                lobbyLoc == null ? (RED + "Not set") : (AQUA + format(lobbyLoc, true))
                        ),
                        p -> {
                            if(lobbyLoc == null) {
                                arena.setLobby(p.getLocation());
                                p.sendMessage(GREEN + "lobby's new position: " + format(p.getLocation(), true) + "!");
                                guis().change(player, editArena(player, arena, previous));
                            } else
                                guis().change(player, editLobby(arena, editArena(player, arena, previous)));
                        }
                )
                .set(
                        7,
                        GuiUtil.itemStack(Material.BARRIER, "Remove"),
                        p -> {//TODO add confirm
                            if(arena.isEnabled()) {
                                player.sendMessage(RED + "Disable the arena before removing it!");
                                return;
                            }
                            QuakeCraftReloaded.get().getArenaManager().removeArena(arena.getId());
                            p.sendMessage(GREEN + "Arena '" + arena.getId() + "' removed!");
                            previous.run(player);
                        }
                )
                .set(
                        8,
                        GuiUtil.itemStack(Material.ARROW, "Back"),
                        previous
                )
                .build();
    }

    public Gui editSpawnsGui(Arena arena) {//TODO page-based displaying
        List<Location> locs = arena.getSpawns();
        if(locs.size() > (GuiSize.DOUBLE.size() - 2))
            return null;
        ChestGui gui = new ChestGui(GuiSize.min(locs.size() + 2), PlaceholderValue.fake(arena.getId() + "'s Spawns"));

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
            final int index = i;
            gui.setIcon(
                    i,
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.MONSTER_EGG,
                                    "Spawn " + (i + 1),
                                    String.valueOf(AQUA) + format(loc, !sameWorld)
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
                            p.sendMessage(GREEN + "Spawn added to arena!");
                            guis().change(p, editSpawnsGui(arena));//Reload page
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
                                AQUA + "To " + format(loc, true)),
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
                            p.sendMessage(GREEN + "Spawn " + (index + 1) + " new position: " + format(p.getLocation(), true) + "!");
                            guis().change(p, editSpawnGui(list, index, previous));
                        }
                )
                .add(
                        GuiUtil.itemStack(Material.BARRIER, "Remove"),
                        p -> {//TODO add confirm
                            list.remove(index);
                            p.sendMessage(GREEN + "Spawn " + (index + 1) + " removed!");
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

    public Gui editLobby(Arena arena, Link previous) {//TODO rethink GUI system
        Location loc = arena.getLobby();
        return ChestGui.builder(9)
                .title("Lobby editor")
                .add(
                        GuiUtil.itemStack(
                                Material.SADDLE,
                                "Teleport",
                                AQUA + "To " + format(loc, true)),
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
                            arena.setLobby(p.getLocation());
                            p.sendMessage(GREEN + "lobby's new position: " + format(p.getLocation(), true) + "!");
                            guis().change(p, editLobby(arena, previous));
                        }
                )
                .set(
                        8,
                        GuiUtil.itemStack(Material.ARROW, "Back"),
                        previous
                )
                .build();
    }

    private boolean enable(Player player, Arena arena) {
        if (!arena.isReady()) {
            player.sendMessage(RED + "The arena \"" + arena.getName() + "\" is not ready.");
            return false;
        }
        QuakeCraftReloaded.get().getGameManager().addGame(new Game(arena));
        player.sendMessage(GREEN + "Arena \"" + arena.getName() + "\" enabled successfully.");
        return true;
    }

    private boolean disable(Player player, Arena arena) {
        QuakeCraftReloaded.get().getGameManager().removeGame(arena);
        player.sendMessage(GREEN + "The arena \"" + arena.getName() + "\" disabled successfully.");
        return true;
    }

    private String format(Location loc, boolean world) {
        return (world ? (loc.getWorld().getName() + ":") : "") + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public static ClickHandler nonArenaFilter(BiConsumer<Player, String> listener) {
        return (player, id) -> {
            Arena arena = QuakeCraftReloaded.get().getArenaManager().getArena(id);
            if (arena != null)
                return "Already taken";
            listener.accept(player, id);
            return null;
        };
    }
}
