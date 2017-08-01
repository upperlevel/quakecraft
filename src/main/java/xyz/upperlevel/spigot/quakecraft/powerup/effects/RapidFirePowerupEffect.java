package xyz.upperlevel.spigot.quakecraft.powerup.effects;

import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.spigot.quakecraft.game.Participant;
import xyz.upperlevel.uppercore.config.Config;

public class RapidFirePowerupEffect extends TimeBasedPowerupEffect {
    @Getter
    @Setter
    private float multiplier;

    public RapidFirePowerupEffect() {
        super("rapid-fire");
    }

    @Override
    public void start(Participant player) {
        player.setGunCooldownBase(multiplier);
    }

    @Override
    public void end(Participant player) {
        player.setGunCooldownBase(1.0f);
    }

    public void load(Config config) {
        super.load(config);
        this.multiplier = config.getFloatRequired("cooldown");
    }
}
