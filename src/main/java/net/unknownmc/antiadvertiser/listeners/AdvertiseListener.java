package net.unknownmc.antiadvertiser.listeners;

import net.unknownmc.antiadvertiser.AntiAdvertiser;
import net.unknownmc.antiadvertiser.api.AdvertiseType;
import net.unknownmc.antiadvertiser.api.PlayerAdvertiseEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class AdvertiseListener implements Listener {
    private AntiAdvertiser plugin;

    public AdvertiseListener(AntiAdvertiser plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGH)
    @SuppressWarnings("deprecation")
    public void onPlayerChat(PlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.chat")) {
            return;
        }
        if (!plugin.safeChat(e.getPlayer(), e.getMessage())) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), e.getMessage(), AdvertiseType.CHAT);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (plugin.getConfig().getBoolean("stealth-mode")) {
                    e.getRecipients().clear();
                    e.getRecipients().add(e.getPlayer());
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.commands")) {
            return;
        }
        if (!plugin.safeChat(e.getPlayer(), e.getMessage())) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), e.getMessage(), AdvertiseType.COMMAND);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (plugin.getConfig().getBoolean("stealth-mode")) {
                    e.getPlayer().sendMessage(plugin.prepareString(plugin.getConfig().getString("messages.stealth-mode-command"), e.getPlayer(), e.getMessage()));
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void sign(SignChangeEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.signs")) {
            return;
        }
        // Combine it all into one string. No spaces, so "unknownmc\n.net" becomes "unknownmc.net"
        String lines = "";
        for (String line : e.getLines()) {
            lines = lines + (plugin.getConfig().getBoolean("sign-merge-lines") ? "" : " ") + line + "\n";
        }
        if (!plugin.safeChat(e.getPlayer(), lines)) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), lines, AdvertiseType.SIGN);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void bookDrop(PlayerDropItemEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.books")) {
            return;
        }
        ItemStack drop = e.getItemDrop().getItemStack();
        if (drop.getType() != Material.WRITTEN_BOOK && drop.getType() != Material.BOOK_AND_QUILL) {
            return;
        }
        BookMeta book = (BookMeta) drop.getItemMeta();
        // Process one page at a time as to not spam the mods' chat when detecting an advertisement in a long book and not fill up the logs.
        for (String page : book.getPages()) {
            if (!plugin.safeChat(e.getPlayer(), page)) {
                PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), page, AdvertiseType.BOOK);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void itemDrop(PlayerDropItemEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.item-names")) {
            return;
        }
        ItemStack drop = e.getItemDrop().getItemStack();
        //Prevent advertisements in the display name or lore of an item.
        if(drop.hasItemMeta()) {
            if(drop.getItemMeta().hasDisplayName()) {
                if(!plugin.safeChat(e.getPlayer(), drop.getItemMeta().getDisplayName())) {
                    PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), drop.getItemMeta().getDisplayName(), AdvertiseType.ITEM);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }

            if(drop.getItemMeta().hasLore()) {
                for(String lore : drop.getItemMeta().getLore()) {
                    if(!plugin.safeChat(e.getPlayer(), lore)) {
                        PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), drop.getItemMeta().getDisplayName(), AdvertiseType.ITEM);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void bookMove(InventoryClickEvent e) {
        if (!plugin.getConfig().getBoolean("monitor.books"))
            return;
        ItemStack[] checks = {e.getCurrentItem(), e.getCursor()};
        Player clicker = (Player) e.getWhoClicked();
        for (ItemStack item : checks) {
            if (item == null)
                continue;
            if (item.getType() != Material.BOOK_AND_QUILL && item.getType() != Material.WRITTEN_BOOK)
                continue;
            BookMeta book = (BookMeta) item.getItemMeta();
            for (String page : book.getPages()) {
                if (!plugin.safeChat(clicker, page)) {
                    PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(clicker, page, AdvertiseType.BOOK);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
