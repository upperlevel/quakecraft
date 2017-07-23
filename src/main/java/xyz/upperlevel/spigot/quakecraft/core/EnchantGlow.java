package xyz.upperlevel.spigot.quakecraft.core;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class EnchantGlow extends Enchantment {

    static {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            EnchantGlow glow = new EnchantGlow();
            Enchantment.registerEnchantment(glow);
        } catch(Exception e){
            //e.printStackTrace();
        }
    }

    public EnchantGlow() {
        super(256);
    }

    public String getName() {
        return "NewEnchant";
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getStartLevel() {
        return 0;
    }

    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    public boolean conflictsWith(Enchantment e) {
        return false;
    }

    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    public static void addGlow(ItemStack item) {
        item.addEnchantment(new EnchantGlow() , 1);
    }

    public static void addGlow(ItemMeta meta) {
        meta.addEnchant(new EnchantGlow() , 1, true);
    }
}