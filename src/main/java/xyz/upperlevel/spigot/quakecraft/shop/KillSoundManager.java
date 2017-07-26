package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import xyz.upperlevel.spigot.quakecraft.QuakePlayer;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.sound.CompatibleSound;

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
        return "kill-sounds";
    }

    @Override
    public String getConfigLoc() {
        return "kill-sounds";
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
        return "kill sound";
    }

    public class KillSound extends SimplePurchase<KillSound> {
        @Getter
        private final Sound sound;
        @Getter
        private final float pitch, volume;

        public KillSound(String id, Config config) {
            super(KillSoundManager.this, id, config);
            Object obj = config.getRequired("sound");
            Config s;
            if (obj instanceof Map)
                s = Config.wrap((Map<String, Object>) obj);
            else if (obj instanceof ConfigurationSection)
                s = Config.wrap((ConfigurationSection) obj);
            else
                s = null;

            if (s != null) {
                this.sound = s.getSoundRequired("type");
                this.pitch = s.getFloat("pitch", -1.0f);
                this.volume = s.getFloat("volume", -1.0f);
            } else {
                this.sound = CompatibleSound.get(obj.toString());
                this.pitch = -1.0f;
                this.volume = -1.0f;
            }
        }

    }
}
