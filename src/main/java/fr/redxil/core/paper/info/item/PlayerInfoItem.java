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
import fr.redxil.api.common.player.APIPlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerInfoItem extends Item {

    @Override
    public ItemStack getItemStack(InventoryInfoHolder inventoryInfoHolder) {

        APIPlayer apiPlayer = getAPIPlayer(inventoryInfoHolder);

        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwningPlayer(inventoryInfoHolder.getPlayerRelated());
        skullMeta.setDisplayName("Joueur: " + apiPlayer.getRealName());
        skullMeta.setLore(generateLore(apiPlayer));
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    @Override
    public void onItemClicked(InventoryInfoHolder inventoryInfoHolder, InventoryClickEvent inventoryClickEvent) {

    }

    public List<String> generateLore(APIPlayer apiPlayer) {

        ArrayList<String> stringList = new ArrayList<>();
        String name = apiPlayer.getName();
        if (!name.equalsIgnoreCase(apiPlayer.getRealName()))
            stringList.add("Nick: " + name);
        stringList.add("UUID: " + apiPlayer.getUUID().toString());
        return stringList;
    }

    public APIPlayer getAPIPlayer(InventoryInfoHolder inventoryInfoHolder) {

        Optional<Object> object = inventoryInfoHolder.getData("apiPlayer");
        return (APIPlayer) object.orElse(null);

    }

}
