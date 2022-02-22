package fr.redxil.core.paper.info;

import fr.redline.invinteract.inv.InventoryCreator;
import fr.redline.invinteract.inv.container.Container;
import fr.redline.invinteract.inv.holder.InventoryInfoHolder;
import fr.redline.invinteract.inv.page.Page;
import fr.redxil.core.paper.info.item.PlayerInfoItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.Optional;

public class InfoInventory extends InventoryCreator {

    public InfoInventory() {
        super("Info", 5);
        this.createContainer("info").ifPresent(this::loadInfo);
    }

    public void loadInfo(Container info) {
        Optional<Page> opPage = info.getPage(1);
        if (opPage.isEmpty())
            return;
        Page page = opPage.get();
        page.setItem(new PlayerInfoItem(), 0);
    }

    @Override
    public void onClickInventory(InventoryInfoHolder inventoryInfoHolder, InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onCloseInventory(Player player, InventoryInfoHolder inventoryInfoHolder) {

    }

    @Override
    public void onClickUnderInventory(InventoryInfoHolder inventoryInfoHolder, InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {

    }
}
