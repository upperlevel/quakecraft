package xyz.upperlevel.quakecraft.powerup.effects;

import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

public interface PowerupEffect {
    String getId();

    ItemStack getDisplay();

    void apply(Participant participant);

    void load(Config config);
}
