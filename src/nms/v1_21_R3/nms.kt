/**
 * NMS 1.21.4 type aliases
 * NOTE: uses Mojang-mapped names.
 */

package phonon.xc.nms

import net.minecraft.server.level.ServerPlayer
import net.minecraft.nbt.Tag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.IntTag
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.ItemStack
import java.lang.reflect.Field

internal typealias NmsItemStack = ItemStack
internal typealias NmsNBTTagCompound = CompoundTag
internal typealias NmsNBTTagList = ListTag
internal typealias NmsNBTTagString = StringTag
internal typealias NmsNBTTagInt = IntTag
internal typealias NmsPacketPlayOutSetSlot = ClientboundContainerSetSlotPacket
internal typealias CraftItemStack = org.bukkit.craftbukkit.inventory.CraftItemStack
internal typealias CraftPlayer = org.bukkit.craftbukkit.entity.CraftPlayer
internal typealias CraftMagicNumbers = org.bukkit.craftbukkit.util.CraftMagicNumbers

// ============================================================================
// EXTENSION FUNCTIONS FOR API COMPATIBILITY
// ============================================================================

/**
 * Get player selected item as an NMS ItemStack.
 */
internal fun CraftPlayer.getMainHandNMSItem(): NmsItemStack {
    val nmsPlayer = this.handle
    return nmsPlayer.getMainHandItem()
}

/**
 * Wrapper for getting player connection and sending packet.
 */
internal fun <T: PacketListener> ServerPlayer.sendPacket(p: Packet<T>) {
    return this.connection.send(p)
}

// ============================================================================
// ItemStack NBT access shims (method names changed across versions)
// ============================================================================
private object ItemStackTagAccess {
    val tagField: Field? = try {
        ItemStack::class.java.getDeclaredField("tag").apply { isAccessible = true }
    } catch (_: Throwable) { null }
}

internal fun ItemStack.getTag(): CompoundTag? {
    // Prefer reflection field if present; fallback to safer getter patterns
    ItemStackTagAccess.tagField?.let { f ->
        return f.get(this) as? CompoundTag
    }
    return try {
        val m = ItemStack::class.java.getMethod("getTag")
        m.invoke(this) as? CompoundTag
    } catch (_: Throwable) {
        try {
            val m = ItemStack::class.java.getMethod("getOrCreateTag")
            m.invoke(this) as? CompoundTag
        } catch (_: Throwable) {
            null
        }
    }
}

internal fun ItemStack.hasTag(): Boolean {
    return getTag() != null
}

internal fun ItemStack.setTag(tag: CompoundTag) {
    ItemStackTagAccess.tagField?.let { f ->
        f.set(this, tag); return
    }
    try {
        val m = ItemStack::class.java.getMethod("setTag", CompoundTag::class.java)
        m.invoke(this, tag)
    } catch (_: Throwable) {
        // ignore
    }
}

/**
 * Send packet to all players in list of players within distance
 * from origin.
 */
internal fun <T: PacketListener> List<CraftPlayer>.broadcastPacketWithinDistance(
    packet: Packet<T>,
    originX: Double,
    originY: Double,
    originZ: Double,
    maxDistance: Double,
) {
    val maxDistanceSq = maxDistance * maxDistance

    for ( player in this ) {
        val loc = player.location

        val dx = loc.x - originX
        val dy = loc.y - originY
        val dz = loc.z - originZ

        val distanceSq = (dx * dx) + (dy * dy) + (dz * dz)

        if ( distanceSq <= maxDistanceSq ) {
            player.getHandle().connection.send(packet)
        }
    }
}

/**
 * Wrapper to send a PacketPlayOutSetSlot to a player inventory slot.
 */
internal fun ServerPlayer.sendItemSlotChange(slot: Int, item: ItemStack) {
    val packet = NmsPacketPlayOutSetSlot(
        this.inventoryMenu.containerId,
        this.inventoryMenu.incrementStateId(),
        slot,
        item,
    )
    this.connection.send(packet)
}

/**
 * Compatibility wrapper for setting NBT tag in compound tag.
 * Using "putTag" because actual function name is generic.
 */
internal fun CompoundTag.putTag(key: String, tag: Tag) {
    this.put(key, tag)
}

/**
 * Compatibility wrapper for compound tag "hasKey" in older versions.
 */
internal fun CompoundTag.containsKey(key: String): Boolean {
    return this.contains(key)
}

/**
 * Compatibility wrapper for compound tag "hasKeyOfType" in older versions.
 */
internal fun CompoundTag.containsKeyOfType(key: String, ty: Int): Boolean {
    return this.contains(key, ty)
}

// ============================================================================
// NBT TAG WRAPPERS
// ============================================================================

@JvmInline
internal value class NBTTagString(val tag: NmsNBTTagString) {
    constructor(s: String) : this(NmsNBTTagString.valueOf(s))

    fun toNms(): NmsNBTTagString {
        return this.tag
    }
}

@JvmInline
internal value class NBTTagInt(val tag: NmsNBTTagInt) {
    constructor(i: Int) : this(NmsNBTTagInt.valueOf(i))

    fun toNms(): NmsNBTTagInt {
        return this.tag
    }
}

