import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import phonon.xc.XC

internal class GunInspect(
    val xc: XC,
    val id: Int,
): InventoryHolder {
    val inv: Inventory = Bukkit.createInventory(this, 27, Component.text("${ChatColor.GOLD}Inspect Menu"))

    override public fun getInventory(): Inventory {
        val gun = xc.storage.gun[id]!!
        val gunItem = gun.toItemStack(xc)
        val meta = gunItem.itemMeta
        val lore = ArrayList<String>()
        if(gun.tier > -1) {
            lore.add("${ChatColor.GOLD}Tier: " + "${ChatColor.WHITE}${gun.tier} ")
        }
        lore.add("${ChatColor.GOLD}Projectile Damage: " + "${ChatColor.WHITE}${gun.projectileDamage}")
        lore.add("${ChatColor.GOLD}Magazine: " + "${ChatColor.WHITE}${gun.ammoMax}")
        lore.add("${ChatColor.GOLD}Ammo Type: " + "${ChatColor.WHITE}${xc.storage.ammo[gun.ammoId]?.itemName}")


        lore.add("${ChatColor.GOLD}Reload Time: " + "${ChatColor.WHITE}${gun.reloadTimeMillis / 1000}s")

        if(gun.autoFire) {
            lore.add("${ChatColor.GOLD}Auto Fire Rate: " + "${ChatColor.WHITE}${gun.autoFireDelayTicks}")
        }
        if(gun.crawlRequired) {
            lore.add("${ChatColor.GOLD}Crawling Time: " + "${ChatColor.WHITE}${gun.crawlTimeMillis / 1000}s")

        }
        // Explosive Config
        if(gun.explosionDamage > 0.1) {
            lore.add("${ChatColor.GOLD}Explosion Damage: " + "${ChatColor.WHITE}${gun.explosionDamage}")
            lore.add("${ChatColor.GOLD}Explosion Radius: " + "${ChatColor.WHITE}${gun.explosionRadius}")
            lore.add("${ChatColor.GOLD}Explosion Falloff: " + "${ChatColor.WHITE}${gun.explosionFalloff}")
        }

        lore.add("${ChatColor.GOLD}Damage Drop Distance: " + "${ChatColor.WHITE}${gun.projectileDamageDropDistance}")
        lore.add("${ChatColor.GOLD}Max Distance: " + "${ChatColor.WHITE}${gun.projectileMaxDistance}")
        meta.lore = lore
        gunItem.itemMeta = meta
        
        inv.setItem(13,gunItem)

        return this.inv
    }
}

/**
 * Helper extension function on XC to create custom item gui.
 */
internal fun XC.createInspect(
    id: Int,
): GunInspect {
    return GunInspect(this, id)
}
