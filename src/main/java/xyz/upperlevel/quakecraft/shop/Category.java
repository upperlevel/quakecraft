package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.quakecraft.Quakecraft;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.gui.Gui;

import java.io.File;

import static xyz.upperlevel.quakecraft.Quake.get;

@RequiredArgsConstructor
public abstract class Category {
    @Getter
    protected final PurchaseRegistry registry;
    @Getter
    protected Gui gui;

    protected void loadGui(String name, File file) {
        gui = Quakecraft.get().getGuis().register(name, gui);
    }

    public void loadGui() {
        File guiFile = new File(
                get().getDataFolder(),
                "shop/" + getGuiLoc() + ".yml"
        );
        loadGui(getGuiRegistryName(), guiFile);
    }

    public abstract String getGuiLoc();

    /**
     * @return The name associated with the Gui in the gui registry
     */
    public abstract String getGuiRegistryName();
}
