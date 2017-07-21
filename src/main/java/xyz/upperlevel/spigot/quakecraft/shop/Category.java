package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded;
import xyz.upperlevel.uppercore.gui.Gui;

import java.io.File;

public abstract class Category {
    @Getter
    private Gui gui;

    public void loadGui() {
        File guiFile = new File(
                QuakeCraftReloaded.get().getDataFolder(),
                "guis" + File.separator + getGuiLoc() + ".yml"
        );
        gui = QuakeCraftReloaded.get().getGuis().load(guiFile);
    }

    public abstract String getGuiLoc();
}
