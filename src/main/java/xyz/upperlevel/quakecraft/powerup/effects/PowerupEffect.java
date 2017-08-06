package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

public interface PowerupEffect {
    String getId();

    ItemStack getDisplay();

    void apply(Participant participant);

    void load(Config config);
}
