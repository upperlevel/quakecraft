package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import org.bukkit.Location;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.sound.PlaySound;

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
        return "kill-sounds.gui";
    }

    @Override
    public String getConfigLoc() {
        return "kill-sounds.types";
    }

    @Override
    public void setSelected(QuakeAccount player, KillSound purchase) {
        player.setSelectedKillSound(purchase);
    }

    @Override
    public KillSound getSelected(QuakeAccount player) {
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
