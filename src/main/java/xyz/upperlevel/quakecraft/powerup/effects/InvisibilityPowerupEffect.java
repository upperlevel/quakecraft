package xyz.upperlevel.quakecraft.powerup.effects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.upperlevel.quakecraft.phases.game.Gamer;

import java.util.HashMap;
import java.util.Map;

public class InvisibilityPowerupEffect extends TimeBasedPowerupEffect {
    private final Map<Player, ItemStack[]> armorByPlayer = new HashMap<>();

    public InvisibilityPowerupEffect() {
        super("invisibility");
    }

    @Override
    public void start(Gamer gamer) {
        Player player = gamer.getPlayer();

        armorByPlayer.put(player, player.getInventory().getArmorContents());
        player.getInventory().setArmorContents(null);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
    }

    @Override
    public void end(Gamer gamer) {
        Player player = gamer.getPlayer();
        player.getInventory().setArmorContents(armorByPlayer.remove(player));
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
