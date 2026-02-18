/**
 * NMS 1.21.4 compatibility for recoil packets.
 * Uses Mojang-mapped names and unversioned CraftBukkit packages.
 */

package phonon.xc.nms.recoil

import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket
import net.minecraft.commands.arguments.EntityAnchorArgument
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

/**
 * 1.21 changed the player position packet API significantly.
 * For stability, use LookAt packets to approximate recoil.
 */
public fun Player.sendRecoilPacketUsingRelativeTeleport(
    recoilHorizontal: Float,
    recoilVertical: Float,
) {
    val eye = this.eyeLocation
    val newYaw = eye.yaw + recoilHorizontal
    val newPitch = eye.pitch + recoilVertical
    val loc = eye.clone()
    loc.yaw = newYaw
    loc.pitch = newPitch
    val dir = loc.direction.multiply(50.0)

    val packet = ClientboundPlayerLookAtPacket(
        EntityAnchorArgument.Anchor.EYES,
        eye.x + dir.x,
        eye.y + dir.y,
        eye.z + dir.z,
    )
    (this as CraftPlayer).handle.connection.send(packet)
}

/**
 * Create gun visual recoil by telling client to look at a position.
 */
public fun Player.sendRecoilPacketUsingLookAt(
    dirX: Double,
    dirY: Double,
    dirZ: Double,
) {
    val packet = ClientboundPlayerLookAtPacket(
        EntityAnchorArgument.Anchor.EYES,
        dirX,
        dirY,
        dirZ,
    )

    (this as CraftPlayer).handle.connection.send(packet)
}
