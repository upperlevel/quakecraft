package xyz.upperlevel.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.quakecraft.phases.Gamer;
import xyz.upperlevel.uppercore.config.Config;

public class SpeedPowerupEffect extends TimeBasedPowerupEffect {
    public static final float DEF_WALK_VALUE = 0.2f;
    @Getter
    @Setter
    private float speed;

    public SpeedPowerupEffect() {
        super("speed");
    }

    @Override
    public void start(Gamer player) {
        player.getPlayer().setWalkSpeed(speed);
    }

    @Override
    public void end(Gamer player) {
        player.getPlayer().setWalkSpeed(DEF_WALK_VALUE);
    }

    public void load(Config config) {
        super.load(config);
        this.speed = config.getFloatRequired("value");
    }
}
