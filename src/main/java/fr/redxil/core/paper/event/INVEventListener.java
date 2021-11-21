/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.paper.inventory.InventoryGUI;
import fr.redxil.api.paper.itemstack.APIItemStack;
import fr.redxil.api.paper.itemstack.GlobalAction;
import fr.redxil.api.paper.itemstack.InvItemAction;
import fr.redxil.api.paper.itemstack.OFFInvItemAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class INVEventListener implements Listener {

    @EventHandler
    public void invClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player)) return;

        Inventory clickedInv = event.getInventory();
        if (clickedInv == null)
            return;

        InventoryHolder inventoryHolder = clickedInv.getHolder();
        if (inventoryHolder instanceof InventoryGUI)
            ((InventoryGUI) inventoryHolder).onClose(event);

    }

    @EventHandler
    public void invClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null)
            return;

        InventoryHolder inventoryHolder = event.getClickedInventory().getHolder();
        if (inventoryHolder instanceof InventoryGUI)
            ((InventoryGUI) inventoryHolder).onClick(event);

        ItemStack clickedItem = clickedInv.getItem(event.getRawSlot());
        if (clickedItem == null)
            return;

        if (!(clickedItem instanceof APIItemStack))
            return;

        InvItemAction itemAction = ((APIItemStack) clickedItem).getInvAction();
        if (itemAction != null)
            itemAction.onClick((Player) event.getWhoClicked(), event);

        GlobalAction globalAction = ((APIItemStack) clickedItem).getGlobalAction();
        if (globalAction != null)
            if (globalAction.onClick((Player) event.getWhoClicked()))
                event.setCancelled(true);

    }

    @EventHandler
    public void invInteract(InventoryInteractEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory clickedInv = event.getInventory();
        if (clickedInv == null || event instanceof InventoryClickEvent)
            return;

        InventoryHolder inventoryHolder = clickedInv.getHolder();
        if (inventoryHolder instanceof InventoryGUI)
            ((InventoryGUI) inventoryHolder).onInteract(event);

    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        APIPlayer player = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (player == null) return;
        if (player.isFreeze()) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null)
            return;

        if (item instanceof APIItemStack) {

            System.out.println("test1");
            APIItemStack apiItemStack = (APIItemStack) item;
            OFFInvItemAction action = apiItemStack.getOFFInvAction();
            if (action != null)
                action.onClick(event.getPlayer(), event);
            GlobalAction actionGlobal = apiItemStack.getGlobalAction();
            if (actionGlobal != null)
                if (actionGlobal.onClick(event.getPlayer()))
                    event.setCancelled(true);
        }

    }

}
