package xyz.upperlevel.quakecraft.arena.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.wesjd.anvilgui.AnvilGUI.ClickHandler;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.quakecraft.game.Game;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.powerup.effects.PowerupEffect;
import xyz.upperlevel.uppercore.command.*;
import xyz.upperlevel.uppercore.command.Optional;
import xyz.upperlevel.uppercore.gui.*;

import java.util.*;
import java.util.function.BiConsumer;

import static org.bukkit.ChatColor.*;
import static xyz.upperlevel.uppercore.Uppercore.guis;
import static xyz.upperlevel.uppercore.gui.InputFilters.filterInt;
import static xyz.upperlevel.uppercore.gui.InputFilters.plain;
import static xyz.upperlevel.uppercore.util.LocUtil.format;

@WithPermission(value = "setupgui", desc = "Allows you to edit arenas from a GUI interface")
public class ArenaSetupGuiCommand extends Command {
    public ArenaSetupGuiCommand() {
        super("setupgui");
        setDescription("Setups the arena with a simple GUI interface.");
    }

    @Executor(sender = Sender.PLAYER)
    public void run(CommandSender sender, @Optional @Argument("arena") Arena arena) {
        Player player = (Player) sender;
        sender.sendMessage(GREEN + "Opening GUI.");
        guis().open(player, arena == null ? new ArenaSelectGui() : new ArenaEditGui(arena));
    }


    public static class ArenaSelectGui extends MenuGui {

        @Override
        public String buildTitle(int i) {
            return "Select Arena";
        }

        @Override
        public List<Icon> buildBody() {
            List<Arena> arenas = Quakecraft.get().getArenaManager().getArenas();
            List<Icon> res = new ArrayList<>(arenas.size());
            for (Arena arena : arenas) {
                res.add(Icon.of(
                        GuiUtil.itemStack(
                                Material.MONSTER_EGG,
                                AQUA + arena.getId(),
                                GRAY + "Id: " + AQUA + arena.getId(),
                                GRAY + "Name: " + AQUA + arena.getName(),
                                GRAY + "Spawns: " + AQUA + arena.getSpawns().size()

                        ),
                        p -> guis().open(p, new ArenaEditGui(arena)))
                );
            }
            return res;
        }

