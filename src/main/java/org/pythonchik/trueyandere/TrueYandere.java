package org.pythonchik.trueyandere;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Objects;

public final class TrueYandere extends JavaPlugin {
    private static FileConfiguration config;
    Plugin plugin = this;
    public static Message message;
    public static Message getMessage(){return message;}
    public static String getMenuName(){return  message.hex("&7[&6Расы&7]");}
    public void reload5(){
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
    }


    @Override
    public void onEnable() {
        plugin = this;
        loadConfig();
        message = new Message(this);
        new Menu(this,config);
        getCommand("race").setExecutor(new RaceCommand(this,config));
        getCommand("race").setTabCompleter(new RaceTabCommand(this,config));
        getServer().getPluginManager().registerEvents(new listeners(this,config),this);
        getServer().getScheduler().runTaskTimer(plugin, this::applyEffects, 100, 100);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        config = null;
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void applyEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ConfigurationSection race_temp = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING)));
            if (race_temp == null) {
                return;
            }
            if (race_temp.getKeys(false).contains("biomes") || race_temp.getKeys(false).contains("height")) {
                if (race_temp.getKeys(false).contains("height")){
                    //height
                    boolean condition = player.getLocation().getY() >= race_temp.getInt("height");
                    if (condition) {
                        if (race_temp.getKeys(false).contains("effects")) {
                            ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("effects");
                            if (section != null) {
                                for (String effect : section.getKeys(false)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 200, section.getInt(effect)-1));
                                }
                            }
                        }
                    } //effects
                    else {
                        if (race_temp.getKeys(false).contains("sub_effects")) {
                            ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                            if (section != null) {
                                for (String effect : section.getKeys(false)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 200, section.getInt(effect)-1));
                                }
                            }
                        }
                    }
                    if (player.getPersistentDataContainer().has(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"))) { // should always work, may not work if config have changed
                        if (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) == 0
                                || (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) < 0 && condition)
                                || (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) > 0 && !condition)) {
                            setDefaultAttributes(player);
                            if (condition) {

                                if (race_temp.getKeys(false).contains("attributes")) {
                                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("attributes");
                                    if (section != null) {
                                        for (String attribute : section.getKeys(false)) {
                                            player.getAttribute(Attribute.valueOf(attribute)).setBaseValue(section.getDouble(attribute));
                                        }
                                    }
                                }
                                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 1);
                            } //You are above, use main
                            else {

                                if (race_temp.getKeys(false).contains("sub_attributes")) {
                                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_attributes");
                                    if (section != null) {
                                        for (String attribute : section.getKeys(false)) {
                                            player.getAttribute(Attribute.valueOf(attribute)).setBaseValue(section.getDouble(attribute));
                                        }
                                    }
                                }
                                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, -1);
                            } //You are below, use SUB
                        }
                    } else {
                        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 0);
                    }
                } else {
                    //biomes
                    boolean condition = race_temp.getStringList("biomes").contains(player.getLocation().getWorld().getBiome(player.getLocation()).getKey().getKey().toLowerCase()); //player is in biome
                    if (condition){
                        if (race_temp.getKeys(false).contains("effects")) {
                            ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("effects");
                            if (section != null) {
                                for (String effect : section.getKeys(false)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 200, section.getInt(effect)-1));
                                }
                            }
                        }
                    } //effects
                    else {
                        if (race_temp.getKeys(false).contains("sub_effects")) {
                            ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                            if (section != null) {
                                for (String effect : section.getKeys(false)) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 200, section.getInt(effect)-1));
                                }
                            }
                        }
                    }
                    if (player.getPersistentDataContainer().has(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"))) { // should always work, may not work if config have changed
                        if (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) == 0
                                || (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) < 0 && condition)
                                || (player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) > 0 && !condition)) {
                            setDefaultAttributes(player);
                            if (condition) {
                                if (race_temp.getKeys(false).contains("attributes")) {
                                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("attributes");
                                    if (section != null) {
                                        for (String attribute : section.getKeys(false)) {
                                            player.getAttribute(Attribute.valueOf(attribute)).setBaseValue(section.getDouble(attribute));
                                        }
                                    }
                                }
                                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 1);
                            } //You are above, use main
                            else {
                                if (race_temp.getKeys(false).contains("sub_attributes")) {
                                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("sub_attributes");
                                    if (section != null) {
                                        for (String attribute : section.getKeys(false)) {
                                            player.getAttribute(Attribute.valueOf(attribute)).setBaseValue(section.getDouble(attribute));
                                        }
                                    }
                                }
                                player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, -1);
                            } //You are below, use SUB
                        }
                    } else {
                        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 0);
                    }
                }
            } else {  // unconditionally
                if (race_temp.getKeys(false).contains("effects")) {
                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("effects");
                    if (section != null) {
                        for (String effect : section.getKeys(false)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 200, section.getInt(effect)-1));
                        }
                    }
                }
                if (player.getPersistentDataContainer().has(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) && player.getPersistentDataContainer().get(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER) == 0) {
                    setDefaultAttributes(player);
                    if (race_temp.getKeys(false).contains("attributes")) {
                        ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING))).getConfigurationSection("attributes");
                        if (section != null) {
                            for (String attribute : section.getKeys(false)) {
                                player.getAttribute(Attribute.valueOf(attribute)).setBaseValue(section.getDouble(attribute));
                            }
                        }
                    }
                    player.getPersistentDataContainer().set(new NamespacedKey(plugin, "shikanono_nokonoko_koshitantan"), PersistentDataType.INTEGER, 1);
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
