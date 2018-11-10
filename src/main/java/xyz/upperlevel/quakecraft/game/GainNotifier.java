package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.QuakeAccount;
import xyz.upperlevel.quakecraft.events.ParticipantGainMoneyEvent;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.nms.impl.entity.PlayerNms;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.util.List;

public class GainNotifier {
    public static final long MESSAGE_TIME = 4500L;
    private static Message GAIN;

    @Getter
    private final QuakeAccount player;
    private long lastGainTime = -1;
    private float lastGain = -1;

    public GainNotifier(QuakeAccount player) {
        this.player = player;
    }

    public void onGain(float amount) {
        long currentTime = System.currentTimeMillis();
        if (lastGainTime + MESSAGE_TIME > currentTime) {
            amount += lastGain;
        }
        List<String> message = GAIN.get(player.getPlayer(), "raw_gain", format(amount), "gain", EconomyManager.format(amount));
        PlayerNms.sendActionBar(player.getPlayer(), message);
        lastGainTime = currentTime;
        lastGain = amount;
    }

    private String format(float f) {
        if (f == (int) f)
            return Integer.toString((int) f);
        else
            return String.format("%.2f", f);
    }

    public static void setup(Config config) {
        if (!EconomyManager.isEnabled()) {
            Quake.get().getLogger().warning("Economy not found, disabling gain notifier");
            return;
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onGain(ParticipantGainMoneyEvent event) {
                QuakeAccount player = Quake.get().getPlayerManager().getAccount(event.getPlayer().getPlayer());
                player.getGainNotifier().onGain(event.getGain());
            }
        }, Quake.get());
        GAIN = config.getMessageRequired("on-gain");
    }
}
