package xyz.upperlevel.quakecraft.phases.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.powerup.PowerupEffectManager;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.PhaseManager;
import xyz.upperlevel.uppercore.arena.event.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent;
import xyz.upperlevel.uppercore.arena.event.ArenaQuitEvent.ArenaQuitReason;
import xyz.upperlevel.uppercore.arena.event.JoinSignUpdateEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.sound.PlaySound;
import xyz.upperlevel.uppercore.task.Countdown;
import xyz.upperlevel.uppercore.util.Dbg;

import java.util.*;

import static xyz.upperlevel.quakecraft.Quake.getProfileController;
import static xyz.upperlevel.uppercore.Uppercore.hotbars;


public class GamePhase extends PhaseManager {
    private static int gameCountdown;

    private static List<String> permittedCommands;
    private static Message cannotRunCommandDuringGame;

    private static GameBoard gameBoard;
    private static Message joinSign;
    private static Message startMessage;
    private static PlaySound startSound;

    private static boolean sneakDisabled;
    private static Message sneakDisabledMessage;

    public static GameHotbar hotbar;

    @Getter
    private final QuakeArena arena;

    private final Map<Player, Gamer> gamersByPlayer = new HashMap<>();
    private final List<Gamer> gamers = new ArrayList<>();

    private final Set<Player> spectators = new HashSet<>();

    @Getter
    private final PlaceholderRegistry<?> placeholders;

    private final Map<Player, BoardModel.Hook> boardByPlayer = new HashMap<>();

    @Getter
    private final Countdown countdown;

    private final Map<Player, Shot> shootings = new HashMap<>();
    private final CompassTargeter compassTargeter;

    public GamePhase(QuakeArena arena) {
        super("game");

        this.arena = arena;
        this.placeholders = PlaceholderRegistry.create(arena.getPlaceholders());
        buildPlaceholders();
        this.compassTargeter = new CompassTargeter(this);
        this.countdown = Countdown.create(
                gameCountdown * 20,
                20,
                tick -> {
                    updateBoards();
                    arena.updateJoinSigns();
                },
                () -> {
                    Player winner = gamers.get(0).getPlayer();
                    end(winner);
                    Dbg.p(String.format("[%s] Game countdown ended, the first is %s", arena.getName(), winner.getName()));
                }
        );
    }

    private void buildPlaceholders() {
        placeholders.set("ranking_name", (p, s) -> {
            if (s == null)
                return null;
            try {
                return gamers.get(Integer.parseInt(s) - 1).getPlayer().getName();
            } catch (Exception e) {
                return null;
            }
        });
        placeholders.set("ranking_kills", (p, s) -> {
            try {
                return String.valueOf(gamers.get(Integer.parseInt(s) - 1).getKills());
            } catch (Exception e) {
                return null;
            }
        });
        placeholders.set("ranking_gun", (p, s) -> {
            try {
                Profile player = getProfileController().getOrCreateProfile(gamers.get(Integer.parseInt(s) - 1).getPlayer());
                return player.getRailgun() == null ? Railgun.CUSTOM_NAME.resolve(p) : player.getRailgun().getName().resolve(p);
            } catch (Exception e) {
                return null;
            }
        });
        placeholders.set("game_countdown", () -> {
            String res = countdown.toString("mm:ss");
            //Dbg.pf("[%s] Placeholder countdown request: %s", arena.getName(), res);
            return res;
        });
    }

    /**
     * Sorts the gamers list based on the player that has the highest number of kills.
     */
    public void updateRanking() {
        gamers.sort((prev, next) -> (next.getKills() - prev.getKills()));
    }

    // This function is called by both Gamer & Spectator.
    private void setupPlayer(Player player) {
        BoardModel.Hook hooked = gameBoard.hook(new Board());
        boardByPlayer.put(player, hooked);
        hooked.open(player, placeholders);
    }

    public void updateBoards() {
        boardByPlayer.forEach((p, b) -> b.render(p, placeholders));
    }

    private void addGamer(Player player) {
        setupPlayer(player);

        Gamer g = new Gamer(this, player);
        gamersByPlayer.put(player, g);
        gamers.add(g);

        hotbars().view(player).addHotbar(hotbar);

        Profile profile = getProfileController().getOrCreateProfile(player);
        player.getInventory().setArmorContents(new ItemStack[]{ // Sets the account's selected armor.
                profile.getSelectedBoots().getItem().resolve(player),
                profile.getSelectedLeggings().getItem().resolve(player),
                profile.getSelectedChestplate().getItem().resolve(player),
                profile.getSelectedHat().getItem().resolve(player)
        });

        player.setGameMode(GameMode.ADVENTURE);
    }

