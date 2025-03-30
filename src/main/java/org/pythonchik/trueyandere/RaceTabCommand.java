package org.pythonchik.trueyandere;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RaceTabCommand implements TabCompleter {
    TrueYandere plugin;
    FileConfiguration config;
    public RaceTabCommand(TrueYandere plugin, FileConfiguration config){this.plugin=plugin;this.config=config;}
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("race")) {
            if (sender.isOp()) {
                if (args.length == 1) {
                    List<String> completions = new ArrayList<>();
                    completions.add("reload");
                    completions.add("set");
                    completions.add("add");
                    completions.add("set_race");
                    return completions;

                } else if (args.length == 2 && !args[0].equals("reload")) {
                    List<String> completions = new ArrayList<>();
                    for (Player name : Bukkit.getOnlinePlayers()) {
                        completions.add(name.getName());
                    }
                    return completions;

                } else if (args.length == 3 && args[0].equals("add")) {
                    List<String> completions = new ArrayList<>();
                    completions.add("1");
                    completions.add("2");
                    completions.add("999");
                    return completions;

                } else if (args.length == 3 && args[0].equals("set")) {
                    List<String> completions = new ArrayList<>();
                    completions.add("0");
                    completions.add("1");
                    completions.add("2");
                    completions.add("999");
                    return completions;

                } else if (args.length == 3 && args[0].equals("set_race")) {
                    if (Bukkit.getServer().getPlayer(args[1]) != null) {
                        List<String> completions = new ArrayList<>(config.getKeys(false));
                        completions.removeIf(compel -> Bukkit.getServer().getPlayer(args[1]).getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING).equals(compel) || config.getString(compel + ".type", "info").equals("info"));
                        return completions;
                    } else {
                        return new ArrayList<>(List.of("Пожалуйста, напиши нормального игрока, а не это -> " + args[1]));
                    }
                } else if (args.length == 4 && (args[0].equals("set") || args[0].equals("set_race") || args[0].equals("add"))) {
                    return new ArrayList<>(List.of("[silent]"));
                }

            }
        }
        return null;
    }
}
