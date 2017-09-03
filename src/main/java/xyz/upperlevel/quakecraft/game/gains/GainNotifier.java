package xyz.upperlevel.quakecraft.game.gains;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.upperlevel.quakecraft.QuakePlayer;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.events.ParticipantGainMoneyEvent;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.util.nms.impl.entity.PlayerNms;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GainNotifier {
    public static final long MESSAGE_TIME = 4500L;
    private static Message GAIN;
    @Getter
    private final QuakePlayer player;
    private long lastGainTime = -1;
    private float lastGain = -1;


    public void onGain(float amount) {
        long currentTime = System.currentTimeMillis();
        if(lastGainTime + MESSAGE_TIME > currentTime) {
            amount += lastGain;
        }
        List<String> message = GAIN.get(player.getPlayer(), "raw_gain", format(amount), "gain", EconomyManager.format(amount));
        BaseComponent[] comps;
        if(message.size() == 1)
            comps = ComponentSerializer.parse(message.get(0));
        else if(message.isEmpty())
            comps = new BaseComponent[0];
        else
            comps = ComponentSerializer.parse(message.stream().collect(Collectors.joining("\n")));

        PlayerNms.sendActionBar(player.getPlayer(), comps);
        lastGainTime = currentTime;
        lastGain = amount;
    }

    private String format(float f) {
        if(f == (int)f)
            return Integer.toString((int) f);
        else
            return String.format("%.2f", f);
    }

    public static void setup() {
        if(!EconomyManager.isEnabled()) {
            Quakecraft.get().getLogger().warning("Economy not found, disabling gain notifier");
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onGain(ParticipantGainMoneyEvent event) {
                QuakePlayer player = Quakecraft.get().getPlayerManager().getPlayer(event.getPlayer().getPlayer());
                player.getGainNotifier().onGain(event.getGain());
            }
        }, Quakecraft.get());
        GAIN = Quakecraft.get().getMessages().get("game.on-gain");
    }
}
