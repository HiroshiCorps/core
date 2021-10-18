/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.inventory;

import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.spigot.inventory.InventoryGUI;
import fr.redxil.api.spigot.itemstack.APIItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;

import java.util.HashMap;

public class ServersInventory extends InventoryGUI {

    public ServersInventory(Player player, ServerType ServerType) {
        super(player, "Menu Principal", 5, new HashMap<String, Object>() {{
            put("type", ServerType);
        }});
    }

    @Override
    protected void buildGUI() {
        getInventory().setItem(0, new APIItemStack(Material.DIRT).setName("Test").setInvAction((player, event) -> player.sendMessage("Salut")));
    }

    @Override
    public void onInteract(InventoryInteractEvent inventoryInteractEvent) {

    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {

    }
}
