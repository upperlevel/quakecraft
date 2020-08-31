package xyz.upperlevel.quakecraft.powerup.effects;

import xyz.upperlevel.quakecraft.Quake;
import xyz.upperlevel.quakecraft.phases.game.Gamer;
import xyz.upperlevel.uppercore.config.Config;

public class DashBoostPowerupEffect extends TimeBasedPowerupEffect {
    private float multiplier = 1.0f;

    public DashBoostPowerupEffect() {
        super("dash-boost");
    }

    @Override
    public void start(Gamer gamer) {
        gamer.setDashBoostBase(multiplier);
    }

    @Override
    public void end(Gamer gamer) {
        gamer.setDashBoostBase(1.0f);
    }

    @Override
    public void load(Config config) {
        super.load(config);
        this.multiplier = config.getFloatRequired("value");
    }
}
