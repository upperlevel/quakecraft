package xyz.upperlevel.quakecraft.profile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.railgun.Railgun;
import xyz.upperlevel.uppercore.Uppercore;

import java.io.StringWriter;
import java.util.*;

public class Profile extends HashMap<String, Object> {
    public Profile() {
        super();
    }

    public Profile(Map<String, Object> data) {
        super(data);
    }

    public UUID getId() {
        return UUID.fromString((String) get("id"));
    }

    public Profile setId(UUID id) {
        put("id", id.toString());
        return this;
    }

    public String getName() {
        return (String) get("name");
    }

    public Profile setName(String name) {
        put("name", name);
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
        new Yaml().dump(this, writer);
        return writer.toString();
    }

    public Purchase<?> getSelectedPurchase(PurchaseManager<?> manager, String key) {
        if (!containsKey(key)) {
            // The searched purchase wasn't stored yet, choosing the default purchase.
            return manager.getDefault();
        }
        String id = (String) get(key);
        Purchase<?> selected = manager.get(id);
        if (selected == null) {
            Uppercore.logger().warning(String.format("Purchase not found for the manager '%s': %s", manager.getPurchaseName(), id));
            return manager.getDefault();
        }
        return selected;
    }

    public Set<Purchase<?>> getSelectedPurchases() {
        return new HashSet<>(Arrays.asList(
                getSelectedCase(),
                getSelectedLaser(),
                getSelectedBarrel(),
                getSelectedMuzzle(),
                getSelectedTrigger(),

                getSelectedBoots(),
                getSelectedLeggings(),
                getSelectedChestplate(),
                getSelectedHat(),

                getSelectedKillSound(),

                getSelectedDashPower(),
                getSelectedDashCooldown()
        ));
    }

    public Set<Purchase<?>> getPurchases() {
        if (!containsKey("purchases")) {
            return new HashSet<>();
        }
        try {
            // Yup, every time purchases are requested, we need to parse their serializated value.
            JSONArray array = (JSONArray) new JSONParser().parse((String) get("purchases"));
            Set<Purchase<?>> purchases = new HashSet<>();
            for (Object object : array) {
                String id = (String) object;
                PurchaseRegistry registry = Quake.get().getShop().getRegistry();
                Purchase<?> purchase = registry.getPurchase(id);
                if (purchase == null) {
                    Uppercore.logger().warning(String.format("Purchase couldn't be solved for: %s, changed?", id));
                    continue;
                }
                purchases.add(purchase);
            }
            // Makes default purchases bought by the player.
            // This is a trick in order to add default selected purchases (like "none" boots, "none" chestplate...)
            purchases.addAll(getSelectedPurchases());
            return purchases;
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Profile setPurchases(Set<Purchase<?>> purchases) {
        JSONArray array = new JSONArray();
        purchases.forEach(purchase -> array.add(purchase.getFullId()));

        // Yup, every time purchases are set they are JSONified in order to be DB-ready.
        put("purchases", array.toJSONString());
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Stats

    public int getKills() {
        return (Integer) getOrDefault("kills", 0);
    }

    public Profile setKills(int kills) {
        put("kills", kills);
        return this;
    }

    public int getDeaths() {
        return (Integer) getOrDefault("deaths", 0);
    }

    public Profile setDeaths(int deaths) {
        put("deaths", deaths);
        return this;
    }

    public int getWonMatches() {
        return (Integer) getOrDefault("won_matches", 0);
    }

    public Profile setWonMatches(int wonMatches) {
        put("won_matches", wonMatches);
        return this;
    }

    public int getPlayedMatches() {
        return (Integer) getOrDefault("played_matches", 0);
    }

    public Profile setPlayedMatches(int playedMatches) {
        put("played_matches", playedMatches);
        return this;
    }

    public double getKdRatio() {
        return getKills() / (double) getDeaths();
    }

    public double getWinRatio() {
        int won = getWonMatches();
        int lost = getPlayedMatches() - won;
        return won / (double) lost;
    }

    // ------------------------------------------------------------------------------------------------ Gun

    public GunCategory getGunCategory() {
        return Quake.get().getShop().getGuns();
    }

    public BarrelManager.Barrel getSelectedBarrel() {
        return (BarrelManager.Barrel) getSelectedPurchase(getGunCategory().getBarrels(), "selected_barrel");
    }

    public Profile setSelectedBarrel(BarrelManager.Barrel barrel) {
        put("selected_barrel", barrel.getId());
        return this;
    }

    public CaseManager.Case getSelectedCase() {
        return (CaseManager.Case) getSelectedPurchase(getGunCategory().getCases(), "selected_case");
    }

    public Profile setSelectedCase(CaseManager.Case _case) {
        put("selected_case", _case.getId());
        return this;
    }

    public LaserManager.Laser getSelectedLaser() {
        return (LaserManager.Laser) getSelectedPurchase(getGunCategory().getLasers(), "selected_laser");
    }

    public Profile setSelectedLaser(LaserManager.Laser laser) {
        put("selected_laser", laser.getId());
        return this;
    }

    public MuzzleManager.Muzzle getSelectedMuzzle() {
        return (MuzzleManager.Muzzle) getSelectedPurchase(getGunCategory().getMuzzles(), "selected_muzzle");
    }

    public Profile setSelectedMuzzle(MuzzleManager.Muzzle muzzle) {
        put("selected_muzzle", muzzle.getId());
        return this;
    }

    public TriggerManager.Trigger getSelectedTrigger() {
        return (TriggerManager.Trigger) getSelectedPurchase(getGunCategory().getTriggers(), "selected_trigger");
    }

    public Profile setSelectedTrigger(TriggerManager.Trigger trigger) {
        put("selected_trigger", trigger.getId());
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
        put("selected_hat", hat.getId());
        return this;
    }

    public ChestplateManager.Chestplate getSelectedChestplate() {
        return (ChestplateManager.Chestplate) getSelectedPurchase(getArmorCategory().getChestplates(), "selected_chestplate");
    }

    public Profile setSelectedChestplate(ChestplateManager.Chestplate chestplate) {
        put("selected_chestplate", chestplate.getId());
        return this;
    }

    public LeggingManager.Legging getSelectedLeggings() {
        return (LeggingManager.Legging) getSelectedPurchase(getArmorCategory().getLeggings(), "selected_leggings");
    }

    public Profile setSelectedLeggings(LeggingManager.Legging leggings) {
        put("selected_leggings", leggings.getId());
        return this;
    }

    public BootManager.Boot getSelectedBoots() {
        return (BootManager.Boot) getSelectedPurchase(getArmorCategory().getBoots(), "selected_boots");
    }

    public Profile setSelectedBoots(BootManager.Boot boot) {
        put("selected_boots", boot.getId());
        return this;
    }

    // ------------------------------------------------------------------------------------------------ Kill sound

    public KillSoundManager.KillSound getSelectedKillSound() {
        return (KillSoundManager.KillSound) getSelectedPurchase(Quake.get().getShop().getKillSounds(), "selected_kill_sound");
    }

    public Profile setSelectedKillSound(KillSoundManager.KillSound killSound) {
        put("selected_kill_sound", killSound.getId());
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
        put("selected_dash_power", dashPower.getId());
        return this;
    }

    public DashCooldownManager.DashCooldown getSelectedDashCooldown() {
        return (DashCooldownManager.DashCooldown) getSelectedPurchase(getDashCategory().getCooldown(), "selected_dash_cooldown");
    }

    public Profile setSelectedDashCooldown(DashCooldownManager.DashCooldown dashCooldown) {
        put("selected_dash_cooldown", dashCooldown.getId());
        return this;
    }
}
