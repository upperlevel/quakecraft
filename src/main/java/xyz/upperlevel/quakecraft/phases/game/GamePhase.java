package xyz.upperlevel.quakecraft.phases.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.arena.QuakeArena;
import xyz.upperlevel.quakecraft.phases.lobby.LobbyPhase;
import xyz.upperlevel.quakecraft.powerup.Powerup;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.arena.Phase;
import xyz.upperlevel.uppercore.arena.events.ArenaJoinEvent;
import xyz.upperlevel.uppercore.arena.events.ArenaQuitEvent;
import xyz.upperlevel.uppercore.board.Board;
import xyz.upperlevel.uppercore.board.BoardModel;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.sound.PlaySound;
import xyz.upperlevel.uppercore.task.Countdown;
import xyz.upperlevel.uppercore.util.Dbg;

import java.util.*;

import static xyz.upperlevel.uppercore.Uppercore.hotbars;


public class GamePhase extends Phase {
    private static int gameCountdown;

    private static GameBoard gameBoard;
    private static Message startMessage;
    private static PlaySound startSound;

    private static String defaultKillMessage;
    private static Message shotMessage;
    private static Message headshotMessage;
    private static boolean sneakDisabled;
    private static Message sneakDisabledMessage;
    private static int killsToWin;

    private static GameHotbar hotbar;
    private static int gunSlot;

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
                },
                () -> {
                    Player winner = gamers.get(0).getPlayer();
                    getPhaseManager().setPhase(new EndingPhase(this, winner));
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
                QuakeAccount player = Quake.get().getPlayerManager().getAccount(gamers.get(Integer.parseInt(s) - 1).getPlayer());
                return player.getGun() == null ? Railgun.CUSTOM_NAME.resolve(p) : player.getGun().getName().resolve(p);
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

        QuakeAccount account = Quake.getAccount(player);
        player.getInventory().setArmorContents(new ItemStack[]{ // Sets the account's selected armor.
                account.getSelectedBoot().getItem().resolve(player),
                account.getSelectedLegging().getItem().resolve(player),
                account.getSelectedChestplate().getItem().resolve(player),
                account.getSelectedHat().getItem().resolve(player)
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

        // TODO setup spectator
        player.sendMessage("You're a spectator for the arena: " + arena.getName() + "!");
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public Set<Player> getSpectators() {
        return Collections.unmodifiableSet(spectators);
    }

    public void onPlayerQuit(Player player) {
        // Gamer
        Gamer g = gamersByPlayer.remove(player);
        if (g != null) {
            gamers.remove(g);
            hotbars().view(player).removeHotbar(hotbar);

            Dbg.p(String.format("[%s] The gamer %s left the game", arena.getName(), player.getName()));
        }

        // Spectator
        if (spectators.remove(player)) {
            Dbg.p(String.format("[%s] The spectator %s left the game", arena.getName(), player.getName()));
        }
    }

    @Override
    public void onEnable(Phase previous) {
        super.onEnable(previous);

        // First thing done is to register all the present players as gamers.
        arena.getPlayers().forEach(this::addGamer);

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
        arena.getPowerups()
                .stream()
                .map(Powerup::getEffect)
                .distinct()
                .forEach(e -> e.clear(gamers));

        shootings.values().forEach(Shot::cancel); // Cancels all tasks related to shots.
        shootings.clear();
    }

    @EventHandler
    public void onArenaJoin(ArenaJoinEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            addSpectator(p);
            Dbg.p(String.format("[%s] %s joined as a spectator", arena.getName(), p.getName()));
        }
    }

    @EventHandler
    public void onArenaQuit(ArenaQuitEvent e) {
        if (arena.equals(e.getArena())) {
            Player p = e.getPlayer();
            onPlayerQuit(p);

            if (gamers.size() == 1) {
                Dbg.p(String.format("[%s] The player %s is the only player left, he won!", arena.getName(), p.getName()));

                // TODO switch to winning state
            }

            if (gamers.isEmpty()) { // If the arena is now empty, just restarts.
                Dbg.p(String.format("[%s] Was left empty without players", arena.getName()));
                getPhaseManager().setPhase(new LobbyPhase(arena));
            }
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

        if (gamer != null && e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            e.setCancelled(true);
            gamer.die();
        }
    }

    private void onShoot(Player player) {
        if (!shootings.containsKey(player)) {
            Dbg.p(String.format("%s is shooting", player.getName()));

            Shot shot = new Shot(this, player);
            shootings.put(player, shot);
            shot.start();

            player.setExp(1.0f);
            long firingDelay = (long) Quake.getAccount(player).getSelectedTrigger().getFiringSpeed();
            Countdown.create(
                    firingDelay, 1,
                    tick -> player.setExp((float) (tick / firingDelay)),
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
        if (getGamer(player).getKills() >= killsToWin) {
            Dbg.p(String.format("Game ended, %s has > %d kills", player.getName(), killsToWin));
            getPhaseManager().setPhase(new EndingPhase(this, player));
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (arena.equals(Quake.getArena(p))) {
            if (p.getInventory().getHeldItemSlot() == gunSlot) { // Checks if the player is holding the gun item by slot (anything better).
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
        if (arena.equals(Quake.getArena(e.getPlayer())) && sneakDisabled) {
            e.setCancelled(true);
            sneakDisabledMessage.send(e.getPlayer(), placeholders);
        }
    }

    public static void loadConfig() {
        Config config = Quake.getConfigSection("game");
        gameCountdown = config.getIntRequired("duration");
        gameBoard = config.getRequired("game-board", GameBoard.class);
        startMessage = config.getMessage("start-message");
        startSound = config.getPlaySound("start-sound");
        sneakDisabled = config.getBoolRequired("sneak-disabled");
        sneakDisabledMessage = config.getMessageRequired("sneak-disabled-message");
        killsToWin = config.getIntRequired("kills-to-win");
        hotbar = config.getRequired("playing-hotbar", GameHotbar.class);
    }
}
