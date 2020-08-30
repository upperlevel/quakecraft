package xyz.upperlevel.quakecraft.profile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.shop.KillSoundManager;
import xyz.upperlevel.quakecraft.shop.armor.*;
import xyz.upperlevel.quakecraft.shop.dash.DashCategory;
import xyz.upperlevel.quakecraft.shop.dash.DashCooldownManager;
import xyz.upperlevel.quakecraft.shop.dash.DashPowerManager;
import xyz.upperlevel.quakecraft.shop.gun.*;
import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseManager;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.Uppercore;

import java.io.StringWriter;
import java.util.*;

public class Profile {
    public final Map<String, Object> data;

    public Profile() {
        this(new HashMap<>());
    }

    public Profile(Map<String, Object> data) {
        this.data = data;
    }

    public UUID getId() {
        return UUID.fromString((String) this.data.get("id"));
    }

    public Profile setId(UUID id) {
        this.data.put("id", id.toString());
        return this;
    }

    public String getName() {
        return (String) this.data.get("name");
    }

    public Profile setName(String name) {
        this.data.put("name", name);
        return this;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getId());
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getId());
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        new Yaml().dump(this.data, writer);
        return writer.toString();
    }

    public Purchase<?> getSelectedPurchase(PurchaseManager<?> manager, String key) {
        if (!data.containsKey(key)) {
            // The searched purchase wasn't stored yet, choosing the default purchase.
            return manager.getDefault();
        }
        String id = (String) data.get(key);
        Purchase<?> selected = manager.get(id);
        if (selected == null) {
            Uppercore.logger().warning(String.format("Purchase not found for the manager '%s': %s", manager.getPurchaseName(), id));
            return manager.getDefault();
        }
        return selected;
    }

    // ------------------------------------------------------------------------------------------------ Stats

    public int getKills() {
        return (Integer) this.data.get("kills");
    }

    public Profile setKills(int kills) {
        this.data.put("kills", kills);
        return this;
    }

    public int getDeaths() {
        return (Integer) this.data.get("deaths");
    }

    public Profile setDeaths(int deaths) {
        this.data.put("deaths", deaths);
        return this;
    }

    public int getWonMatches() {
        return (Integer) this.data.get("won_matches");
    }

    public Profile setWonMatches(int wonMatches) {
        this.data.put("won_matches", wonMatches);
        return this;
    }

    public int getPlayedMatches() {
        return (int) this.data.get("played_matches");
    }

    public Profile setPlayedMatches(int playedMatches) {
        this.data.put("played_matches", playedMatches);
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Gun

    public GunCategory getGunCategory() {
        return Quake.get().getShop().getGuns();
    }

    public BarrelManager.Barrel getSelectedBarrel() {
        return (BarrelManager.Barrel) getSelectedPurchase(getGunCategory().getBarrels(), "selected_barrel");
    }

    public Profile setSelectedBarrel(BarrelManager.Barrel barrel) {
        this.data.put("selected_barrel", barrel.getId());
        return this;
    }

    public CaseManager.Case getSelectedCase() {
        return (CaseManager.Case) getSelectedPurchase(getGunCategory().getCases(), "selected_case");
    }

    public Profile setSelectedCase(CaseManager.Case _case) {
        this.data.put("selected_case", _case.getId());
        return this;
    }

    public LaserManager.Laser getSelectedLaser() {
        return (LaserManager.Laser) getSelectedPurchase(getGunCategory().getLasers(), "selected_laser");
    }

    public Profile setSelectedLaser(LaserManager.Laser laser) {
        this.data.put("selected_laser", laser.getId());
        return this;
    }

    public MuzzleManager.Muzzle getSelectedMuzzle() {
        return (MuzzleManager.Muzzle) getSelectedPurchase(getGunCategory().getMuzzles(), "selected_muzzle");
    }

    public Profile setSelectedMuzzle(MuzzleManager.Muzzle muzzle) {
        this.data.put("selected_muzzle", muzzle.getId());
        return this;
    }

    public TriggerManager.Trigger getSelectedTrigger() {
        return (TriggerManager.Trigger) getSelectedPurchase(getGunCategory().getTriggers(), "selected_trigger");
    }

    public Profile setSelectedTrigger(TriggerManager.Trigger trigger) {
        this.data.put("selected_trigger", trigger.getId());
        return this;
    }

    public Railgun getRailgun() {
        return getGunCategory().getGuns().computeSelected(this);
    }

    public Profile setRailgun(List<? extends Purchase<?>> components) {
        setSelectedCase((CaseManager.Case) components.get(0));
        setSelectedLaser((LaserManager.Laser) components.get(1));
        setSelectedBarrel((BarrelManager.Barrel) components.get(2));
        setSelectedMuzzle((MuzzleManager.Muzzle) components.get(3));
        setSelectedTrigger((TriggerManager.Trigger) components.get(4));
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Armor

    public ArmorCategory getArmorCategory() {
        return Quake.get().getShop().getArmors();
    }

    public HatManager.Hat getSelectedHat() {
        return (HatManager.Hat) getSelectedPurchase(getArmorCategory().getHats(), "selected_hat");
    }

    public Profile setSelectedHat(HatManager.Hat hat) {
        this.data.put("selected_hat", hat.getId());
        return this;
    }

    public ChestplateManager.Chestplate getSelectedChestplate() {
        return (ChestplateManager.Chestplate) getSelectedPurchase(getArmorCategory().getChestplates(), "selected_chestplate");
    }

    public Profile setSelectedChestplate(ChestplateManager.Chestplate chestplate) {
        this.data.put("selected_chestplate", chestplate.getId());
        return this;
    }

    public LeggingManager.Legging getSelectedLeggings() {
        return (LeggingManager.Legging) getSelectedPurchase(getArmorCategory().getLeggings(), "selected_leggings");
    }

    public Profile setSelectedLeggings(LeggingManager.Legging leggings) {
        this.data.put("selected_leggings", leggings.getId());
        return this;
    }

    public BootManager.Boot getSelectedBoots() {
        return (BootManager.Boot) getSelectedPurchase(getArmorCategory().getBoots(), "selected_boots");
    }

    public Profile setSelectedBoots(BootManager.Boot boot) {
        this.data.put("selected_boot", boot.getId());
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Kill sound

    public KillSoundManager.KillSound getSelectedKillSound() {
        return (KillSoundManager.KillSound) getSelectedPurchase(Quake.get().getShop().getKillSounds(), "selected_kill_sound");
    }

    public Profile setSelectedKillSound(KillSoundManager.KillSound killSound) {
        this.data.put("selected_killsound", killSound.getId());
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Dash

    public DashCategory getDashCategory() {
        return Quake.get().getShop().getDashes();
    }

    public DashPowerManager.DashPower getSelectedDashPower() {
        return (DashPowerManager.DashPower) getSelectedPurchase(getDashCategory().getPower(), "selected_dash_power");
    }

    public Profile setSelectedDashPower(DashPowerManager.DashPower dashPower) {
        this.data.put("selected_dash_power", dashPower.getId());
        return this;
    }

    public DashCooldownManager.DashCooldown getSelectedDashCooldown() {
        return (DashCooldownManager.DashCooldown) getSelectedPurchase(getDashCategory().getCooldown(), "selected_dash_cooldown");
    }

    public Profile setSelectedDashCooldown(DashCooldownManager.DashCooldown dashCooldown) {
        this.data.put("selected_dash_cooldown", dashCooldown.getId());
        return this;
    }

    public Set<Purchase<?>> getPurchases() {
        return new HashSet<>(Arrays.asList(
                getSelectedCase(),
                getSelectedLaser(),
                getSelectedBarrel(),
                getSelectedMuzzle(),
                getSelectedTrigger(),

                getSelectedBoots(),
                getSelectedChestplate(),
                getSelectedLeggings(),
                getSelectedHat(),

                getSelectedKillSound(),

                getSelectedDashPower(),
                getSelectedDashCooldown()
        ));
    }
}
