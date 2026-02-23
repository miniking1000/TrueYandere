package org.pythonchik.trueyandere;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public final class TrueYandere extends JavaPlugin {
    public static FileConfiguration config;
    JavaPlugin plugin = this;
    static TrueYandere instance;
    public static Message message;
    public static Message getMessage(){return message;}
    public static String getMenuName(){return message.hex("&7[&6Расы&7]");}
    public static TrueYandere getPlugin() {
        return instance;
    }
    public void reload5(){
        plugin = this;
        instance = this;
        loadConfig();
        new Menu(this, config);
    }


    @Override
    public void onEnable() {
        plugin = this;
        instance = this;
        loadConfig();
        CraftManager.registerCrafts(this);
        message = new Message(this);
        new Menu(this,config);
        getCommand("race").setExecutor(new RaceCommand(this,config));
        getCommand("race").setTabCompleter(new RaceTabCommand(this,config));
        listeners listnrs = new listeners(this, config);
        getServer().getPluginManager().registerEvents(listnrs,this);
        getServer().getScheduler().runTaskTimer(plugin, this::applyEffects, 100, 100);
        // Plugin startup logic
        Bukkit.getScheduler().runTaskTimer(this, listnrs::task, 20L, 20L);

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
            if (!player.getGameMode().equals(GameMode.SURVIVAL)) continue;
            String raceId = player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING);
            if (raceId == null) continue;
            ConfigurationSection race_temp = config.getConfigurationSection(raceId);
            if (race_temp == null) {
                continue;
            }
            //drop armor
            if (race_temp.contains("spec") && race_temp.getConfigurationSection("spec").contains("armor")) {
                PlayerInventory inv = player.getInventory();
                ItemStack[] armor = inv.getArmorContents();

                boolean changed = false;

                for (int i = 0; i < armor.length; i++) {
                    ItemStack item = armor[i];

                    if (item == null || item.getType().isAir()) continue;

                    if (!canWear(race_temp, item)) {
                        ItemStack drop = item.clone();

                        armor[i] = null;
                        player.getWorld().dropItemNaturally(
                                player.getLocation(),
                                drop
                        );

                        changed = true;
                    }
                }

                if (changed) {
                    inv.setArmorContents(armor);
                    player.updateInventory();
                    message.send(player, "&cВы не смоги удержать часть брони!", false);
                }

            }

            //setDefaultAttributes(player);

            ConfigurationSection trigger = race_temp.getConfigurationSection("tick_trigger");
            if (trigger == null) {
                applyEffectsSection(player, race_temp.getConfigurationSection("effects"));
                applyAttributesSection(player, race_temp.getConfigurationSection("attributes"));
                continue;
            }

            boolean conditionMatched = evaluateConditions(player, trigger.getConfigurationSection("conditions"));
            String stateKey = conditionMatched ? "on_true" : "on_false";
            ConfigurationSection selectedState = trigger.getConfigurationSection(stateKey);

            if (selectedState == null) continue;
            applyEffectsSection(player, selectedState.getConfigurationSection("effects"));
            applyAttributesSection(player, selectedState.getConfigurationSection("attributes"));
        }
    }

    private boolean evaluateConditions(Player player, ConfigurationSection conditions) {
        if (conditions == null || conditions.getKeys(false).isEmpty()) return true;

        if (conditions.contains("height_min") && player.getLocation().getY() < conditions.getDouble("height_min")) {
            return false;
        }
        if (conditions.contains("height_max") && player.getLocation().getY() > conditions.getDouble("height_max")) {
            return false;
        }
        boolean inWater = player.getLocation().getBlock().isLiquid() && player.getLocation().getBlock().getType().toString().toLowerCase().contains("water");
        if (conditions.contains("biomes")) {
            String biomeKey = player.getLocation().getWorld().getBiome(player.getLocation()).getKey().getKey().toLowerCase();
            boolean inBiomeList = conditions.getStringList("biomes").contains(biomeKey);
            if (conditions.getBoolean("biome_or_water", false)) {
                if (!(inBiomeList || inWater)) return false;
            } else if (!inBiomeList) {
                return false;
            }
        }
        if (conditions.getBoolean("require_water", false) && !inWater) {
            return false;
        }

        String timeCondition = conditions.getString("time", "any").toLowerCase(Locale.ROOT);
        long worldTime = player.getWorld().getTime();
        if (timeCondition.equals("day") && !(worldTime < 12300 || worldTime > 23850)) {
            return false;
        }
        if (timeCondition.equals("night") && (worldTime < 12300 || worldTime > 23850)) {
            return false;
        }

        return true;
    }

    private void applyEffectsSection(Player player, ConfigurationSection section) {
        if (section == null) return;
        for (String effect : section.getKeys(false)) {
            PotionEffectType effectType = PotionEffectType.getByName(effect);
            if (effectType == null) continue;
            player.addPotionEffect(new PotionEffect(effectType, 600, section.getInt(effect) - 1, false, false));
        }
    }

    private void applyAttributesSection(Player player, ConfigurationSection section) {
        if (section == null) return;
        for (String attribute : section.getKeys(false)) {
            AttributeInstance instance;
            try {
                instance = player.getAttribute(Attribute.valueOf(attribute));
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (instance == null) continue;
            instance.setBaseValue(section.getDouble(attribute));
        }
    }

    public boolean canWear(ConfigurationSection race_temp, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;

        String materialName = item.getType().name();

        boolean isArmor = materialName.endsWith("_HELMET") ||
                materialName.endsWith("_CHESTPLATE") ||
                materialName.endsWith("_LEGGINGS") ||
                materialName.endsWith("_BOOTS");

        if (!isArmor) return true;
        String maxTier = race_temp.getConfigurationSection("spec").getString("armor", "NETHERITE");
        List<String> tiers = Arrays.asList("LEATHER", "GOLD", "CHAINMAIL", "IRON", "DIAMOND", "NETHERITE");

        int playerLimit = tiers.indexOf(maxTier);

        String itemTier = materialName.split("_")[0];
        int currentItemTier = tiers.indexOf(itemTier);

        return currentItemTier <= playerLimit;
    }
    /*
    public static void setDefaultAttributes(Player player) {
        Registry.ATTRIBUTE.iterator().forEachRemaining(attribute -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                System.out.println(instance.getAttribute().name() + " | " + instance.getDefaultValue() + " | " + instance.getBaseValue() + " | " + instance.getValue());
                instance.setBaseValue(instance.getDefaultValue());
            }
        });
    }
     */
    public static Map<Attribute, Double> AttributeDefaults = new HashMap<>();
    static {
        Map<Attribute, Double> map1 = Map.of(
                Attribute.ARMOR, 0.0,
                Attribute.ARMOR_TOUGHNESS, 0.0,
                Attribute.ATTACK_DAMAGE, 1.0,
                Attribute.ATTACK_KNOCKBACK, 0.0,
                Attribute.ATTACK_SPEED, 4.0,
                Attribute.BLOCK_BREAK_SPEED, 1.0,
                Attribute.BLOCK_INTERACTION_RANGE, 4.5,
                Attribute.BURNING_TIME, 1.0,
                Attribute.CAMERA_DISTANCE, 4.0,
                Attribute.ENTITY_INTERACTION_RANGE, 3.0
        );
        Map<Attribute, Double> map2 = Map.of(
                Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, 0.0,
                Attribute.FALL_DAMAGE_MULTIPLIER, 1.0,
                //Attribute.FLYING_SPEED, -1,
                //Attribute.FOLLOW_RANGE, -1,
                Attribute.GRAVITY, 0.08,
                Attribute.JUMP_STRENGTH, 0.41999998688697815,
                Attribute.KNOCKBACK_RESISTANCE, 0.0,
                Attribute.LUCK, 0.0,
                Attribute.MAX_ABSORPTION, 0.0,
                Attribute.MAX_HEALTH, 20.0,
                Attribute.MINING_EFFICIENCY, 0.0,
                Attribute.MOVEMENT_EFFICIENCY, 0.0);
        Map<Attribute, Double> map3 = Map.of(
                Attribute.MOVEMENT_SPEED, 0.10000000149011612,
                Attribute.OXYGEN_BONUS, 0.0,
                Attribute.SAFE_FALL_DISTANCE, 3.0,
                Attribute.SCALE, 1.0,
                Attribute.SNEAKING_SPEED, 0.3,
                //Attribute.SPAWN_REINFORCEMENTS, -1,
                Attribute.STEP_HEIGHT, 0.6,
                Attribute.SUBMERGED_MINING_SPEED, 0.2,
                Attribute.SWEEPING_DAMAGE_RATIO, 0.0,
                //Attribute.TEMPT_RANGE, -1,
                Attribute.WATER_MOVEMENT_EFFICIENCY, 0.0,
                Attribute.WAYPOINT_RECEIVE_RANGE, 60000000.0
        );
        Map<Attribute, Double> map4 = Map.of(
                Attribute.WAYPOINT_TRANSMIT_RANGE, 60000000.0
        );
        AttributeDefaults.putAll(map1);
        AttributeDefaults.putAll(map2);
        AttributeDefaults.putAll(map3);
        AttributeDefaults.putAll(map4);

    }

    public static void setDefaultAttributes(Player player) {
        Registry.ATTRIBUTE.iterator().forEachRemaining(attribute -> {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(AttributeDefaults.getOrDefault(attribute, instance.getDefaultValue()));
            }
        });
    }

}
