package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.spigot.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.gui.Gui;

import java.io.File;

import static xyz.upperlevel.spigot.quakecraft.QuakeCraftReloaded.get;

@RequiredArgsConstructor
public abstract class Category {
    @Getter
    protected final PurchaseRegistry registry;
    @Getter
    protected Gui gui;

    protected void loadGui(File file) {
        get().getGuis().load(file);
        gui = get().getGuis().get(getGuiLoc()).get();
    }

    public void loadGui() {
        File guiFile = new File(
                get().getDataFolder(),
                "guis" + File.separator + getGuiLoc() + ".yml"
        );
        loadGui(guiFile);
    }

    public abstract String getGuiLoc();
}