    public boolean isGamer(Player player) {
        return gamersByPlayer.containsKey(player);
    }

    public Gamer getGamer(Player player) {
        return gamersByPlayer.get(player);
    }

    public List<Gamer> getGamers() {
        return Collections.unmodifiableList(gamers);
    }

    private void addSpectator(Player player) {
        setupPlayer(player);
        spectators.add(player);

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(arena.getSpawns().get(0));
        player.sendMessage("You're a spectator for the arena: " + arena.getName() + "!");
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public Set<Player> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }

    public void clearPlayer(Player player, boolean update) {
        boardByPlayer.remove(player);

        // Gamer
        Gamer g = gamersByPlayer.remove(player);
        if (g != null) {
            // Always tries to clear all powerup effects that could be possible applied.
            arena.getPowerups().forEach(powerup -> powerup.getEffect().clear(Collections.singleton(g)));

            gamers.remove(g);
            hotbars().view(player).removeHotbar(hotbar);

            if (update) {
                updateRanking();
                updateBoards();
            }
        }

        // Spectator
        spectators.remove(player);
    }

    public boolean isEnding() {
        return getPhase() instanceof EndingPhase;
    }

    public void end(Player winner) {
        countdown.cancel();
        setPhase(new EndingPhase(this, winner));
    }

    private void checkJustOneGamer() {
        if (gamers.size() == 1 && !isEnding()) {
            Player winner = gamers.get(0).getPlayer();
            Bukkit.getScheduler().runTask(
                    Quake.get(),
                    () -> end(winner)
            );
        }
    }

    private void checkNoGamers() {
        if (gamers.isEmpty() && !isEnding()) {
            new ArrayList<>(arena.getPlayers()).forEach(p -> arena.quit(p, ArenaQuitReason.ARENA_ABORT));
            arena.getPhaseManager().setPhase(new LobbyPhase(arena));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        super.onEnable(previous);

        // First thing done is to register all the present players as gamers.
        arena.getPlayers().forEach(this::addGamer);
        arena.updateJoinSigns();

        gamers.stream().map(Gamer::getPlayer).forEach(player -> {
            Profile profile = getProfileController().getOrCreateProfile(player);
            getProfileController().updateProfile(player.getUniqueId(), new Profile().setPlayedMatches(profile.getPlayedMatches() + 1));
        });

        updateBoards();
        compassTargeter.start();

        arena.getPlayers().forEach(p -> {
            startMessage.send(p, placeholders);
            startSound.play(p);
        });

        for (Powerup box : arena.getPowerups()) { // Spawns the power-ups when the game begins.
            box.onGameBegin(this);
        }

        // Teleports the gamer to the spawns, the assignment is round-robin.
        List<Location> spawns = arena.getSpawns();

        for (int i = 0; i < gamers.size(); i++) {
            Player p = gamers.get(i).getPlayer();
            p.teleport(spawns.get(i % spawns.size()));
        }

        countdown.start();

        checkJustOneGamer();
        checkNoGamers(); // How did it start!? Should never reach this state.
    }

    @Override
    public void onDisable(Phase next) {
        super.onDisable(next);

        countdown.cancel();
        compassTargeter.cancel();

        for (Powerup powerup : arena.getPowerups()) { // Removes the power-ups when the game ends.
            powerup.onGameEnd();
        }

        // Clears the power-ups effect from players.
        PowerupEffectManager.get().forEach(e -> e.clear(gamers));

        shootings.values().forEach(Shot::cancel); // Cancels all tasks related to shots.
        shootings.clear();

        arena.getPlayers().forEach(p -> clearPlayer(p, false));
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            Bukkit.getScheduler().runTask(Quake.get(), () -> addSpectator(p));
            Dbg.p(String.format("[%s] %s joined as a spectator", arena.getName(), p.getName()));
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            clearPlayer(e.getPlayer(), true);

            if (e.getReason() != ArenaQuitReason.ARENA_ABORT && !isEnding()) {
                checkJustOneGamer();
                checkNoGamers();
            }
        }
    }

