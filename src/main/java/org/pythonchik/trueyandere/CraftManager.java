package org.pythonchik.trueyandere;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CraftManager {

    public static void registerCrafts(JavaPlugin plugin) {
        ItemStack rebirthCore = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta meta = rebirthCore.getItemMeta();
        meta.setDisplayName("§6Ядро перерождения");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Нужно наполнить кровью");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(Util.Keys.BloodType.getValue(), PersistentDataType.STRING, "");
        rebirthCore.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(plugin, "rebirth_core");
        ShapedRecipe recipe = new ShapedRecipe(key, rebirthCore);
        recipe.shape("GNG", "SES", "GDG");

        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('N', Material.NETHERITE_BLOCK);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('E', Material.END_ROD);
        recipe.setIngredient('D', Material.DEBUG_STICK);

        Bukkit.addRecipe(recipe);
    }
}
