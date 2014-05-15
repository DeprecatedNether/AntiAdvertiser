package net.unknownmc.antiadvertiser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AdvertiseListener implements Listener {

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            handleChat(e.getPlayer(), e.getMessage());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            handleChat(e.getPlayer(), e.getMessage());
            e.setCancelled(true);
        }
    }

    public boolean handleChat(Player player, String message) {
        AntiAdvertiser.logToFile(player.getName(), message);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("antiadvertiser.notify")) {
                String moderatorMessage = ChatColor.translateAlternateColorCodes('&', AntiAdvertiser.config.getString("messages.moderator-message")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message);
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + moderatorMessage);
            }
        }

        if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("WARN")) {
            String playerMessage = ChatColor.translateAlternateColorCodes('&', AntiAdvertiser.config.getString("messages.player-message")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message);
            player.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + playerMessage);
        } else if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("KICK")) {
            String kickMessage = ChatColor.translateAlternateColorCodes('&', AntiAdvertiser.config.getString("messages.kick-message")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message);
            player.kickPlayer(ChatColor.GOLD + "[AntiAdvertiser]\n" + kickMessage);
            String kickBcast = ChatColor.translateAlternateColorCodes('&', AntiAdvertiser.config.getString("messages.kick-broadcast")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + kickBcast);
            }
        }
        if (!AntiAdvertiser.config.getString("onDetect.command").equals("")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes("&".charAt(0), AntiAdvertiser.config.getString("onDetect.command")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message));
        }
        return true;
    }
}