    @EventHandler
    public void onJoinSignUpdate(JoinSignUpdateEvent e) {
        if (arena.equals(e.getArena())) {
            e.getJoinSigns().forEach(sign -> joinSign.setSign(sign, null, placeholders));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (arena.equals(Quake.getArena(e.getEntity()))) {
            e.setDeathMessage(null);
            e.setKeepInventory(true);
            e.getEntity().spigot().respawn();
            Gamer gamer = getGamer(e.getEntity());
            if (gamer != null) gamer.die();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;

        Gamer gamer = getGamer((Player) e.getEntity());
        if (gamer != null) {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                e.setCancelled(true);
                gamer.die();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        Gamer gamer = getGamer(p);
        if (gamer == null) return;

        // Before the player starts to get void damage kills him.
        // Apparently there's an issue with VOID damage calls as Spigot
        // could fire EntityDamageEvent (cause: VOID) many times even if
        // the player has been teleported in the first call. This would lead
        // the player to teleport around the Quake's spawns.
        double y = p.getLocation().getY();
        if (y < -10) {
            Dbg.pf("%s is falling (%.2f), killing him", gamer.getName(), y);
            gamer.die();
        }
    }

    private void onShoot(Player player) {
        if (!shootings.containsKey(player)) {
            Dbg.p(String.format("%s is shooting", player.getName()));

            Shot shot = new Shot(this, player);
            shootings.put(player, shot);
            shot.start();

            Gamer gamer = getGamer(player);

            player.setExp(1.0f);
            long firingDelay = (long) (getProfileController().getOrCreateProfile(player).getSelectedTrigger().getFiringSpeed() * gamer.getGunCooldownBase());
            Countdown.create(
                    firingDelay, 1,
                    tick -> player.setExp((float) tick / firingDelay),
                    () -> {
                        player.setExp(0.0f); // to be sure
                        shootings.remove(player);
                        Dbg.p(String.format("%s can shoot again", player.getName()));
                    }
            ).start();
        } else {
            Dbg.p(String.format("%s can't shoot, he's still recharging!", player.getName()));
        }
    }

    private void onDash(Player player) {
        Dbg.p(String.format("%s is dashing", player.getName()));
        Dash.swish(player);
    }

    public boolean goOnIfHasWon(Player player) {
        if (getGamer(player).getKills() >= arena.getKillsToWin()) {
            Dbg.p(String.format("Game ended, %s has > %d kills", player.getName(), arena.getKillsToWin()));
            end(player);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (arena.equals(Quake.getArena(p)) && !spectators.contains(p)) {
            // Checks if the holding item is at the same position where is supposed to be the gun.
            // We check the slot because we don't know the actual item that is generated after placeholder replacement.
            // TODO: find a way to have it and check for the item instead of the slot.
            if (p.getInventory().getHeldItemSlot() == hotbar.getGunSlot()) {
                // Right click: shoot
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    onShoot(p);
                }
                // Left click: dash
                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    onDash(p);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (sneakDisabled && arena.equals(Quake.getArena(e.getPlayer())) && !spectators.contains(e.getPlayer())) {
            e.setCancelled(true);
            sneakDisabledMessage.send(e.getPlayer(), placeholders);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String cmd = e.getMessage();
        if (getGamer(player) != null) {
            if (permittedCommands.stream().noneMatch(cmd::startsWith)) {
                cannotRunCommandDuringGame.send(player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Any player inside of the arena is absolutely denied to interact with its own inventory during game.
        if (arena.hasPlayer((Player) e.getWhoClicked()))
            e.setCancelled(true);
    }

    public static void loadConfig() {
        Config msg = Quake.getConfigSection("messages.game");

        Config config = Quake.getConfigSection("game");
        permittedCommands = config.getStringList("permitted-commands");
        cannotRunCommandDuringGame = msg.getMessage("cannot-run-command-during-game");

        gameCountdown = config.getIntRequired("duration");
        gameBoard = config.getRequired("game-board", GameBoard.class);
        startMessage = config.getMessage("start-message");
        startSound = config.getPlaySound("start-sound");
        sneakDisabled = config.getBoolRequired("sneak-disabled");
        sneakDisabledMessage = config.getMessageRequired("sneak-disabled-message");
        hotbar = config.getRequired("playing-hotbar", GameHotbar.class);
        joinSign = Quake.getConfigSection("join-signs").getMessage("playing");
    }
}
