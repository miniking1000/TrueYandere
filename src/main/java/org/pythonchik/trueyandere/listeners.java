package org.pythonchik.trueyandere;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class listeners implements Listener {
    TrueYandere plugin;
    FileConfiguration config;
    private final HashMap<UUID, Long> lastUse = new HashMap<>();
    private final long cooldownMillis = 20 * 60 * 1000; // 20 min
    private final HashMap<UUID, Long> voiddudescd = new HashMap<>();
    private final long voidCDMS = 20 * 1000; // 20 sec



    public listeners(TrueYandere plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    private final Message message = TrueYandere.getMessage();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getPersistentDataContainer().has(Util.Keys.Race.getValue()) || !event.getPlayer().getPersistentDataContainer().has(new NamespacedKey(plugin, "CH4NGE"))) {
            event.getPlayer().getPersistentDataContainer().set(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER, 1);
            event.getPlayer().getPersistentDataContainer().set(Util.Keys.Race.getValue(), PersistentDataType.STRING, "human");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFinalDamage() < player.getHealth()) return;

        // Проверка на расу
        String race = player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING);
        if (!"angel".equals(race)) return;

        // Проверка кулдауна
        long now = System.currentTimeMillis();
        Long lastUsed = lastUse.get(player.getUniqueId());
        if (lastUsed != null && now - lastUsed < cooldownMillis) return;

        // Применяем "ангельское спасение"
        event.setCancelled(true);
        player.setHealth(5.0);

        // Визуальные и звуковые эффекты тотема
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30);

        // Эффекты
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0, false, false));

        // Сброс состояния
        player.setFireTicks(0);
        player.setRemainingAir(player.getMaximumAir());
        player.setFallDistance(0);

        // Обновление времени использования
        lastUse.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onVoidgoomClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING).equals("voiddude")) {
            long now = System.currentTimeMillis();
            Long lastUsed = voiddudescd.get(player.getUniqueId());
            if (lastUsed != null && now - lastUsed < voidCDMS) return;
            if (!player.isSneaking()) return;
            if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
            // only voiddudes here!
            ItemStack offstack = player.getInventory().getItemInOffHand();
            if (offstack != null && offstack.getType().equals(Material.TOTEM_OF_UNDYING)){
                // only totems here!
                if (event.getClickedBlock() == null) {
                    // on air
                    EnderPearl pearl = player.launchProjectile(EnderPearl.class);
                    pearl.setShooter(player);
                    voiddudescd.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (!event.getPlayer().getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING).equals("cobble")) {

            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;

            Material blockType = event.getBlock().getType();

            // Create a virtual iron pickaxe with no enchantments
            ItemStack fakePick = new ItemStack(Material.IRON_PICKAXE);

            // Check if this block can be broken by an iron pickaxe
            if (!blockType.isItem()) return; // Just in case

            if (!blockType.isBlock()) return;

            // Cancel original event (no normal drop)
            event.setDropItems(false);

            // Manually break the block
            event.getBlock().setType(Material.AIR);

            // Drop what it would if mined with iron pickaxe
            List<ItemStack> drops = event.getBlock().getDrops(fakePick, event.getPlayer()).stream().toList();

            for (ItemStack drop : drops) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            }

            // Optional: give XP
            int xp = event.getExpToDrop();
            if (xp > 0) {
                event.getBlock().getWorld().spawn(event.getBlock().getLocation(), org.bukkit.entity.ExperienceOrb.class).setExperience(xp);
            }
        }
    }

    @EventHandler
    public void onFoodEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection race_temp = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING)));
        if (race_temp == null) {
            return;
        }
        if (race_temp.getKeys(false).contains("spec") && race_temp.getConfigurationSection("spec").getKeys(false).contains("diet")) {
            List<Material> meats = List.of(Material.BEEF, Material.COOKED_BEEF, Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.MUTTON, Material.COOKED_MUTTON,
                    Material.CHICKEN, Material.COOKED_CHICKEN, Material.RABBIT, Material.COOKED_RABBIT, Material.COD, Material.COOKED_COD,
                    Material.SALMON, Material.COOKED_SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH, Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.RABBIT_STEW);
            boolean isMeat = meats.contains(event.getItem().getType());
            String diet = race_temp.getConfigurationSection("spec").getString("diet");
            if (diet.equalsIgnoreCase("meat_only") && !isMeat) {
                event.setCancelled(true);
                message.send(player, "&cВаш организм способен переварить только мясную пищу.", false);
                return;
            } else if (diet.equalsIgnoreCase("meat_hater") && isMeat) {
                event.setCancelled(true);
                message.send(player, "&cВаш организм способен переварить только растительную пищу.", false);
                return;
            }
        }

        if (player.getInventory().getItemInMainHand().getItemMeta() != null && event.getItem().equals(player.getInventory().getItemInMainHand())) {
            if (race_temp.getKeys(false).contains("foods") && race_temp.getKeys(false).contains("sub_effects")) {
                if (race_temp.getStringList("foods").contains(player.getInventory().getItemInMainHand().getType().getKey().getKey())){
                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                    if (section != null) {
                        for (String effect : section.getKeys(false)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 600, section.getInt(effect)-1, false, false));
                        }
                    }
                }
            }
        }
        if (player.getInventory().getItemInOffHand().getItemMeta() != null && event.getItem().equals(player.getInventory().getItemInOffHand())) {
            if (race_temp.getKeys(false).contains("foods") && race_temp.getKeys(false).contains("sub_effects")) {
                if (race_temp.getStringList("foods").contains(player.getInventory().getItemInOffHand().getType().getKey().getKey())){
                    ConfigurationSection section = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING))).getConfigurationSection("sub_effects");
                    if (section != null) {
                        for (String effect : section.getKeys(false)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(effect), 600, section.getInt(effect)-1, false, false));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void forThatOneRace(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ConfigurationSection race_temp = config.getConfigurationSection(Objects.requireNonNull(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING)));
        if (race_temp == null) {
            return;
        }
        if (race_temp.getKeys(false).contains("spec") && race_temp.getConfigurationSection("spec").getKeys(false).contains("hunger")) {
            double modifier = race_temp.getConfigurationSection("spec").getDouble("hunger");
            event.setExhaustion((float) (event.getExhaustion()*modifier));
        }
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = ((Player) event.getEntity().getShooter());
            if (config.contains(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING) + ".spec")) {
                event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(config.getInt(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING) + ".spec.arrow", 1)));
            }
        }
    }

    HashMap<Player, Location> lastLocation = new HashMap<>();
    HashMap<Player, Integer> timeStill = new HashMap<>();

    public void task() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getPersistentDataContainer().getOrDefault(Util.Keys.Race.getValue(), PersistentDataType.STRING, "random dude with no tag").equals("elf")) {
                Location currentLocation = player.getLocation();

                if (!lastLocation.containsKey(player)) {
                    lastLocation.put(player, currentLocation);
                    timeStill.put(player, 0);
                    return;
                }

                Location lastLoc = lastLocation.get(player);
                if (currentLocation.getWorld().equals(lastLoc.getWorld())) {
                    double distance = lastLoc.distance(currentLocation);
                    if (distance < 0.01 && player.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
                        int stoodStillSoFar = timeStill.getOrDefault(player, 0) + 1;
                        timeStill.put(player, stoodStillSoFar);
                        if (stoodStillSoFar >= 10) {
                            for (Player heh : Bukkit.getOnlinePlayers()) heh.hidePlayer(plugin, player);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 99, true, false));
                        }
                        continue;

                    }
                }
                for (Player heh : Bukkit.getOnlinePlayers()) heh.showPlayer(plugin, player);
                timeStill.put(player, 0);
                lastLocation.put(player, currentLocation);
            }
        }
    }

    @EventHandler
    public void onUseRebirthCore(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.STRUCTURE_BLOCK || item.getItemMeta() == null || !item.getItemMeta().getPersistentDataContainer().has(Util.Keys.BloodType.getValue())) return;

        ItemMeta meta = item.getItemMeta();

        boolean isCharged = meta.getPersistentDataContainer().has(Util.Keys.BloodType.getValue(), PersistentDataType.STRING) && !meta.getPersistentDataContainer().getOrDefault(Util.Keys.BloodType.getValue(), PersistentDataType.STRING, "").equals("");

        if (isCharged) {
            // Получаем кровь из ядра
            String bloodInfo = meta.getPersistentDataContainer().get(Util.Keys.BloodType.getValue(), PersistentDataType.STRING);
            if (bloodInfo == null || !bloodInfo.contains(";")) {
                message.send(player,"&6Это ядро сломано из-за какой то ошибки(скорее всего моей), напиши Fоксе и отложи это ядро в уголок инвентаря.");
                return;
            }

            String[] split = bloodInfo.split(";");
            String donorClass = split[0];
            String donorRace = split[1];

            String playerRace = player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING);
            String playerClass = config.getString(playerRace + ".class");

            String bestMatch = null;
            int bestSpecificity = -1;

            for (String key : config.getKeys(false)) {
                if (!config.getString(key + ".type", "").equals("combined")) continue;

                String craft = config.getString(key + ".craft", "");
                String[] sides = craft.split("\\+");
                if (sides.length != 2) continue;

                for (int i = 0; i < 2; i++) {
                    String[] s1 = sides[i].split(";");
                    String[] s2 = sides[1 - i].split(";");
                    if (s1.length != 2 || s2.length != 2) continue;

                    // player: s1; blood: s2
                    boolean playerClassMatch = s1[0].equals("*") || s1[0].equalsIgnoreCase(playerClass);
                    boolean playerRaceMatch = s1[1].equals("*") || s1[1].equalsIgnoreCase(playerRace);

                    boolean bloodClassMatch = s2[0].equals("*") || s2[0].equalsIgnoreCase(donorClass);
                    boolean bloodRaceMatch = s2[1].equals("*") || s2[1].equalsIgnoreCase(donorRace);

                    if (playerClassMatch && playerRaceMatch && bloodClassMatch && bloodRaceMatch) {
                        int specificity =
                                (s1[0].equals("*") ? 0 : 1) + (s1[1].equals("*") ? 0 : 1) +
                                        (s2[0].equals("*") ? 0 : 1) + (s2[1].equals("*") ? 0 : 1); // more specific - better
                        if (specificity > bestSpecificity) {
                            bestSpecificity = specificity;
                            bestMatch = key;
                        }
                    }
                }
            }

            if (bestMatch != null) {
                if (new Random().nextFloat() <= 0.01) {
                    String race = Util.getRandomCombinedKey(config);

                    player.getPersistentDataContainer().set(Util.Keys.Race.getValue(), PersistentDataType.STRING, race);
                    if (player.getInventory().getItemInOffHand().getAmount() == 1) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        ItemStack longassnameforuselessonetimevariableisthebestthingicouldeverdo = player.getInventory().getItemInMainHand();
                        longassnameforuselessonetimevariableisthebestthingicouldeverdo.setAmount(longassnameforuselessonetimevariableisthebestthingicouldeverdo.getAmount()-1);
                        player.getInventory().setItemInMainHand(longassnameforuselessonetimevariableisthebestthingicouldeverdo);
                    }
                    message.send(player, "&dВаша кровь мутировала и гибрид получился случайным (шанс 1%): " + config.getString(bestMatch + ".menu_name", race), false);
                    player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0,1,0), 9999);
                    player.getWorld().playSound(player, Sound.ITEM_TOTEM_USE, 0.7f, 1);
                    player.getWorld().playSound(player, Sound.ENTITY_DOLPHIN_EAT, 0.9f, 1);
                } else {
                    player.getPersistentDataContainer().set(Util.Keys.Race.getValue(), PersistentDataType.STRING, bestMatch);
                    if (player.getInventory().getItemInOffHand().getAmount() == 1) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        ItemStack longassnameforuselessonetimevariableisthebestthingicouldeverdo = player.getInventory().getItemInMainHand();
                        longassnameforuselessonetimevariableisthebestthingicouldeverdo.setAmount(longassnameforuselessonetimevariableisthebestthingicouldeverdo.getAmount() - 1);
                        player.getInventory().setItemInMainHand(longassnameforuselessonetimevariableisthebestthingicouldeverdo);
                    }
                    message.send(player, "&6Вы переродились в " + config.getString(bestMatch + ".menu_name", bestMatch), false);
                    player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 9999);
                    player.getWorld().playSound(player, Sound.ITEM_TOTEM_USE, 0.7f, 1);
                }
            } else {
                message.send(player, "&6Эта кровь не подходит вам...", false);
            }

            return;
        }

        // just crafted core
        if (Util.shouldTakeBlood(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 90, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 90, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 60 * 45, 1, false, false));

            String raceId = player.getPersistentDataContainer().get(Util.Keys.Race.getValue(), PersistentDataType.STRING);
            String raceDisplayName = config.getString(raceId + ".menu_name");
            if (raceDisplayName == null) raceDisplayName = raceId;

            meta.setLore(List.of(message.hex(String.format("&7Содержит кровь %s - (%s&7)", player.getName(), raceDisplayName))));
            meta.getPersistentDataContainer().set(Util.Keys.BloodType.getValue(), PersistentDataType.STRING, config.getString(raceId + ".class", "*") + ";" + raceId);
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);

            player.sendMessage("§6Вы наполнили ядро своей кровью...");
        } else {
            player.sendMessage("§6Как не печально, но твоя кровь не нужна...");
        }
    }


    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(TrueYandere.getMenuName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "i-race"))){
                    Player player = (Player) event.getWhoClicked();
                    String race = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(Util.Keys.IRace.getValue(), PersistentDataType.STRING);
                    if (race.equals(player.getPersistentDataContainer().get(Util.Keys.Race.getValue(),PersistentDataType.STRING))){
                        message.send(player, "&6Вы и так уже играете за расу " + config.getString(race + ".menu_name"));
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
                                player.getPersistentDataContainer().set(Util.Keys.Race.getValue(), PersistentDataType.STRING, race);
                                message.send(player, "&6Теперь Вы играете за расу &r" + config.getString(race + ".menu_name"));
                            } else {
                                if (player.getPersistentDataContainer().get(Util.Keys.CH4NGE.getValue(), PersistentDataType.INTEGER) >= 1) {
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
                                        message.send(player, "&cВы не находитесь в нужном биоме что-бы изменить расу на &r" + config.getString(race + ".menu_name"));
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
                                    player.getPersistentDataContainer().set(Util.Keys.CH4NGE.getValue(), PersistentDataType.INTEGER, player.getPersistentDataContainer().get(Util.Keys.CH4NGE.getValue(), PersistentDataType.INTEGER) - 1);
                                    player.getPersistentDataContainer().set(Util.Keys.Race.getValue(), PersistentDataType.STRING, race);

                                    player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0,1,0), 150);
                                    player.getWorld().playSound(player, Sound.ITEM_TOTEM_USE, 0.7f, 1);
                                    message.send(player, "&6Выбрана раса " + config.getString(race + ".menu_name"));

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
                                    message.send(player, "&6У вас нету флаконов душ");
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
        for (Player heh : Bukkit.getOnlinePlayers()) heh.showPlayer(plugin, player);
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
