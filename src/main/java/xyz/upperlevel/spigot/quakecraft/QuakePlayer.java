package xyz.upperlevel.spigot.quakecraft;

import lombok.Data;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.shop.*;
import xyz.upperlevel.spigot.quakecraft.shop.armor.*;
import xyz.upperlevel.spigot.quakecraft.shop.gun.*;

import java.util.Arrays;
import java.util.HashSet;
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

    private BootManager.Boot selectedBoot;
    private LeggingManager.Legging selectedLegging;
    private ChestplateManager.Chestplate selectedChestplate;
    private HatManager.Hat selectedHat;
    
    public QuakePlayer(Player player) {
        this.player = player;

        ShopCategory shop = QuakeCraftReloaded.get().getShop();

        GunCategory guns = shop.getGuns();

        selectedBarrel = guns.getBarrels().getDefault();
        selectedCase = guns.getCases().getDefault();
        selectedLaser = guns.getLasers().getDefault();
        selectedMuzzle = guns.getMuzzles().getDefault();
        selectedTrigger = guns.getTriggers().getDefault();
        selectedGun = guns.getGuns().getDefault();

        ArmorCategory armors = shop.getArmors();

        selectedBoot = armors.getBoots().getDefault();
        selectedLegging = armors.getLeggings().getDefault();
        selectedChestplate = armors.getChestplates().getDefault();
        selectedHat = armors.getHats().getDefault();

        purchases.addAll(Arrays.asList(
                selectedBarrel,
                selectedCase,
                selectedLaser,
                selectedMuzzle,
                selectedTrigger,
                selectedGun,

                selectedBoot,
                selectedLegging,
                selectedChestplate,
                selectedHat
        ));
    }

    public void load() {
    }

    public void save() {
    }
}
