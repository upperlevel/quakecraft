package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.uppercore.config.Config;

@RequiredArgsConstructor
public abstract class BasePowerupEffect implements PowerupEffect {
    @Getter
    private final String id;
    @Getter
    @Setter
    private ItemStack display;

    @Override
    public void load(Config config) {
        this.display = config.getCustomItemRequired("display").resolve(null);
    }
}
