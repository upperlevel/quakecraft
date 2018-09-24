package xyz.upperlevel.quakecraft.game;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.arena.Arena;
import xyz.upperlevel.quakecraft.events.GameJoinEvent;
import xyz.upperlevel.quakecraft.events.GameQuitEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.placeholder.PlaceholderRegistry;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;
import xyz.upperlevel.uppercore.placeholder.message.Message;
import xyz.upperlevel.uppercore.util.LocUtil;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quakecraft.get;

@Data
public class Game implements Listener {
    private static Message CANNOT_JOIN_MAX_REACHED;

    private final String id;
    private final Arena arena;
    private final PhaseManager phaseManager = new PhaseManager();
    private final Set<Player> players = new HashSet<>();
    private Map<Block, Sign> signs = new HashMap<>();

    @Getter
    private final PlaceholderRegistry placeholders;

    private Player winner;

    public Game(Arena arena) {
        this.id = arena.getId();
        this.arena = arena;
        this.placeholders = PlaceholderRegistry.create();
        fillPlaceholders(placeholders);
    }

    // load game constructor
    public Game(Arena arena, Config config) {
        this(arena);

        for (Location sign : config.getLocationList("signs")) {
            BlockState b_stat = sign.getBlock().getState();
            if (b_stat instanceof Sign)
                addSign((Sign) b_stat);
        }
    }

    public void fillPlaceholders(PlaceholderRegistry reg) {
        reg.set("game", arena::getId);
        reg.set("game_name", arena::getName);
        reg.set("game_min_players", () -> String.valueOf(getMinPlayers()));
        reg.set("game_max_players", () -> String.valueOf(getMinPlayers()));
        reg.set("game_players", () -> String.valueOf(players.size()));
        reg.set("game_winner", () -> getWinner() != null ? getWinner().getName() : "");
    }

    /**
     * Gets name of this arena.
     */
    public String getId() {
        return getArena().getId();
    }

    /**
     * Gets display name of this arena.
     */
    public String getName() {
        return getArena().getName();
    }

    /**
     * Gets minimum count of players of this arena.
     */
    public int getMinPlayers() {
        return getArena().getMinPlayers();
    }

    /**
     * Gets maximum count of players of this arena.
     */
    public int getMaxPlayers() {
        return getArena().getMaxPlayers();
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, get());
        getPhaseManager().setPhase(new LobbyPhase(this));
    }

    public void stop() {
        HandlerList.unregisterAll(this);
        getPhaseManager().setPhase(null);
    }

    public boolean addSign(Sign sign) {
        return signs.put(sign.getBlock(), sign) == null;
    }

    public boolean removeSign(Sign sign) {
        return signs.remove(sign.getBlock()) != null;
    }

    public Collection<Sign> getSigns() {
        return signs.values();
    }

    public void setSignLines(List<PlaceholderValue<String>> lines, PlaceholderRegistry<?> reg) {
        int limit = lines.size();
        if(limit > 4) {
            Quakecraft.get().getLogger().severe("Sign lines must be only of 4 elements!");
            limit = 4;
        }
        for (int i = 0; i < limit; i++) {
            String line = lines.get(i).resolve(null, reg);
            for (Sign sign : signs.values()) {
                sign.setLine(i, line);
            }
        }
        signs.values().forEach(Sign::update);
    }

    public boolean join(Player player) {
        if (players.add(player)) {
            Quakecraft.get().getPlayerManager().getPlayer(player).saveItems();
            player.setExp(0f);
            GameJoinEvent e = new GameJoinEvent(this, player);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                players.remove(player);
                if (e.getKickReason() != null)
                    player.sendMessage(e.getKickReason().toArray(new String[0]));
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isPlaying(Player player) {
        return players.contains(player);
    }

    public boolean leave(Player player) {
        if (players.remove(player)) {
            Bukkit.getPluginManager().callEvent(new GameQuitEvent(this, player));
            Location lobby = Quakecraft.get().getArenaManager().getLobby();
            if (lobby != null) {
                player.teleport(Quakecraft.get().getArenaManager().getLobby());
            } else {
                Quakecraft.get().getLogger().severe("global lobby location not set, use '/quake lobby set' to set the global lobby location");
            }
            Quakecraft.get().getPlayerManager().getPlayer(player).restoreItems();
            return true;
        }
        return false;
    }

    public void broadcast(String msg) {
        players.forEach(player -> player.sendMessage(msg));
    }

    public void broadcast(Message msg) {
        msg.broadcast(players, placeholders);
    }

    public void broadcast(Message msg, PlaceholderRegistry placeholders) {
        msg.broadcast(players, placeholders);
    }


    public Map<String, Object> save() {
        Map<String, Object> dt = new HashMap<>();
        dt.put("id", id);
        dt.put("signs", signs.keySet()
                .stream()
                .map(b -> LocUtil.serialize(b.getLocation()))
                .collect(Collectors.toList()));
        return dt;
    }


    public static void loadConfig() {
        CANNOT_JOIN_MAX_REACHED = Quakecraft.get().getMessages().get("game.cannot-join.max-players");
    }
}
