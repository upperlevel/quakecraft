package xyz.upperlevel.spigot.quakecraft.core;

import lombok.Getter;

public class PhaseManager {

    @Getter
    private Phase phase;

    public void setPhase(Phase phase) {
        Phase old = this.phase;
        if (old != null)
            old.onDisable(phase);
        this.phase = phase;
        if (phase != null)
            phase.onEnable(old);
    }
}
