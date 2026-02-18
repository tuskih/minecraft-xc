/**
 * Utils for sound effects.
 */
package phonon.xc.util.sound

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.plugin.Plugin
import java.util.logging.Logger


/**
 * Wrapper for playing sound effect 
 */
public data class SoundPacket(
    val sound: String,
    val world: World,
    val location: Location,
    val volume: Float,
    val pitch: Float,
)

/**
 * Runnable task to spawn explosion particles
 */
public class TaskSounds(
    val plugin: Plugin,
    val sounds: ArrayList<SoundPacket>,
): Runnable {
    private fun fallbackKeyFor(name: String): String {
        val n = name.lowercase()
        return when {
            "reload" in n && ("finish" in n || "end" in n) -> "minecraft:item.crossbow.loading_end"
            "reload" in n -> "minecraft:item.crossbow.loading_start"
            "empty" in n -> "minecraft:block.note_block.hat"
            "explosion" in n || "explode" in n -> "minecraft:entity.generic.explode"
            "shoot" in n || "fire" in n -> "minecraft:item.crossbow.shoot"
            else -> "minecraft:item.crossbow.shoot"
        }
    }

    private fun playSoundSafe(logger: Logger, s: SoundPacket) {
        try {
            s.world.playSound(
                s.location,
                s.sound,
                s.volume,
                s.pitch,
            )
        } catch (err: Exception) {
            val fb = fallbackKeyFor(s.sound)
            try {
                s.world.playSound(
                    s.location,
                    fb,
                    s.volume,
                    s.pitch,
                )
                logger.warning("Invalid sound '${s.sound}', used fallback '${fb}'")
            } catch (_: Exception) {
                // swallow to avoid noisy logs each tick
            }
        }
    }

    override fun run() {
        val logger = plugin.logger
        if ( Bukkit.isPrimaryThread() ) {
            for ( s in sounds ) playSoundSafe(logger, s)
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable { for ( s in sounds ) playSoundSafe(logger, s) })
        }
    }
}
