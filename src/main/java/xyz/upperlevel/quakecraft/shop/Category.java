package xyz.upperlevel.quakecraft.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.upperlevel.quakecraft.shop.purchase.PurchaseRegistry;
import xyz.upperlevel.uppercore.gui.Gui;

import java.io.File;

import static xyz.upperlevel.quakecraft.Quakecraft.get;

@RequiredArgsConstructor
public abstract class Category {
    @Getter
    protected final PurchaseRegistry registry;
    @Getter
    protected Gui gui;

    protected void loadGui(File file) {
        gui = get().getGuis().loadFile(file).get();
    }

    public void loadGui() {
        File guiFile = new File(
                get().getDataFolder(),
                "shop" + File.separator + getGuiLoc() + ".yml"
        );
        loadGui(guiFile);
    }

    public abstract String getGuiLoc();
}
