/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.info.item;

import fr.redline.invinteract.inv.holder.InventoryInfoHolder;
import fr.redline.invinteract.item.Item;
import fr.redxil.api.common.player.APIOfflinePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class RankItem extends Item {
    @Override
    public ItemStack getItemStack(InventoryInfoHolder inventoryInfoHolder) {
        ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(getAPIPlayer(inventoryInfoHolder).getRank().getRankName());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClicked(InventoryInfoHolder inventoryInfoHolder, InventoryClickEvent inventoryClickEvent) {

    }

    public APIOfflinePlayer getAPIPlayer(InventoryInfoHolder inventoryInfoHolder) {

        Optional<Object> object = inventoryInfoHolder.getData("apiPlayer");
        return (APIOfflinePlayer) object.orElse(null);

    }
}
