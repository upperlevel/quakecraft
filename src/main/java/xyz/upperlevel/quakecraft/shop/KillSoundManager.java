package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.sound.CompatibleSound;
import xyz.upperlevel.uppercore.sound.PlaySound;

import java.util.Map;

public class KillSoundManager extends SinglePurchaseManager<KillSoundManager.KillSound> {

    public KillSoundManager(PurchaseRegistry registry) {
        super(registry);
    }

    @Override
    public KillSound deserialize(String id, Config config) {
        return new KillSound(id, config);
    }

    @Override
    public String getGuiLoc() {
        return "kill_sounds/kill_sounds_gui";
    }

    @Override
    public String getConfigLoc() {
        return "kill_sounds/kill_sounds";
    }

    @Override
    public void setSelected(QuakePlayer player, KillSound purchase) {
        player.setSelectedKillSound(purchase);
    }

    @Override
    public KillSound getSelected(QuakePlayer player) {
        return player.getSelectedKillSound();
    }

    @Override
    public String getPurchaseName() {
        return "kill_sound";
    }

    public class KillSound extends SimplePurchase<KillSound> {
        @Getter
        private final PlaySound sound;

        public KillSound(String id, Config config) {
            super(KillSoundManager.this, id, config);
            sound = config.getPlaySound("sound", PlaySound.SILENT);
        }

        public void play(Location loc) {
            sound.play(loc);
        }
    }
}
