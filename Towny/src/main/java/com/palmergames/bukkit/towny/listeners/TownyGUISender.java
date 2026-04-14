package com.palmergames.bukkit.towny.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TownyGUISender implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // On vérifie le titre avec ton glyph
        if (title.contains("towny_confirm_create")) {
            event.setCancelled(true); // Bloque le vol d'items
            
            if (!(event.getWhoClicked() instanceof Player player)) return;
            
            int slot = event.getRawSlot();

            if (slot == 20) { 
                player.performCommand("towny:confirm");
                player.closeInventory();
            } else if (slot == 24) { 
                player.performCommand("towny:cancel");
                player.closeInventory();
            }
        }
    }
}