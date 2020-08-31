package xyz.upperlevel.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.quakecraft.phases.game.Gamer;
import xyz.upperlevel.uppercore.config.Config;

public class RapidFirePowerupEffect extends TimeBasedPowerupEffect {
    @Getter
    @Setter
    private float multiplier;

    public RapidFirePowerupEffect() {
        super("rapid-fire");
    }

    @Override
    public void start(Gamer player) {
        player.setGunCooldownBase(multiplier);
    }

    @Override
    public void end(Gamer player) {
        player.setGunCooldownBase(1.0f);
    }

    @Override
    public void load(Config config) {
        super.load(config);
        this.multiplier = config.getFloatRequired("cooldown");
    }
}
