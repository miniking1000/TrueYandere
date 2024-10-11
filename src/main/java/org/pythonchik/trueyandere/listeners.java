package org.pythonchik.trueyandere;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Objects;

public class listeners implements Listener {
    TrueYandere plugin;
    FileConfiguration config;

    public listeners(TrueYandere plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    private final Message message = TrueYandere.getMessage();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getPersistentDataContainer().has(new NamespacedKey(plugin, "race")) || !event.getPlayer().getPersistentDataContainer().has(new NamespacedKey(plugin, "CH4NGE"))) {
            event.getPlayer().getPersistentDataContainer().set(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER, 1);
            event.getPlayer().getPersistentDataContainer().set(new NamespacedKey(plugin, "race"), PersistentDataType.STRING, "human");
        }
    }

    @EventHandler
    public void onFoodEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getItemMeta() != null && event.getItem().equals(player.getInventory().getItemInMainHand())) {
            ConfigurationSection race_temp = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING)));
            if (race_temp == null) {
                return;
            }
            if (race_temp.getKeys(false).contains("foods") && race_temp.getKeys(false).contains("sub_effects")) {
                if (race_temp.getStringList("foods").contains(player.getInventory().getItemInMainHand().getType().getKey().getKey())){
                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                    if (section != null) {
                        for (String effect : section.getKeys(false)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 600, section.getInt(effect)-1));
                        }
                    }
                }
            }
        }
        if (player.getInventory().getItemInOffHand().getItemMeta() != null && event.getItem().equals(player.getInventory().getItemInOffHand())) {
            ConfigurationSection race_temp = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING)));
            if (race_temp == null) {
                return;
            }
            if (race_temp.getKeys(false).contains("foods") && race_temp.getKeys(false).contains("sub_effects")) {
                if (race_temp.getStringList("foods").contains(player.getInventory().getItemInOffHand().getType().getKey().getKey())){
                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                    if (section != null) {
                        for (String effect : section.getKeys(false)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 600, section.getInt(effect)-1));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = ((Player) event.getEntity().getShooter());
            if (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING).equals("elf")){
                event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(3));
            }
        }
    }

    @EventHandler
    public void onPlanksCraft(CraftItemEvent event) {
        Player player = ((Player) event.getWhoClicked());
        if (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING).equalsIgnoreCase("elf")){
            ArrayList<Material> materials = new ArrayList<>();

            materials.add(Material.OAK_PLANKS);
            materials.add(Material.SPRUCE_PLANKS);
            materials.add(Material.BIRCH_PLANKS);
            materials.add(Material.JUNGLE_PLANKS);
            materials.add(Material.ACACIA_PLANKS);
            materials.add(Material.DARK_OAK_PLANKS);
            materials.add(Material.MANGROVE_PLANKS);
            materials.add(Material.CHERRY_PLANKS);
            materials.add(Material.BAMBOO_PLANKS);
            materials.add(Material.CRIMSON_PLANKS);
            materials.add(Material.WARPED_PLANKS);

            if ((event.getCursor().getAmount() == 0 || (event.getCursor() != null && event.getCursor().getMaxStackSize() == 64 && event.getCursor().getAmount() <= 60)) && materials.contains(event.getRecipe().getResult().getType())) {
                //event.getCursor().setAmount(Math.max(64,event.getCursor().getAmount()+event.getRecipe().getResult().getAmount()));
                event.getWhoClicked().getInventory().addItem(new ItemStack(event.getRecipe().getResult().getType(), event.getRecipe().getResult().getAmount()));
            }
        }
    }


    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(TrueYandere.getMenuName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "i-race"))){
                    Player player = (Player) event.getWhoClicked();
                    String race = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "i-race"), PersistentDataType.STRING);
                    if (race.equals(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"),PersistentDataType.STRING))){
                        message.send(player, "Вы и так уже играете за расу " + config.getString(race + ".menu_name"));
                        for (ItemStack item : event.getInventory()){
                            ItemMeta meta = item.getItemMeta();
                            if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "c-race"), PersistentDataType.BOOLEAN)) {
                                ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                                lore.removeLast();
                                lore.removeLast();
                                lore.removeLast();
                                meta.setLore(lore);
                                meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "c-race"));
                                meta.setEnchantmentGlintOverride(false);
                                item.setItemMeta(meta);
                            }
                        } //reset conformation click
                    } else {
                        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "c-race"))) {
                            if (race.equals("human")) {
                                setDefaultAttributes(player);
                                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "race"), PersistentDataType.STRING, race);
                                message.send(player, "&7Теперь Вы играете за расу &r" + config.getString(race + ".menu_name"));
                            } else {
                                if (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER) >= 1) {
                                    boolean allow = true;
                                    if (config.getConfigurationSection(race).getKeys(false).contains("biomes")) {
                                        allow = false;
                                        for (String racceeee : config.getStringList(race + ".biomes")) {
                                            if (player.getWorld().getBiome(player.getLocation()).getKey().getKey().equals(racceeee)){
                                                allow = true;
                                            }
                                        }
                                    }
                                    if (!allow) {
                                        message.send(player, "Вы не находитесь в нужном биоме что-бы изменить расу на " + config.getString(race + ".menu_name"));
                                        for (ItemStack item : event.getInventory()){
                                            ItemMeta meta = item.getItemMeta();
                                            if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "c-race"), PersistentDataType.BOOLEAN)) {
                                                ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                                                lore.removeLast();
                                                lore.removeLast();
                                                lore.removeLast();
                                                meta.setLore(lore);
                                                meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "c-race"));
                                                meta.setEnchantmentGlintOverride(false);
                                                item.setItemMeta(meta);
                                            }
                                        } //reset conformation click
                                        return;
                                    }
                                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER, player.getPersistentDataContainer().get(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER) - 1);
                                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "race"), PersistentDataType.STRING, race);
                                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 0);

                                    message.send(player, "Выбрана раса " + config.getString(race + ".menu_name"));

                                    TextComponent base_comp = new TextComponent("Если хотите то можете установить один из скинов расы:  ");

                                    TextComponent m_component = new TextComponent("[М]");
                                    m_component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skin set \"{юрля}\"".replace("{юрля}",config.getString(race + ".skin.m"))));
                                    m_component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/skin set \"{юрля}\"".replace("{юрля}",config.getString(race + ".skin.m")))));
                                    m_component.setColor(ChatColor.GOLD);

                                    base_comp.addExtra(m_component);

                                    base_comp.addExtra(" | ");

                                    TextComponent f_component = new TextComponent("[Ж]");
                                    f_component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skin set \"{юрля}\"".replace("{юрля}",config.getString(race + ".skin.f"))));
                                    f_component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/skin set \"{юрля}\"".replace("{юрля}", config.getString(race + ".skin.f")))));
                                    f_component.setColor(ChatColor.GOLD);

                                    base_comp.addExtra(f_component);
                                    player.spigot().sendMessage(base_comp);
                                    player.closeInventory();
                                } else {
                                    message.send(player, "У вас нету флаконов душ");
                                }

                            }
                        } else {
                            for (ItemStack item : event.getInventory()){
                                ItemMeta meta = item.getItemMeta();
                                if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "c-race"), PersistentDataType.BOOLEAN)) {
                                    ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                                    lore.removeLast();
                                    lore.removeLast();
                                    lore.removeLast();
                                    meta.setLore(lore);
                                    meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "c-race"));
                                    meta.setEnchantmentGlintOverride(false);
                                    item.setItemMeta(meta);
                                }
                            }
                            ItemStack stack = event.getCurrentItem();
                            ItemMeta meta = stack.getItemMeta();
                            ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                            lore.add(message.hex("&7-------------"));
                            lore.add(message.hex("&4&lВы уверены что хотите сменить расу? Это действие НЕобратимо!!!"));
                            lore.add(message.hex("&4&lНажмите на голову еще раз что-бы подтвердить выбор"));
                            meta.setLore(lore);
                            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "c-race"), PersistentDataType.BOOLEAN, true);
                            meta.setEnchantmentGlintOverride(true);
                            stack.setItemMeta(meta);
                        }
                    }
                }
            }
        }
    }

    public void setDefaultAttributes(Player player) {
        if (player.getAttribute(Attribute.GENERIC_ARMOR) != null) {
            player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS) != null) {
            player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
        }
        if (player.getAttribute(Attribute.GENERIC_BURNING_TIME) != null) {
            player.getAttribute(Attribute.GENERIC_BURNING_TIME).setBaseValue(1);
        }
        if (player.getAttribute(Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE) != null) {
            player.getAttribute(Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER) != null) {
            player.getAttribute(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER).setBaseValue(1);
        }
        if (player.getAttribute(Attribute.GENERIC_GRAVITY) != null) {
            player.getAttribute(Attribute.GENERIC_GRAVITY).setBaseValue(0.08);
        }
        // Btw, what is your favorite food, someone who reads this? please make a commit with your username and food below:
        // I will start, miniking1000 - honey
        if (player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH) != null) {
            player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
        }
        if (player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_LUCK) != null) {
            player.getAttribute(Attribute.GENERIC_LUCK).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_MAX_ABSORPTION) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_ABSORPTION).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_EFFICIENCY) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_EFFICIENCY).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
        }
        if (player.getAttribute(Attribute.GENERIC_OXYGEN_BONUS) != null) {
            player.getAttribute(Attribute.GENERIC_OXYGEN_BONUS).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE) != null) {
            player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE).setBaseValue(3);
        }
        if (player.getAttribute(Attribute.GENERIC_SCALE) != null) {
            player.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(1);
        }
        if (player.getAttribute(Attribute.GENERIC_STEP_HEIGHT) != null) {
            player.getAttribute(Attribute.GENERIC_STEP_HEIGHT).setBaseValue(0.6);
        }
        if (player.getAttribute(Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY) != null) {
            player.getAttribute(Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED) != null) {
            player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1);
        }
        if (player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE) != null) {
            player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
        }
        if (player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE) != null) {
            player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(3);
        }
        if (player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY) != null) {
            player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY).setBaseValue(0);
        }
        if (player.getAttribute(Attribute.PLAYER_SNEAKING_SPEED) != null) {
            player.getAttribute(Attribute.PLAYER_SNEAKING_SPEED).setBaseValue(0.3);
        }
        if (player.getAttribute(Attribute.PLAYER_SUBMERGED_MINING_SPEED) != null) {
            player.getAttribute(Attribute.PLAYER_SUBMERGED_MINING_SPEED).setBaseValue(0.2);
        }
        if (player.getAttribute(Attribute.PLAYER_SWEEPING_DAMAGE_RATIO) != null) {
            player.getAttribute(Attribute.PLAYER_SWEEPING_DAMAGE_RATIO).setBaseValue(0);
        }

    }

}
