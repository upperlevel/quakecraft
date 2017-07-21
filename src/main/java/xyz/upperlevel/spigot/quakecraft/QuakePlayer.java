package xyz.upperlevel.spigot.quakecraft;

import lombok.Data;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.shop.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class QuakePlayer {

    private final Player player;

    public long kills, deaths;
    public long wonMatches, playedMatches;

    private final Set<Purchase<?>> purchases = new HashSet<>();

    private BarrelManager.Barrel selectedBarrel;
    private CaseManager.Case selectedCase;
    private LaserManager.Laser selectedLaser;
    private MuzzleManager.Muzzle selectedMuzzle;
    private TriggerManager.Trigger selectedTrigger;
    private GunManager.Gun selectedGun;

    public QuakePlayer(Player player) {
        this.player = player;

        ShopManager shop = QuakeCraftReloaded.get().getShopManager();

        selectedBarrel = shop.getBarrels().getDefault();
        selectedCase = shop.getCases().getDefault();
        selectedLaser = shop.getLasers().getDefault();
        selectedMuzzle = shop.getMuzzles().getDefault();
        selectedTrigger = shop.getTriggers().getDefault();
        selectedGun = shop.getGuns().getDefault();
    }

    public void load() {
    }

    public void save() {
    }
}
