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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class AdvertiseListener implements Listener {

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.chat")) {
            return;
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), e.getMessage(), AdvertiseType.CHAT);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (AntiAdvertiser.config.getBoolean("stealth-mode")) {
                    Iterator<Player> it = e.getRecipients().iterator();
                    while (it.hasNext()) {
                        Player next = it.next();
                        if (!next.equals(e.getPlayer())) {
                            it.remove();
                        }
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (!AntiAdvertiser.config.getBoolean("monitor.commands")) {
            return;
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), e.getMessage())) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), e.getMessage(), AdvertiseType.COMMAND);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (AntiAdvertiser.config.getBoolean("stealth-mode")) {
                    e.getPlayer().sendMessage(AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("messages.stealth-mode-command"), e.getPlayer(), e.getMessage()));
                }
                e.setCancelled(true);
            }
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
            lines = lines + line + "\n";
        }
        if (!AntiAdvertiser.safeChat(e.getPlayer(), lines)) {
            PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), lines, AdvertiseType.SIGN);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setCancelled(true);
            }
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
        for (int pg = 1; pg <= book.getPageCount(); pg++) {
            if (!AntiAdvertiser.safeChat(e.getPlayer(), book.getPage(pg))) {
                PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(e.getPlayer(), book.getPage(pg), AdvertiseType.BOOK);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void handleAdvertisers(PlayerAdvertiseEvent e) {
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
        AntiAdvertiser.log.info(e.getPlayer().getName() + " tried advertising with " + e.getType().toString().toLowerCase() + ": " + e.getMessage());
        try {
            String filePath = AntiAdvertiser.detectionsFile.getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            Date date = new Date();
            fw.write("[" + dateFormat.format(date) + "] " + player.getName() + " (" + player.getUniqueId() + ") [" + e.getType().toString() + "]: " + message + "\n");
            fw.close();
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (online.hasPermission("antiadvertiser.notify")) {
                String moderatorMessage = AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("messages.moderator-message"), e.getPlayer(), e.getMessage());
                online.sendMessage(ChatColor.GREEN + "[AntiAdvertiser] " + moderatorMessage);
            }
        }

        if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("WARN")) {
            String playerMessage = AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("messages.player-message"), e.getPlayer(), e.getMessage());
            player.sendMessage(ChatColor.GREEN + playerMessage);
        } else if (AntiAdvertiser.config.getString("onDetect.action").equalsIgnoreCase("KICK")) {
            String kickMessage = AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("messages.kick-message"), e.getPlayer(), e.getMessage());
            player.kickPlayer(ChatColor.GOLD + kickMessage);
            String kickBcast = AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("messages.kick-broadcast"), e.getPlayer(), e.getMessage());
            if (!kickBcast.equals("")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(ChatColor.GREEN + kickBcast);
                }
            }
        }
        if (!AntiAdvertiser.config.getString("onDetect.command").equals("")) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), AntiAdvertiser.prepareString(AntiAdvertiser.config.getString("onDetect.command"), e.getPlayer(), e.getMessage()));
        }
    }
}