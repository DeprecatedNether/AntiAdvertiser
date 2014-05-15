package net.unknownmc.antiadvertiser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AdvertiseListener
        implements Listener
{
    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent e)
    {
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

    public boolean handleChat(Player player, String message)
    {
        AntiAdvertiser.logToFile(player.getName(), message);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("antiadvertiser.notify")) {
                String moderatorMessage = AntiAdvertiser.config.getString("messages.moderator-message");
                moderatorMessage = ChatColor.translateAlternateColorCodes("&".charAt(0), moderatorMessage);
                moderatorMessage = moderatorMessage.replace("{player}", player.getName());
                moderatorMessage = moderatorMessage.replace("{display}", player.getDisplayName());
                moderatorMessage = moderatorMessage.replace("{message}", message);
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + moderatorMessage);
            }
        }
        if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("WARN")) {
            String playerMessage = AntiAdvertiser.config.getString("messages.player-message");
            playerMessage = ChatColor.translateAlternateColorCodes("&".charAt(0), playerMessage);
            playerMessage = playerMessage.replace("{player}", player.getName());
            playerMessage = playerMessage.replace("{display}", player.getDisplayName());
            playerMessage = playerMessage.replace("{message}", message);
            player.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + playerMessage);
        }
        else if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("KICK")) {
            String kickMessage = AntiAdvertiser.config.getString("messages.kick-message");
            kickMessage = ChatColor.translateAlternateColorCodes("&".charAt(0), kickMessage);
            kickMessage = kickMessage.replace("{player}", player.getName());
            kickMessage = kickMessage.replace("{display}", player.getDisplayName());
            kickMessage = kickMessage.replace("{message}", message);
            player.kickPlayer(ChatColor.GOLD + "[AntiAdvertiser]\n" + kickMessage);
            String kickBcast = AntiAdvertiser.config.getString("messages.kick-broadcast");
            kickBcast = kickBcast.replace("{player}", player.getName());
            kickBcast = kickBcast.replace("{display}", player.getDisplayName());
            kickBcast = kickBcast.replace("{message}", message);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + kickBcast);
            }
        }
        if ((!AntiAdvertiser.config.getString("onDetect.command").equals("")) && (AntiAdvertiser.config.getString("onDetect.command") != null)) {
            String cmd = AntiAdvertiser.config.getString("onDetect.command");
            cmd = ChatColor.translateAlternateColorCodes("&".charAt(0), cmd);
            cmd = cmd.replace("{player}", player.getName());
            cmd = cmd.replace("{display}", player.getDisplayName());
            cmd = cmd.replace("{message}", message);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
        return true;
    }
}