        @Override
        public List<Icon> buildFooter() {
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.wool(DyeColor.LIME, GOLD + "Add"),
                            p -> {
                                AnvilGui anvil = new AnvilGui();
                                anvil.setMessage("Id");
                                anvil.setListener(nonArenaFilter(
                                        (pl, id) -> {
                                            Arena arena = new Arena(id);
                                            Quakecraft.get().getArenaManager().addArena(arena);
                                            p.sendMessage(GREEN + "Arena added!");
                                            refreshAll();
                                            guis().back(pl);
                                        }
                                ));
                                guis().open(p, anvil);
                            }
                    ),
                    Icon.of(GuiUtil.itemStack(Material.ARROW, GOLD + "Back"), GuiAction.back())
            );
        }

        @Override
        public void onOpen(Player player) {
            super.onOpen(player);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class ArenaEditGui extends SimpleGui {
        @Getter
        private final Arena arena;

        @Override
        public String buildTitle() {
            return arena.getId() + "'s setup";
        }

        @Override
        public List<Icon> buildBody() {
            Location lobbyLoc = arena.getLobby();
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.wool(
                                    arena.isEnabled() ? DyeColor.LIME : DyeColor.RED,
                                    arena.isEnabled() ? RED + "Disable" : GREEN + "Enable"
                            ),
                            p -> {
                                boolean changed = arena.isEnabled() ? disable(p, arena) : enable(p, arena);
                                if (changed)
                                    refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.NAME_TAG,
                                    GOLD + "Name",
                                    AQUA + arena.getName()
                            ),
                            p -> {
                                AnvilGui gui = new AnvilGui();
                                gui.setMessage("name");
                                gui.setListener(plain((pl, n) -> {
                                    arena.setName(n);
                                    refreshAll();
                                    guis().back(p);
                                    pl.sendMessage(GREEN + "New arena name: '" + n + "'!");
                                }));
                                guis().open(p, gui);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.STONE_SLAB2,
                                    GOLD + "Min players",
                                    AQUA + (arena.getMinPlayers() > 0 ? String.valueOf(arena.getMinPlayers()) : (RED + "Not set"))
                            ),
                            p -> {
                                AnvilGui gui = new AnvilGui();
                                gui.setMessage("Min");
                                gui.setListener(filterInt(
                                        (pl, i) -> {
                                            arena.setMinPlayers(i);
                                            pl.sendMessage(GREEN + "Arena's min players: '" + i + "'!");
                                            refreshAll();
                                            guis().back(pl);
                                        },
                                        i -> i >= 2
                                ));
                                guis().open(p, gui);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.RED_SANDSTONE,
                                    GOLD + "Max players",
                                    AQUA + (arena.getMaxPlayers() > 0 ? String.valueOf(arena.getMaxPlayers()) : (RED + "Not set"))
                            ),
                            p -> {
                                AnvilGui gui = new AnvilGui();
                                gui.setMessage("Max");
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
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.MONSTER_EGG,
                                    GOLD + "Spawns",
                                    AQUA + String.valueOf(arena.getSpawns().size())
                            ),
                            p -> guis().open(p, new EditArenaSpawnsGui(arena))
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.WATCH,
                                    GOLD + "Lobby",
                                    lobbyLoc == null ? (RED + "Not set") : (AQUA + format(lobbyLoc, true))
                            ),
                            p -> {
                                if (lobbyLoc == null) {
                                    arena.setLobby(p.getLocation());
                                    p.sendMessage(GREEN + "lobby's new position: " + format(p.getLocation(), true) + "!");
                                    refreshAll();
                                } else
                                    guis().change(p, new EditArenaLobbyGui(arena));
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.GOLD_SWORD,
                                    GOLD + "Kills to win",
                                    AQUA + (arena.getKillsToWin() > 0 ? String.valueOf(arena.getKillsToWin()) : (RED + "Not set"))
                            ),
                            p -> {
                                AnvilGui gui = new AnvilGui();
                                gui.setMessage("Kills");
                                gui.setListener(filterInt(
                                        (pl, i) -> {
                                            arena.setKillsToWin(i);
                                            pl.sendMessage(GREEN + "Arena's kills to win: '" + i + "'!");
                                            refreshAll();
                                            guis().back(pl);
                                        },
                                        i -> i >= 2
                                ));
                                guis().open(p, gui);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.TRAPPED_CHEST,
                                    GOLD + "Powerups",
                                    AQUA + String.valueOf(arena.getPowerups().size())
                            ),
                            p -> guis().open(p, new EditArenaPowerupsGui(arena))
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.LEASH,
                                    (arena.isSneakEnabled() ? GREEN : RED) + "Sneaking",
                                    (arena.isSneakEnabled() ? GREEN + "Enabled" : RED + "Disabled")
                            ),
                            p -> {
                                arena.setSneakEnabled(!arena.isSneakEnabled());
                                refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.NAME_TAG,
                                    (arena.isHideNametags() ? RED : GREEN) + "Nametags",
                                    (arena.isHideNametags() ? RED + "Hidden" : GREEN + "Shown")
                            ),
                            p -> {
                                arena.setHideNametags(!arena.isHideNametags());
                                refreshAll();
                            }
                    )
            );
        }

        @Override
        public List<Icon> buildFooter() {
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.itemStack(Material.BARRIER, GOLD + "Remove"),
                            p -> {//TODO add confirm
                                if (arena.isEnabled()) {
                                    p.sendMessage(RED + "Disable the arena before removing it!");
                                    return;
                                }
                                Quakecraft.get().getArenaManager().removeArena(arena.getId());
                                p.sendMessage(GREEN + "Arena '" + arena.getId() + "' removed!");
                                guis().back(p);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, GOLD + "Back"),
                            GuiAction.back()
                    )
            );
        }

        private boolean enable(Player player, Arena arena) {
            if (!arena.isReady()) {
                player.sendMessage(RED + "The arena \"" + arena.getId() + "\" is not ready.");
                return false;
            }
            Quakecraft.get().getGameManager().addGame(new Game(arena));
            player.sendMessage(GREEN + "Arena \"" + arena.getId() + "\" enabled successfully.");
            return true;
        }

        private boolean disable(Player player, Arena arena) {
            Quakecraft.get().getGameManager().removeGame(arena);
            player.sendMessage(GREEN + "The arena \"" + arena.getId() + "\" disabled successfully.");
            return true;
        }

        @Override
        public void onOpen(Player p) {
            super.onOpen(p);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaSpawnsGui extends MenuGui {
        @Getter
        private final Arena arena;

        @Override
        public String buildTitle(int i) {
            return arena.getId() + "'s spawns";
        }

        @Override
        public List<Icon> buildBody() {
            List<Location> locs = arena.getSpawns();
            List<Icon> res = Arrays.asList(new Icon[locs.size()]);

            boolean sameWorld = true;
            if (locs.size() > 0) {
                World w = locs.get(0).getWorld();
                for (int i = locs.size() - 1; i >= 1; i--) {
                    if (w != locs.get(i).getWorld()) {
                        sameWorld = false;
                        break;
                    }
                }
            }

            int i = 0;
            for(Location loc : locs) {
                final int index = i;
                res.set(
                        i,
                        Icon.of(
                                GuiUtil.itemStack(
                                        Material.MONSTER_EGG,
                                        GOLD + "Spawn " + (i + 1),
                                        String.valueOf(AQUA) + format(loc, !sameWorld)
                                ),
                                p -> guis().open(p, new EditArenaSpawnGui(locs, index))
                        )
                );
                i++;
            }

            return res;
        }

        @Override
        public List<Icon> buildFooter() {
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.wool(DyeColor.GREEN, GOLD + "Add"),
                            p -> {
                                arena.getSpawns().add(p.getLocation());
                                p.sendMessage(GREEN + "Spawn added to arena!");
                                refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, "Back"),
                            GuiAction.back()
                    )
            );
        }

        @Override
        public void onOpen(Player p) {
            super.onOpen(p);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaSpawnGui extends SimpleGui {
        private final List<Location> locs;
        private final int index;

        @Override
        public String buildTitle() {
            return "Spawn editor";
        }

        @Override
        public List<Icon> buildBody() {
            Location loc = locs.get(index);
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.SADDLE,
                                    GOLD + "Teleport",
                                    AQUA + "To " + format(loc, true)),
                            p -> {
                                guis().close(p);
                                p.sendMessage(AQUA + "Teleporting...");
                                p.teleport(loc);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.STICK,
                                    GOLD + "Edit"
                            ),
                            p -> {//TODO add confirm
                                locs.set(index, p.getLocation());
                                p.sendMessage(GREEN + "Spawn " + (index + 1) + " new position: " + format(p.getLocation(), true) + "!");
                                refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(Material.BARRIER, GOLD + "Remove"),
                            p -> {//TODO add confirm
                                locs.remove(index);
                                p.sendMessage(GREEN + "Spawn " + (index + 1) + " removed!");
                                guis().back(p);
                            }
                    )
            );
        }

        @Override
        public List<Icon> buildFooter() {
            return Collections.singletonList(
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, GOLD + "Back"),
                            GuiAction.back()
                    )
            );
        }

        @Override
        public void onOpen(Player p) {
            super.onOpen(p);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaLobbyGui extends SimpleGui {
        @Getter
        private final Arena arena;

        @Override
        public String buildTitle() {
            return "Lobby editor";
        }

        @Override
        public List<Icon> buildBody() {
            Location loc = arena.getLobby();
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.SADDLE,
                                    GOLD + "Teleport",
                                    AQUA + "To " + format(loc, true)),
                            p -> {
                                guis().close(p);
                                p.sendMessage(AQUA + "Teleporting...");
                                p.teleport(loc);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(

                                    Material.STICK,
                                    GOLD + "Edit"
                            ),
                            p -> {//TODO add confirm
                                arena.setLobby(p.getLocation());
                                p.sendMessage(GREEN + "lobby's new position: " + format(p.getLocation(), true) + "!");
                                refreshAll();
                            }
                    )
            );
        }

        @Override
        public List<Icon> buildFooter() {
            return Collections.singletonList(
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, "Back"),
                            GuiAction.back()
                    )
            );
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaPowerupsGui extends MenuGui {
        @Getter
        private final Arena arena;

        @Override
        public String buildTitle(int i) {
            return arena.getId() + "'s powerups";
        }

        @Override
        public List<Icon> buildBody() {
            List<Powerup> boxes = arena.getPowerups();
            List<Icon> res = Arrays.asList(new Icon[boxes.size()]);

            boolean sameWorld = true;
            if (boxes.size() > 0) {
                World w = boxes.get(0).getLocation().getWorld();
                for (int i = boxes.size() - 1; i >= 1; i--) {
                    if (w != boxes.get(i).getLocation().getWorld()) {
                        sameWorld = false;
                        break;
                    }
                }
            }

            int i = 0;
            for (Powerup box : boxes) {
                final int index = i;
                res.set(
                        i,
                        Icon.of(
                                GuiUtil.itemStack(
                                        box.getEffect().getDisplay().getType(),
                                        GOLD + "Powerup " + (i + 1),
                                        AQUA + "loc: " + format(box.getLocation(), !sameWorld),
                                        AQUA + "type: " + box.getEffect().getId(),
                                        AQUA + "respawn: " + box.getRespawnTicks()
                                ),
                                p -> guis().open(p, new EditArenaPowerup(boxes, index))
                        )
                );
                i++;
            }
            return res;
        }


        @Override
        public List<Icon> buildFooter() {
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.wool(DyeColor.GREEN, GOLD + "Add"),
                            p -> {
                                arena.getPowerups().add(new Powerup(arena, p.getLocation(), PowerupEffectManager.getDef(), 300));
                                p.sendMessage(GREEN + "Powerup added to arena!");
                                refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, "Back"),
                            GuiAction.back()
                    )
            );
        }

        @Override
        public void onOpen(Player p) {
            super.onOpen(p);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaPowerup extends SimpleGui {
        @Getter
        private final List<Powerup> boxes;
        @Getter
        private final int index;

        @Override
        public String buildTitle() {
            return "Powerup editor";
        }

        @Override
        public List<Icon> buildBody() {
            Powerup box = boxes.get(index);
            return Arrays.asList(
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.SADDLE,
                                    GOLD + "Teleport",
                                    AQUA + "To " + format(box.getLocation(), true)),
                            p -> {
                                guis().close(p);
                                p.sendMessage(AQUA + "Teleporting...");
                                p.teleport(box.getLocation());
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.STICK,
                                    GOLD + "Edit"
                            ),
                            p -> {//TODO add confirm
                                box.setLocation(p.getLocation());
                                p.sendMessage(GREEN + "Powerup " + (index + 1) + " new position: " + format(p.getLocation(), true) + "!");
                                refreshAll();
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    box.getEffect().getDisplay().getType(),
                                    GOLD + "Effect type",
                                    AQUA + box.getEffect().getId()
                            ),
                            p -> guis().open(p, new EditArenaPowerupEffectGui(box))
                    ),
                    Icon.of(
                            GuiUtil.itemStack(
                                    Material.WATCH,
                                    GOLD + "Respawn ticks",
                                    String.valueOf(AQUA) + box.getRespawnTicks() + " ticks"
                            ),
                            p -> {
                                AnvilGui gui = new AnvilGui();
                                gui.setMessage("ticks");
                                gui.setListener(filterInt(
                                        (pl, in) -> {
                                            box.setRespawnTicks(in);
                                            guis().back(pl);
                                        },
                                        i -> i > 0
                                ));
                                guis().open(p, gui);
                            }
                    ),
                    Icon.of(
                            GuiUtil.itemStack(Material.BARRIER, GOLD + "Remove"),
                            p -> {//TODO add confirm
                                boxes.remove(index);
                                p.sendMessage(GREEN + "Powerup " + (index + 1) + " removed!");
                                guis().back(p);
                            }
                    )
            );
        }

        @Override
        public List<Icon> buildFooter() {
            return Collections.singletonList(
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, GOLD + "Back"),
                            GuiAction.back()
                    )
            );
        }

        @Override
        public void onOpen(Player p) {
            super.onOpen(p);
            refreshAll();
        }
    }

    @RequiredArgsConstructor
    public static class EditArenaPowerupEffectGui extends MenuGui {
        @Getter
        private final Powerup powerup;

        @Override
        public String buildTitle(int i) {
            return "new Powerup effect";
        }

        @Override
        public List<Icon> buildBody() {
            Collection<PowerupEffect> effects = PowerupEffectManager.get();
            List<Icon> res = Arrays.asList(new Icon[effects.size()]);

            int i = 0;
            for (PowerupEffect effect : effects) {
                res.set(
                        i,
                        Icon.of(
                                GuiUtil.setNameAndLores(
                                        effect.getDisplay().clone(),
                                        effect.getId()
                                ),
                                p -> {
                                    powerup.setEffect(effect);
                                    guis().back(p);
                                }
                        )
                );
                i++;
            }
            return res;
        }

        @Override
        public List<Icon> buildFooter() {
            return Collections.singletonList(
                    Icon.of(
                            GuiUtil.itemStack(Material.ARROW, GOLD + "Back"),
                            GuiAction.back()
                    )
            );
        }
    }

    public static ClickHandler nonArenaFilter(BiConsumer<Player, String> listener) {
        return (player, id) -> {
            if (!Arena.isValidName(id))
                return "Invalid id";
            Arena arena = Quakecraft.get().getArenaManager().getArena(id);
            if (arena != null)
                return "Already taken";
            listener.accept(player, id);
            return null;
        };
    }
}
