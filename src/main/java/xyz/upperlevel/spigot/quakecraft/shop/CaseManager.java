package xyz.upperlevel.spigot.quakecraft.shop;

import lombok.Getter;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.config.itemstack.CustomItem;

public class CaseManager extends PurchaseManager<CaseManager.Case> {

    @Override
    public Case deserialize(String id, Config config) {
        try {
            return new Case(id, config);
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in case \"" + id + "\"");
            throw e;
        }
    }

    @Override
    public String getGuiLoc() {
        return "cases";
    }

    @Override
    public String getConfigLoc() {
        return "gun/cases";
    }

    @Override
    public String getPurchaseName() {
        return "case";
    }


    @Getter
    public class Case extends Purchase<Case>{
        private final CustomItem display;

        public Case(String id, String name, float cost, CustomItem icon, boolean def, CustomItem display) {
            super(CaseManager.this, id, name, cost, icon, def);
            this.display = display;
        }

        protected Case(String id, Config config) {
            super(CaseManager.this, id, config);
            this.display = CustomItem.deserialize(config.getConfigRequired("display"));
        }
    }
}