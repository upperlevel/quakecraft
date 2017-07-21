package xyz.upperlevel.spigot.quakecraft;

import lombok.Data;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.shop.Purchase;

import java.util.HashSet;
import java.util.Set;

@Data
public class QuakePlayer {

    private final Player player;

    public long kills, deaths;
    public long wonMatches, playedMatches;

    private final Set<Purchase<?>> purchases = new HashSet<>();

    public QuakePlayer(Player player) {
        this.player = player;
    }

    public void load() {
    }

    public void save() {
    }
}
