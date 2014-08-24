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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player attempts to advertise but their message was caught by AntiAdvertiser.
 */
public class PlayerAdvertiseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private String message;
    private AdvertiseType type;
    private Player player;
    private boolean cancelled;

    /**
     * Construct the event.
     * @param player The player advertising.
     * @param message The message that was detected as advertising.
     * @param type What they used to advertise.
     */
    public PlayerAdvertiseEvent(Player player, String message, AdvertiseType type) {
        this.player = player;
        this.message = message;
        this.type = type;
        this.cancelled = false;
    }

    /**
     * Gets the player that advertised.
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the message that was detected as advertising. With books, this is only the page that was detected as having an ad.
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the method used for advertising (book, chat, ...).
     * @return The method (enum from net.unknownmc.api.AdvertiseType)
     */
    public AdvertiseType getType() {
        return type;
    }

    /**
     * Whether the event was cancelled by a plugin.
     * @return True if cancelled, false if not.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Decide whether the event should be cancelled or not.
     * If the event is set to cancel, AntiAdvertiser won't hide (cancel) the message, log it. The player won't be warned/kicked and the custom command won't run.
     * @param cancel Whether to cancel the event (true) or not (false).
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
