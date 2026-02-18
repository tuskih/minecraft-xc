package phonon.xc.listeners

import createInspect
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import phonon.xc.XC
import phonon.xc.item.getGunFromItemBukkit

public class InspectListener(
    val xc: XC,
): Listener {

    @EventHandler
    public fun onClickEvent(e: InventoryClickEvent) {

        val inv = e.view.title
        val player = e.whoClicked as Player

        if(inv.contains("${ChatColor.GOLD}Inspect Menu")) {
            e.isCancelled = true
            val clickedSlot = e.slot
            val clickedGun = e.inventory.getItem(clickedSlot)

            if(clickedGun !=null) {
                xc.getGunFromItemBukkit(clickedGun)?.let { player.openInventory(xc.createInspect(it.itemModelDefault).inventory) }
            }
        }

    }

    // prot
    @EventHandler
    public fun onInteract(e: InventoryInteractEvent) {

        val inv = e.view.title

        if(inv.contains("${ChatColor.GOLD}Inspect Menu") || inv.contains("${ChatColor.GOLD}Inspect Menu") ) {
            e.isCancelled = true
        }

    }

}