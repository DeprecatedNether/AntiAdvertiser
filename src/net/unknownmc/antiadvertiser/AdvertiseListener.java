/*
 * AntiAdvertiser
 * Copyright (C) 2014  DeprecatedNether
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.unknownmc.antiadvertiser;

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
        for (int pg = 1; pg <= book.getPageCount(); pg++) {
            if (!plugin.safeChat(e.getPlayer(), book.getPage(pg))) {
                PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), book.getPage(pg), AdvertiseType.BOOK);
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
    public void handleAdvertisers(final PlayerAdvertiseEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        String message = e.getMessage();
        switch (e.getType()) {
            case BOOK:
                message = message.replace("\n", "  ");
                break;
            case SIGN:
                message = message.replace("\n", " | ");
                break;
        }
        plugin.getLogger().info(e.getPlayer().getName() + " tried advertising with " + e.getType().toString().toLowerCase() + ": " + e.getMessage());
        try {
            String filePath = plugin.detectionsFile.getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            Date date = new Date();
            fw.write("[" + dateFormat.format(date) + "] " + player.getName() + " (" + player.getUniqueId() + ") [" + e.getType().toString() + "]: " + message + "\n");
            fw.close();
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (online.hasPermission("antiadvertiser.notify")) {
                String moderatorMessage = plugin.prepareString(plugin.getConfig().getString("messages.moderator-message"), e.getPlayer(), e.getMessage());
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + moderatorMessage);
            }
        }

        boolean stealth = plugin.getConfig().getBoolean("stealth-mode");
        if (plugin.getConfig().getString("onDetect.action").equalsIgnoreCase("WARN")) {
            if (!stealth) {
                String playerMessage = plugin.prepareString(plugin.getConfig().getString("messages.player-message"), e.getPlayer(), e.getMessage());
                player.sendMessage(ChatColor.GREEN + playerMessage);
            }
        } else if (plugin.getConfig().getString("onDetect.action").equalsIgnoreCase("KICK")) {
            String kickMessage = plugin.prepareString(plugin.getConfig().getString("messages.kick-message"), e.getPlayer(), e.getMessage());
            player.kickPlayer(ChatColor.GOLD + kickMessage);
            String kickBcast = plugin.prepareString(plugin.getConfig().getString("messages.kick-broadcast"), e.getPlayer(), e.getMessage());
            if (!kickBcast.equals("")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(ChatColor.GREEN + kickBcast);
                }
            }
        }
        if (!plugin.getConfig().getString("onDetect.command").equals("")) {
            if (plugin.getConfig().isList("onDetect.command")) {
                for (String cmd : plugin.getConfig().getStringList("onDetect.command")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), plugin.prepareString(cmd, e.getPlayer(), e.getMessage()));
                }
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), plugin.prepareString(plugin.getConfig().getString("onDetect.command"), e.getPlayer(), e.getMessage()));
            }
        }
    }
}