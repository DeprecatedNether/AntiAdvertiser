package net.unknownmc.antiadvertiser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class AdvertiseListener implements Listener {

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.chat")) {
            return;
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            handleChat(e.getPlayer(), e.getMessage());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.commands")) {
            return;
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            handleChat(e.getPlayer(), e.getMessage());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void sign(SignChangeEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.signs")) {
            return;
        }
        // Combine it all into one string. No spaces, so "unknownmc\n.net" becomes "unknownmc.net"
        String lines = "";
        for (String line : e.getLines()) {
            if (!line.equals("")) {
                lines = lines + line;
            }
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), lines)) {
            handleChat(e.getPlayer(), "[SIGN] " + lines);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void bookDrop(PlayerDropItemEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.books")) {
            return;
        }
        ItemStack drop = e.getItemDrop().getItemStack();
        if (drop.getType() != Material.WRITTEN_BOOK && drop.getType() != Material.BOOK_AND_QUILL) {
            return;
        }
        BookMeta book = (BookMeta) drop.getItemMeta();
        // Process one page at a time as to not spam the mods' chat when detecting an advertisement in a long book and not fill up the logs.
        for (int pg = 0; pg < book.getPageCount(); pg++) {
            if (!AntiAdvertiser.safeChat(e.getPlayer(), book.getPage(pg))) {
                handleChat(e.getPlayer(), "[BOOK] " + book.getPage(pg));
                e.setCancelled(true);
            }
        }
    }

    public boolean handleChat(Player player, String message) {
        AntiAdvertiser.logToFile(player, message);
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
            if (!kickBcast.equals("")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + kickBcast);
                }
            }
        }
        if (!AntiAdvertiser.config.getString("onDetect.command").equals("")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes("&".charAt(0), AntiAdvertiser.config.getString("onDetect.command")).replace("{player}", player.getName()).replace("{display}", player.getDisplayName()).replace("{message}", message));
        }
        return true;
    }
}