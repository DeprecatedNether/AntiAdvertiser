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

package net.unknownmc.antiadvertiser.listeners;

import net.unknownmc.antiadvertiser.AntiAdvertiser;
import net.unknownmc.antiadvertiser.api.PlayerAdvertiseEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HandleAdvertising implements Listener {
    private AntiAdvertiser plugin;

    public HandleAdvertising(AntiAdvertiser plugin) {
        this.plugin = plugin;
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
