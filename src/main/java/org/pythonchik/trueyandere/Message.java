package org.pythonchik.trueyandere;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

    private final TrueYandere plug;
    public Message(TrueYandere plug) {this.plug = plug;}
    public void send(CommandSender sender, String message) {
        send(sender, message, true);
    }
    public void send(CommandSender sender, String message, boolean withPrefix) {
        sender.sendMessage(recreator(message, withPrefix));
    }
    public String recreator(String message, boolean withPrefix) {
        return hexPerfix(message, withPrefix);
    }
    public String hex(String message) {
        Pattern pattern = Pattern.compile("(#[a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message).replace('&', '§');
    }
    public ArrayList<String> hex(ArrayList<String> MessageList) {
        ArrayList<String> result = new ArrayList<>();
        for (String message : MessageList) {
            Pattern pattern = Pattern.compile("(#[a-fA-F0-9]{6})");
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String hexCode = message.substring(matcher.start(), matcher.end());
                String replaceSharp = hexCode.replace('#', 'x');

                char[] ch = replaceSharp.toCharArray();
                StringBuilder builder = new StringBuilder("");
                for (char c : ch) {
                    builder.append("&" + c);
                }
                message = message.replace(hexCode, builder.toString());
                matcher = pattern.matcher(message);
            }
            result.add(ChatColor.translateAlternateColorCodes('&', message).replace('&', '§'));
        }
        return result;
    }
    public String hexPerfix(String message, boolean withPrefix) {
        return withPrefix ? hex("&7[&6Расы&7]&r " + message) : hex(message);
    }
}
