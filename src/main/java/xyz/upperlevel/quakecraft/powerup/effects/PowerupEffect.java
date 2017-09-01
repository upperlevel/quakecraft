package xyz.upperlevel.quakecraft.powerup.effects;

import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

import java.util.Collection;
import java.util.List;

public interface PowerupEffect {
    String getId();

    ItemStack getDisplay();

    void apply(Participant participant);

    void clear(Collection<Participant> players);

    void load(Config config);
}
