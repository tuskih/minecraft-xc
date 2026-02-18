package phonon.xc.nms.gun.crawl

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * 1.21 NMS crawl shim: no-op BoxEntity to keep builds compiling.
 * Provides API expected by higher-level crawl system.
 */
class BoxEntity(
    location: Location,
) {
    fun moveAboveLocation(location: Location) { /* no-op */ }
    fun sendCreatePacket(player: Player) { /* no-op */ }
    fun sendMovePacket(player: Player) { /* no-op */ }
    fun sendPeekMetadataPacket(player: Player) { /* no-op */ }
    fun sendDestroyPacket(player: Player) { /* no-op */ }
}
