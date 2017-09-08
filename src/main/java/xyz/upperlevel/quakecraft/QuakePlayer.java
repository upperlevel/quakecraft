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
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.util.PlayerInventoryBackup;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.quakecraft.Quakecraft.get;

@Getter
@Setter
public class QuakePlayer {

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

    private PlayerInventoryBackup preJoinItems;

    public QuakePlayer(Player player) {
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
                selectedBarrel = (BarrelManager.Barrel) r.getManager("barrel").get(selected.getString("barrel"));
                selectedCase = (CaseManager.Case) r.getManager("case").get(selected.getString("case"));
                selectedLaser = (LaserManager.Laser) r.getManager("laser").get(selected.getString("laser"));
                selectedMuzzle = (MuzzleManager.Muzzle) r.getManager("muzzle").get(selected.getString("muzzle"));
                selectedTrigger = (TriggerManager.Trigger) r.getManager("trigger").get(selected.getString("trigger"));
            }
            // >>>>>>>>>> armor
            {
                PurchaseRegistry r = get().getShop().getArmors().getRegistry();
                selectedBoot = (BootManager.Boot) r.getManager("boot").get(selected.getString("boots"));
                selectedLegging = (LeggingManager.Legging) r.getManager("legging").get(selected.getString("leggings"));
                selectedChestplate = (ChestplateManager.Chestplate) r.getManager("chestplate").get(selected.getString("chestplate"));
                selectedHat = (HatManager.Hat) r.getManager("hat").get(selected.getString("hat"));
            }
            // >>>>>>>>>> killsound
            {
                PurchaseRegistry r = get().getShop().getKillSounds().getRegistry();
                selectedKillSound = (KillSoundManager.KillSound) r.getManager("kill_sound").get(selected.getString("kill_sound"));
            }
            // >>>>>>>>>> dash
            {
                PurchaseRegistry r = get().getShop().getDashes().getRegistry();
                selectedDashPower = (DashPowerManager.DashPower) r.getManager("dash_power").get(selected.getString("dash_power"));
                selectedDashCooldown = (DashCooldownManager.DashCooldown) r.getManager("dash_cooldown").get(selected.getString("dash_cooldown"));
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
            selected.put("boots", selectedBoot.getId());
        if (selectedLegging != null)
            selected.put("leggings", selectedLegging.getId());
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
       preJoinItems = new PlayerInventoryBackup(player);
    }

    public void restoreItems() {
        preJoinItems.restore(player);
        preJoinItems = null;
    }
}
