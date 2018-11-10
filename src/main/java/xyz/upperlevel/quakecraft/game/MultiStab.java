package xyz.upperlevel.quakecraft.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.events.BulletMultiStabEvent;
import xyz.upperlevel.quakecraft.phases.GamePhase;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.placeholder.message.Message;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class MultiStab {
    private static TreeMap<Integer, MultiStab> values = new TreeMap<>();

    @Getter
    private final String id;

    @Getter
    private final int kills;

    @Getter
    private final Message message;

    @Getter
    private final GainType gain;

    public MultiStab(String id, int kills, Message message, GainType gain) {
        this.id = id;
        this.kills = kills;
        this.message = message;
        this.gain = gain;
    }

    public MultiStab(String id, Config config) {
        this.id = id;
        this.kills = config.getIntRequired("kills");
        this.message = config.getMessageRequired("message");
        this.gain = GainType.create();
        gain.setAmount(config.getFloatRequired("gain"));
    }

    public void reach(GamePhase phase, Gamer shooter, int killCount) {
        BulletMultiStabEvent event = new BulletMultiStabEvent(phase, this, message);
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()){
            event.getMessage().broadcast(
                    phase.getPlayers(),
                    "killer_name", shooter.getPlayer().getName(),
                    "kills", String.valueOf(killCount)
            );
            gain.grant(shooter);
        }
    }

    public static MultiStab getFor(int kills) {
        Map.Entry<Integer, MultiStab> entry =  values.floorEntry(kills);
        return entry == null ? null : entry.getValue();
    }

    public static void tryReach(GamePhase phase, Gamer gamer, int killCount) {
        MultiStab stab = getFor(killCount);
        if(stab == null) return;
        stab.reach(phase, gamer, killCount);
    }

    public static void loadConfig(Map<String, Config> config) {
        values.clear();
        for(Map.Entry<String, Config> kill : config.entrySet()) {
            MultiStab stab;
            try {
                stab = new MultiStab(kill.getKey(), kill.getValue());
            } catch (InvalidConfigException e) {
                e.addLocation("in multistab " + kill.getKey());
                throw e;
            }
            values.put(stab.kills, stab);
        }
    }

    public static void loadConfig() {
        loadConfig(Config.fromYaml(new File(
                Quake.get().getDataFolder(),
                "game/playing/multistabs.yml"
        )).asConfigMap());
    }
}
