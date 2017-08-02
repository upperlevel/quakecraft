package xyz.upperlevel.spigot.quakecraft;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.upperlevel.spigot.quakecraft.shop.KillSoundManager;
import xyz.upperlevel.spigot.quakecraft.shop.ShopCategory;
import xyz.upperlevel.spigot.quakecraft.shop.armor.*;
import xyz.upperlevel.spigot.quakecraft.shop.dash.DashCategory;
import xyz.upperlevel.spigot.quakecraft.shop.dash.DashCooldownManager;
import xyz.upperlevel.spigot.quakecraft.shop.dash.DashPowerManager;
import xyz.upperlevel.spigot.quakecraft.shop.gun.*;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.railgun.Railgun;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
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
    private Railgun gun;

    private BootManager.Boot selectedBoot;
    private LeggingManager.Legging selectedLegging;
    private ChestplateManager.Chestplate selectedChestplate;
    private HatManager.Hat selectedHat;

    private KillSoundManager.KillSound selectedKillSound;

    private DashPowerManager.DashPower selectedDashPower;
    private DashCooldownManager.DashCooldown selectedDashCooldown;
    
    public QuakePlayer(Player player) {
        this.player = player;

        ShopCategory shop = QuakeCraftReloaded.get().getShop();

        GunCategory guns = shop.getGuns();

        selectedBarrel = guns.getBarrels().getDefault();
        selectedCase = guns.getCases().getDefault();
        selectedLaser = guns.getLasers().getDefault();
        selectedMuzzle = guns.getMuzzles().getDefault();
        selectedTrigger = guns.getTriggers().getDefault();
        gun = guns.getGuns().computeSelected(this);

        ArmorCategory armors = shop.getArmors();

        selectedBoot = armors.getBoots().getDefault();
        selectedLegging = armors.getLeggings().getDefault();
        selectedChestplate = armors.getChestplates().getDefault();
        selectedHat = armors.getHats().getDefault();

        selectedKillSound = shop.getKillSounds().getDefault();

        DashCategory dash = shop.getDashes();

        selectedDashPower = dash.getPower().getDefault();
        selectedDashCooldown = dash.getCooldown().getDefault();

        purchases.addAll(Arrays.asList(
                selectedCase,
                selectedLaser,
                selectedBarrel,
                selectedMuzzle,
                selectedTrigger,

                selectedBoot,
                selectedLegging,
                selectedChestplate,
                selectedHat,

                selectedKillSound,

                selectedDashPower,
                selectedDashCooldown
        ));
    }

    protected void onGunComponentSelectChange() {
        gun = QuakeCraftReloaded.get().getShop().getGuns().getGuns().computeSelected(this);
    }

    public void setSelectedCase(CaseManager.Case gcase) {
        if(this.selectedCase != gcase) {
            this.selectedCase = gcase;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedLaser(LaserManager.Laser laser) {
        if(this.selectedLaser != laser) {
            this.selectedLaser = laser;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedBarrel(BarrelManager.Barrel barrel) {
        if(this.selectedBarrel != barrel) {
            this.selectedBarrel = barrel;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedMuzzle(MuzzleManager.Muzzle muzzle) {
        if(this.selectedMuzzle != muzzle) {
            this.selectedMuzzle = muzzle;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedTrigger(TriggerManager.Trigger trigger) {
        if(this.selectedTrigger != trigger) {
            this.selectedTrigger = trigger;
            onGunComponentSelectChange();
        }
    }

    public void setGunComponents(List<? extends Purchase<?>> components) {
        this.selectedCase = (CaseManager.Case) components.get(0);
        this.selectedLaser = (LaserManager.Laser) components.get(1);
        this.selectedBarrel = (BarrelManager.Barrel) components.get(2);
        this.selectedMuzzle = (MuzzleManager.Muzzle) components.get(3);
        this.selectedTrigger = (TriggerManager.Trigger) components.get(4);
        onGunComponentSelectChange();
    }

    public List<? extends Purchase<?>> getGunComponents() {
        return Arrays.asList(
                selectedCase,
                selectedLaser,
                selectedBarrel,
                selectedMuzzle,
                selectedTrigger
        );
    }

    public void load() {
    }

    public void save() {
    }
}
