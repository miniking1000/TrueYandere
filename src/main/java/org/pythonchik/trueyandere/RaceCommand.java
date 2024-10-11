package org.pythonchik.trueyandere;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class RaceCommand implements CommandExecutor {

    FileConfiguration config;
    private final Message message = TrueYandere.getMessage();
    TrueYandere plugin;
    private final Logger logger = Bukkit.getPluginManager().getPlugin("TrueYandere").getLogger();

    public RaceCommand(TrueYandere plugin, FileConfiguration config){this.plugin = plugin;this.config = config;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        boolean Player = sender instanceof Player;
        if (args.length == 0) {
            if (Player) {
                Player play = (Player) sender;
                Menu.OpenMenu(play, plugin);
            } else {
                logger.info("Используй reload или set/set_race player_name [arg]");
            }
        } else {
            if (args[0].equals("reload") || args[0].equals("set") || args[0].equals("set_race")) {
                // reload - reload plugin
                // set - set someone's race to X
                // add - add a change token(s) to someone
                // remove - remove a change token(s) from someone
                if (Player) {
                    if (!sender.isOp()) {
                        message.send(sender, "Тут могла быть пасхалка, но фокс все равно изменит команду :(");
                        message.send(sender, "Обновление информации, если вдруг не изменит, то как только найдете в ЛС к miniking1000, и не говорить об этом админам");
                        return true;
                    }
                }
                // args >= 1, sender is console or OP (permitted)
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        plugin.reload5();
                        if (Player) message.send(sender, "Перезагрузка завершена");
                        else logger.info("Перезагрузка завершена");
                    }
                } else if (args.length >= 3) {
                    if (args[0].equalsIgnoreCase("set_race")) {
                        Player player = sender.getServer().getPlayer(args[1]);
                        if (player == null) {
                            if (Player) {
                                message.send(sender, "Игрок &9" + args[1] + " &fне найден.");
                            } else {
                                logger.info("Игрок " + args[1] + " не найден.");
                            }
                            return true;
                        }
                        if (!player.getPersistentDataContainer().get(new NamespacedKey(plugin, "race"), PersistentDataType.STRING).equals(args[2])) { // if player is NOT what you want him to be
                            player.getPersistentDataContainer().set(new NamespacedKey(plugin, "race"), PersistentDataType.STRING, args[2]);
                            if (!(args.length >= 4 && (args[3].equalsIgnoreCase("silent") || args[3].equalsIgnoreCase("[silent]")))) {
                                message.send(player, "&7Теперь Вы играете за расу &r" + config.getString(args[2] + ".menu_name"));
                            }
                            if (Player) {
                                message.send(sender, "Игрок &6" + args[1] + "&f теперь играет за расу &6" + config.getString(args[2] + ".menu_name"));
                            } else {
                                logger.info("Игрок " + args[1] + " теперь играет за расу " + config.getString(args[2] + ".menu_name"));
                            }
                            return true;
                        }
                        // Trying to set a player to the same role he already is
                        if (Player) {
                            message.send(sender, "Игрок &6" + args[1] + "&f уже и так играет за &6" + args[2]);
                        } else {
                            logger.info("Игрок " + args[1] + " уже и так играет за " + args[2]);
                        }

                    } else if (args[0].equalsIgnoreCase("set")) {
                        Player player = sender.getServer().getPlayer(args[1]);
                        if (player == null) {
                            if (Player) {
                                message.send(sender, "Игрок &6" + sender + " &fне найден.");
                            } else {
                                logger.info("Игрок " + sender + " не найден.");
                            }
                            return true;
                        }
                        if (!player.getPersistentDataContainer().get(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER).equals(Integer.valueOf(args[2]))) { // PLAYER DOES NOT HAVE THE SAME AMOUNT
                            player.getPersistentDataContainer().set(new NamespacedKey(plugin, "CH4NGE"), PersistentDataType.INTEGER, Integer.valueOf(args[2]));
                            if (!(args.length >= 4 && (args[3].equalsIgnoreCase("silent") || args[3].equalsIgnoreCase("[silent]")))) {
                                message.send(player, "&7Теперь у вас &r" + args[2] + " &7флаконов душ");
                            }
                            if (Player) {
                                message.send(sender, "Теперь у игрока &6" + args[1] + " &fколичество флаконов душ равно &6" + args[2]);
                            } else {
                                logger.info("Теперь у игрока " + args[1] + " " + args[2] + " флаконов душ");
                            }
                            return true;
                        }
                        // player have the same number as you are tring to set
                        if (Player) {
                            message.send(sender, "У игрока &6" + args[1] + "&f уже и так &6" + args[2] + "&f флаконов душ");
                        } else {
                            logger.info("У игрока " + args[1] + " уже и так " + args[2] + " флаконов душ");
                        }
                    }
                } else {
                    if (Player) {
                        message.send(sender, "&cНе достаточно аргументов");
                    } else {
                        logger.warning("Не достаточно аргументов");
                    }
                }
            } else {
                if (Player) {
                    Player play = (Player) sender;
                    Menu.OpenMenu(play, plugin);
                } else {
                    logger.info("Используй reload или set/set_race player_name [arg]");
                }
            }
        }
        return true;
    }
}
