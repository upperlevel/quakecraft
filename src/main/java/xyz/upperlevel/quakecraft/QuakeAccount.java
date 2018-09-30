package xyz.upperlevel.quakecraft;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.game.gains.GainNotifier;
import xyz.upperlevel.quakecraft.shop.KillSoundManager;
import xyz.upperlevel.quakecraft.shop.ShopCategory;
import xyz.upperlevel.quakecraft.shop.armor.*;
import xyz.upperlevel.quakecraft.shop.dash.DashCategory;
import xyz.upperlevel.quakecraft.shop.dash.DashCooldownManager;
import xyz.upperlevel.quakecraft.shop.dash.DashPowerManager;
import xyz.upperlevel.quakecraft.shop.gun.*;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.util.PlayerBackup;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quake.get;

@Getter
@Setter
public class QuakeAccount {

    private final Player player;

    public long kills, deaths;
    public long wonMatches, playedMatches;

    private Set<Purchase<?>> purchases = new HashSet<>();

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

    private GainNotifier gainNotifier = new GainNotifier(this);

    private PlayerBackup preJoinItems;

    public QuakeAccount(Player player) {
        this.player = player;

        ShopCategory shop = get().getShop();

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

        initDefPurchases();
    }

    private void initDefPurchases() {
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
        gun = get().getShop().getGuns().getGuns().computeSelected(this);
    }

