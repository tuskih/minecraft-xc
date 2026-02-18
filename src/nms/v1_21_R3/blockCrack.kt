/**
 * NMS 1.21.4 compatibility for block crack/break animation packets.
 */

package phonon.xc.nms.blockcrack

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.World
import phonon.xc.nms.CraftPlayer
import phonon.xc.nms.broadcastPacketWithinDistance

public fun World.broadcastBlockCrackAnimation(
    players: List<CraftPlayer>,
    entityId: Int,
    blx: Int,
    bly: Int,
    blz: Int,
    breakStage: Int,
) {
    val packet = ClientboundBlockDestructionPacket(entityId, BlockPos(blx, bly, blz), breakStage)

    players.broadcastPacketWithinDistance(
        packet,
        originX = blx.toDouble(),
        originY = bly.toDouble(),
        originZ = blz.toDouble(),
        maxDistance = 64.0,
    )
}
