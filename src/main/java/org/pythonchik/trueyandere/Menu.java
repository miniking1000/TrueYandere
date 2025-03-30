package org.pythonchik.trueyandere;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class Menu {
    TrueYandere plugin;
    static FileConfiguration config;

    public Menu(TrueYandere plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public static void OpenMenu(Player player, TrueYandere plugin) {
        final Message message = TrueYandere.getMessage();
        Inventory gui = Bukkit.createInventory(player, 27, TrueYandere.getMenuName());
        ItemStack[] stacks = new ItemStack[27];
        ItemStack NULitemStack = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta NULmeta = NULitemStack.getItemMeta();
        NULmeta.setDisplayName(" ");
        NULitemStack.setItemMeta(NULmeta);
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == null) {
                stacks[i] = NULitemStack;
            }
        }
        for (String name : config.getKeys(false)) { // race entry
            if (config.getString(name + ".type", "nothing").equalsIgnoreCase("race")) {
                ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = itemStack.getItemMeta();
                SkullMeta skullMeta = (SkullMeta) meta;
                PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID(), "noone");
                try {
                    URL url = new URL(config.getString(name + ".menu_head", "http://textures.minecraft.net/texture/3a41061ed854151fdda13f683dbe2997a2735caa5a2a59a5699314602a14f9"));
                    PlayerTextures textures = playerProfile.getTextures();
                    textures.setSkin(url);
                    playerProfile.setTextures(textures);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                skullMeta.setOwnerProfile(playerProfile);
                skullMeta.setDisplayName(message.hex(config.getString(name + ".menu_name")));
                skullMeta.setLore(message.hex((ArrayList<String>) config.getStringList(name + ".menu_lore")));
                skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "i-race"), PersistentDataType.STRING, name);
                itemStack.setItemMeta(skullMeta);
                stacks[config.getInt(name + ".slot")] = itemStack;

            } else if (config.getString(name + ".type", "nothing").equalsIgnoreCase("info")) {
                Material item = Material.getMaterial(config.getString(name + ".material", "BEDROCK"));
                ItemStack stack = new ItemStack(item);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(config.getString(name + ".name") != null ? message.hex(config.getString(name + ".name")) : "NO INFO NAME");
                ArrayList<String> lore = !config.getStringList(name + ".lore").equals(new ArrayList<>()) ? message.hex((ArrayList<String>) config.getStringList(name + ".lore")) : new ArrayList<>();
                //{Hapax Legomenon}  =  how much you have
                for (int i = 0; i < lore.size(); i++) {
                    String updatedRow = lore.get(i).replace("{Hapax Legomenon}", String.valueOf(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER)));
                    lore.set(i, updatedRow);  // Update the list with the replaced string
                }
                meta.setLore(lore);

                stack.setItemMeta(meta);
                stacks[config.getInt(name + ".slot")] = stack;
            }


        }
        gui.setContents(stacks);
        player.openInventory(gui);
    }
}
