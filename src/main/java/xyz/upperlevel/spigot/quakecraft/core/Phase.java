package xyz.upperlevel.spigot.quakecraft.core;

public interface Phase {

    void onEnable(Phase previous);

    void onDisable(Phase next);
}
