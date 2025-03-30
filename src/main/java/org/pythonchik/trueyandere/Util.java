package org.pythonchik.trueyandere;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class Util {

    public enum Keys {
        Race(new NamespacedKey(TrueYandere.instance, "race")), // the race of the player
        CH4NGE(new NamespacedKey(TrueYandere.instance, "CH4NGE")), // how many times player can change the race
        IRace(new NamespacedKey(TrueYandere.instance, "i-race")), // I think it is used for items? idk tbh
        BloodType(new NamespacedKey(TrueYandere.instance, "blood_type"));  // for the core - blood typed saved or `None` for the start
        private final NamespacedKey value;

        Keys(NamespacedKey value) {
            this.value = value;
        }

        public NamespacedKey getValue() {
            return value;
        }
    }
    public static boolean shouldTakeBlood(Player player) {
        FileConfiguration config = TrueYandere.config;
        String playerRace = player.getPersistentDataContainer().get(Keys.Race.getValue(), PersistentDataType.STRING);
        String playerClass = config.getString(playerRace + ".class");

        for (String key : config.getKeys(false)) {
            if (!config.getString(key + ".type", "").equals("combined")) continue;

            String craft = config.getString(key + ".craft", "");
            String[] parts = craft.split("\\+");
            if (parts.length != 2) continue;

            for (String side : parts) {
                String[] sub = side.split(";");
                if (sub.length != 2) continue;

                String clazz = sub[0], race = sub[1];

                boolean classMatch = clazz.equals("*") || clazz.equalsIgnoreCase(playerClass);
                boolean raceMatch = race.equals("*") || race.equalsIgnoreCase(playerRace);

                if (classMatch && raceMatch) return true;  // хоть одно совпадение — кровь пригодна
            }
        }

        return false;
    }
    public static String getRandomCombinedKey(FileConfiguration config) {
        Set<String> keys = config.getKeys(false); // верхнеуровневые ключи, например: "hydra"
        List<String> combinedKeys = keys.stream()
                .filter(key -> {
                    String type = config.getString(key + ".type");
                    return "combined".equalsIgnoreCase(type);
                })
                .collect(Collectors.toList());

        if (combinedKeys.isEmpty()) return null;

        return combinedKeys.get(new Random().nextInt(combinedKeys.size()));
    }


}
