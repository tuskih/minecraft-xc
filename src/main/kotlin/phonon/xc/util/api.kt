package phonon.xc.util

import org.bukkit.inventory.ItemStack
import phonon.xc.XC

public class XCApi (val xc: XC) {
    /**
     * Get a gun itemstack from ID.
     */
    public fun getGunItemStackFromID(gunId: Int): ItemStack? {
        if ( gunId >= 0 && gunId < xc.config.maxGunTypes ) {
            val gun = xc.storage.gun[gunId]
            if ( gun != null ) {
                val item = gun.toItemStack(xc)
                return item
            }
        }
        return null
    }
}