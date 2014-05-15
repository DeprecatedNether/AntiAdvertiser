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

package net.unknownmc.antiadvertiser.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAdvertiseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private String message;
    private AdvertiseType type;
    private Player player;
    private boolean cancelled;

    public PlayerAdvertiseEvent(Player player, String message, AdvertiseType type) {
        this.player = player;
        this.message = message;
        this.type = type;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public AdvertiseType getType() {
        return type;
    }

    public boolean getCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