    public void setSelectedCase(CaseManager.Case gcase) {
        if (this.selectedCase != gcase) {
            this.selectedCase = gcase;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedLaser(LaserManager.Laser laser) {
        if (this.selectedLaser != laser) {
            this.selectedLaser = laser;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedBarrel(BarrelManager.Barrel barrel) {
        if (this.selectedBarrel != barrel) {
            this.selectedBarrel = barrel;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedMuzzle(MuzzleManager.Muzzle muzzle) {
        if (this.selectedMuzzle != muzzle) {
            this.selectedMuzzle = muzzle;
            onGunComponentSelectChange();
        }
    }

    public void setSelectedTrigger(TriggerManager.Trigger trigger) {
        if (this.selectedTrigger != trigger) {
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
        purchases.addAll(components);//You could call this even when components aren't purchased (ex. cost == 0)
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

    private Purchase<?> getPurchase(PurchaseRegistry reg, Config in, String managerName, String... aliases) {
        PurchaseManager<?> manager = reg.getManager(managerName);
        if (manager == null)
            throw new IllegalArgumentException("Cannot find purchase manager " + managerName);
        String selectedId = in.getString(managerName);
        if(selectedId == null) {
            for (String alias : aliases) {
                selectedId = in.getString(alias);
            }
        }
        if(selectedId == null) {
            return manager.getDefault();
        }
        Purchase<?> obj = manager.get(selectedId);
        if (obj == null) {
            Quake.get().getLogger().warning("Cannot find " + managerName + ": '" + selectedId + "', id changed?");
            return manager.getDefault();
        }
        return obj;
    }

    public void load() {
        long startedAt = System.currentTimeMillis();

        Config data = Config.wrap(get().getStore().connection()
                .database()
                .table("players")
                .document(player.getUniqueId().toString())
                .ask());
        // ------------------ general
        kills = data.getLong("kills", 0);
        deaths = data.getLong("deaths", 0);
        wonMatches = data.getLong("won_matches", 0);
        playedMatches = data.getLong("played_matches", 0);
        purchases.addAll(data.getStringList("purchases", new ArrayList<>())
                .stream()
                .map(id -> get().getShop().getRegistry().getPurchase(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        // ------------------ selected
        Config selected = data.getConfig("selected");
        if (selected != null) {
            // >>>>>>>>>> gun
            {
                PurchaseRegistry r = get().getShop().getGuns().getRegistry();
                selectedBarrel = (BarrelManager.Barrel) getPurchase(r, selected, "barrel");
                selectedCase = (CaseManager.Case) getPurchase(r, selected, "case");
                selectedLaser = (LaserManager.Laser) getPurchase(r, selected, "laser");
                selectedMuzzle = (MuzzleManager.Muzzle) getPurchase(r, selected, "muzzle");
                selectedTrigger = (TriggerManager.Trigger) getPurchase(r, selected, "trigger");
            }
            // >>>>>>>>>> armor
            {
                PurchaseRegistry r = get().getShop().getArmors().getRegistry();
                selectedBoot = (BootManager.Boot) getPurchase(r, selected, "boot", "boots");
                selectedLegging = (LeggingManager.Legging) getPurchase(r, selected, "legging", "leggings");
                selectedChestplate = (ChestplateManager.Chestplate) getPurchase(r, selected, "chestplate", "chestplates");
                selectedHat = (HatManager.Hat) getPurchase(r, selected, "hat", "hats");
            }
            // >>>>>>>>>> killsound
            {
                PurchaseRegistry r = get().getShop().getKillSounds().getRegistry();
                selectedKillSound = (KillSoundManager.KillSound) getPurchase(r, selected, "kill_sound");
            }
            // >>>>>>>>>> dash
            {
                PurchaseRegistry r = get().getShop().getDashes().getRegistry();
                selectedDashPower = (DashPowerManager.DashPower) getPurchase(r, selected, "dash_power");
                selectedDashCooldown = (DashCooldownManager.DashCooldown) getPurchase(r, selected, "dash_cooldown");
            }
        }

        get().getLogger().info("It took " + (System.currentTimeMillis() - startedAt) + " ms to load data from db for player: \"" + player.getName() + "\"");
    }

    public void save() {
        long startedAt = System.currentTimeMillis();

        // ------------------ general
        Map<String, Object> data = new HashMap<>();
        data.put("kills", kills);
        data.put("deaths", deaths);
        data.put("won_matches", wonMatches);
        data.put("played_matches", playedMatches);
        data.put("purchases", purchases.stream()
                .filter(Objects::nonNull)//Don't know why, sometimes the next line gives null pointers
                .map(Purchase::getFullId)
                .collect(Collectors.toList()));
        // ------------------ selected
        // >>>>>>>>>> gun
        Map<String, Object> selected = new HashMap<>();
        if (selectedBarrel != null)
            selected.put("barrel", selectedBarrel.getId());
        if (selectedCase != null)
            selected.put("case", selectedCase.getId());
        if (selectedLaser != null)
            selected.put("laser", selectedLaser.getId());
        if (selectedMuzzle != null)
            selected.put("muzzle", selectedMuzzle.getId());
        if (selectedTrigger != null)
            selected.put("trigger", selectedTrigger.getId());
        // >>>>>>>>>> armor
        if (selectedBoot != null)
            selected.put("boot", selectedBoot.getId());
        if (selectedLegging != null)
            selected.put("legging", selectedLegging.getId());
        if (selectedChestplate != null)
            selected.put("chestplate", selectedChestplate.getId());
        if (selectedHat != null)
            selected.put("hat", selectedHat.getId());
        // >>>>>>>>>>> killsound
        if (selectedKillSound != null)
            selected.put("kill_sound", selectedKillSound.getId());
        // >>>>>>>>>>> dash
        if (selectedDashPower != null)
            selected.put("dash_power", selectedDashPower.getId());
        if (selectedDashCooldown != null)
            selected.put("dash_cooldown", selectedDashCooldown.getId());
        // ------------------
        data.put("selected", selected);

        // UPDATE DB
        get().getStore().connection()
                .database()
                .table("players")
                .document(player.getUniqueId().toString())
                .send(data);

        get().getLogger().info("It took " + (System.currentTimeMillis() - startedAt) + " ms to send data to db for player: \"" + player.getName() + "\"");
    }

    public void saveItems() {
       preJoinItems = new PlayerBackup(player);
    }

    public void restoreItems() {
        preJoinItems.restore(player);
        preJoinItems = null;
    }
}
