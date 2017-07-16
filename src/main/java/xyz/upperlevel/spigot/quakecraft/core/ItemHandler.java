package xyz.upperlevel.spigot.quakecraft.core;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RESET;

public class ItemHandler {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemHandler(Material type) {
        this(type, (byte) 0);
    }

    @SuppressWarnings("deprecation")
    public ItemHandler(Material type, byte data) {
        this(new ItemStack(type, 1, (short) 0, data));
    }

    public ItemHandler(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public ItemHandler setDisplayName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemHandler setLore(String... lore) {
        setLore(Arrays.asList(lore));
        return this;
    }

    public ItemHandler setLore(List<String> lore) {
        meta.setLore(lore.stream().map(line -> RESET + line).collect(Collectors.toList()));
        return this;
    }

    public ItemHandler setDurability(short durability) {
        item.setDurability(durability);
        return this;
    }

    public ItemHandler setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    @Data
    public static class Enchant {

        private final Enchantment enchantment;
        private final int level;
    }

    public ItemHandler addEnchantment(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Serialize the item in a more user-friendly way than bukkit ones.
     */
    @SuppressWarnings("deprecation")
    public static Map<String, Object> serialize(ItemStack item) {
        Map<String, Object> result = new HashMap<>();

        result.put("type", item.getType().name());
        result.put("data", item.getData().getData());
        result.put("amount", item.getAmount());
        result.put("durability", item.getDurability());

        ItemMeta meta = item.getItemMeta();
        result.put("displayName", meta.getDisplayName());
        result.put("lore", meta.getLore());

        // serializes enchants - <enchantment,level>
        List<String> enchantments = new ArrayList<>();
        item.getEnchantments().forEach((enc, lev) -> enchantments.add(enc.getName() + "," + lev));

        result.put("enchantments", enchantments);

        return result;
    }

    /**
     * Deserialize items.
     */
    public static ItemStack deserialize(ConfigurationSection section) {
        Material type = Material.getMaterial(section.getString("type"));
        byte data = (byte) section.getInt("data");

        ItemHandler item = new ItemHandler(type, data);

        item.setAmount(section.getInt("amount"));
        item.setDurability((short) section.getInt("durability"));

        item.setDisplayName(section.getString("displayName"));
        item.setLore(section.getString("lore"));

        List<String> enchants = section.getStringList("enchantments");
        for (String enchant : enchants) {
            String tmp[] = enchant.split(",");

            Enchantment enc = Enchantment.getByName(tmp[0]);
            int lev = Integer.parseInt(tmp[1]);

            item.addEnchantment(enc, lev);
        }

        return item.build();
    }
}
