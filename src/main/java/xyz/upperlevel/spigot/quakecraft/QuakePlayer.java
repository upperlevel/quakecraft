package xyz.upperlevel.spigot.quakecraft;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class QuakePlayer {

    private final Player player;

    public long kills, deaths;
    public long wonMatches, playedMatches;

    public QuakePlayer(Player player) {
        this.player = player;
    }

    public void load() {
    }

    public void save() {
    }
}
