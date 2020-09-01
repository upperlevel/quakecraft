package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.profile.Profile;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.quakecraft.shop.purchase.SimplePurchase;
import xyz.upperlevel.quakecraft.shop.purchase.single.SinglePurchaseManager;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.sound.PlaySound;

public class KillSoundManager extends SinglePurchaseManager<KillSoundManager.KillSound> {

    public KillSoundManager(PurchaseRegistry registry) {
        super(registry, "kill-sound", "kill-sounds");
    }

    @Override
    public KillSound deserialize(String id, Config config) {
        return new KillSound(id, config);
    }

    @Override
    public void setSelected(Player player, KillSound purchase) {
        Quake.getProfileController().updateProfile(player.getUniqueId(), new Profile().setSelectedKillSound(purchase));
    }

    @Override
    public KillSound getSelected(Profile profile) {
        return profile.getSelectedKillSound();
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
