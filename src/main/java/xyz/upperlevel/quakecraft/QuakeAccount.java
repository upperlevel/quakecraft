package xyz.upperlevel.quakecraft;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.phases.game.GainNotifier;
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
import xyz.upperlevel.uppercore.storage.Table;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quake.get;
import static xyz.upperlevel.uppercore.storage.DuplicatePolicy.REPLACE;

public class QuakeAccount {

    public static Table playersTable;

    @Getter
    private final Player player;

    /* Game */

    @Getter
    @Setter
    public long kills, deaths;

    @Getter
    @Setter
    public long wonMatches, playedMatches;

    @Getter
    private Set<Purchase<?>> purchases = new HashSet<>();

    /* Gun */

    @Getter
    private BarrelManager.Barrel selectedBarrel;

    @Getter
    private CaseManager.Case selectedCase;

    @Getter
    private LaserManager.Laser selectedLaser;

    @Getter
    private MuzzleManager.Muzzle selectedMuzzle;

    @Getter
    private TriggerManager.Trigger selectedTrigger;

    @Getter
    private Railgun gun;

    /* Armor */

    @Getter
    @Setter
    private BootManager.Boot selectedBoot;

    @Getter
    @Setter
    private LeggingManager.Legging selectedLegging;

    @Getter
    @Setter
    private ChestplateManager.Chestplate selectedChestplate;

    @Getter
    @Setter
    private HatManager.Hat selectedHat;

    /* Others */

    @Getter
    @Setter
    private KillSoundManager.KillSound selectedKillSound;

    @Getter
    @Setter
    private DashPowerManager.DashPower selectedDashPower;

    @Getter
    @Setter
    private DashCooldownManager.DashCooldown selectedDashCooldown;

    @Getter
    private GainNotifier gainNotifier = new GainNotifier(this); // TODO why is it here!?
    // private PlayerBackup preJoinItems;

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
        if (manager == null) {
            throw new IllegalArgumentException("Cannot find purchase manager " + managerName);
        }
        String selectedId = in.getString(managerName);
        if (selectedId == null) {
            for (String alias : aliases) {
                selectedId = in.getString(alias);
            }
        }
        if (selectedId == null) {
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

        Config data = playersTable.element(player.getUniqueId().toString())
                .asConfig()
                .orElse(Config.empty());

        // ------------------ general
        kills = data.getLong("kills", 0);
        deaths = data.getLong("deaths", 0);

        wonMatches = data.getLong("won-matches", -1);
        if (wonMatches == -1) wonMatches = data.getLong("won_matches", -1);
        wonMatches = wonMatches != -1 ? wonMatches : 0L;

        playedMatches = data.getLong("played-matches", -1);
        if (playedMatches == -1) playedMatches = data.getLong("played_matches", -1);
        playedMatches = playedMatches != -1 ? playedMatches : 0;

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
                selectedKillSound = (KillSoundManager.KillSound) getPurchase(r, selected, "kill-sound", "kill_sound");
            }
            // >>>>>>>>>> dash
            {
                PurchaseRegistry r = get().getShop().getDashes().getRegistry();
                selectedDashPower = (DashPowerManager.DashPower) getPurchase(r, selected, "dash-power", "dash_power");
                selectedDashCooldown = (DashCooldownManager.DashCooldown) getPurchase(r, selected, "dash-cooldown", "dash_cooldown");
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
        data.put("won-matches", wonMatches);
        data.put("played-matches", playedMatches);
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
            selected.put("kill-sound", selectedKillSound.getId());
        // >>>>>>>>>>> dash
        if (selectedDashPower != null)
            selected.put("dash-power", selectedDashPower.getId());
        if (selectedDashCooldown != null)
            selected.put("dash-cooldown", selectedDashCooldown.getId());
        // ------------------
        data.put("selected", selected);

        // UPDATE DB
        playersTable.element(player.getUniqueId().toString()).insert(data, REPLACE);

        get().getLogger().info("It took " + (System.currentTimeMillis() - startedAt) + " ms to send data to db for player: \"" + player.getName() + "\"");
    }

    public static void loadTable() {
        playersTable = get().getRemoteDatabase().table("players");
        playersTable.create(); // Create if not already present
    }
}
