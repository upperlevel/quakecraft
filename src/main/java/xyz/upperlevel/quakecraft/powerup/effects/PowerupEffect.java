package xyz.upperlevel.quakecraft.powerup.effects;

import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.phases.game.Gamer;
import xyz.upperlevel.uppercore.config.Config;

import java.util.Collection;

public interface PowerupEffect {
    String getId();

    ItemStack getDisplay();

    void apply(Gamer participant);

    void clear(Collection<Gamer> players);

    void load(Config config);
}
