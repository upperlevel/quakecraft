package xyz.upperlevel.spigot.quakecraft.game;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class GamePlayer {

    private final Player player;
    public int kills;
    public int deaths;

    public String getName() {
        return player.getName();
    }
}
