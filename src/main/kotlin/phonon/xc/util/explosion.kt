/**
 * Explosion handler
 */

package phonon.xc.util.explosion

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Damageable
import phonon.xc.XC
import phonon.xc.event.XCExplosionDamageEvent
import phonon.xc.util.ChunkCoord3D
import phonon.xc.util.Hitbox
import phonon.xc.util.damage.DamageType
import phonon.xc.util.damage.explosionDamageAfterArmor
import phonon.xc.util.death.XcPlayerDeathEvent
import phonon.xc.util.particle.ParticlePacket
import phonon.xc.util.particle.ParticleExplosion


/**
 * Return damage from explosion based on distance, armor,
 * and blast protection enchant level.
 * 
 * Explosion damage done by a "hat" function:
 *          ___________  baseDamage
 *         /           \
 *     ___/             \____ 0
 *          <--> c <-->
 *                 radius
 * Within center radius, baseDamage is applied.
 * Outside of radius, linearly falloff factor applied, clamped to 0.
 */
public fun baseExplosionDamage(
    baseDamage: Double,
    distance: Double,
    radius: Double,
    falloff: Double,
): Double {
    val distanceBeyondRadius = max(0.0, distance - radius)
    return max(0.0, baseDamage - (distanceBeyondRadius * falloff))
}

/**
 * Create explosion at location.
 */
public fun XC.createExplosion(
    hitboxes: Map<ChunkCoord3D, List<Hitbox>>,
    location: Location,
    source: Entity?,
    maxDistance: Double,
    damage: Double,
    radius: Double,
    falloff: Double, // damage/block falloff
    armorReduction: Double, // damage/armor
    blastProtReduction: Double, // damage/blast protection
    damageType: DamageType,
    blockDamagePower: Float,
    fireTicks: Int, // number of ticks to set targets on fire
    particles: ParticlePacket?,
    weaponType: Int, // metadata for player death tracking
    weaponId: Int,   // metadata for player death tracking
    weaponMaterial: Material, // metadata for player death tracking
) {
    val xc = this

    // Check if region allows explosions
    if ( !xc.canExplodeAt(location) ) {
        // Print to player cannot attack here?
        return
    }

    // Explosion id is a tag for tracking an explosion and which hitboxes
    // Have already been damaged by it. since hitboxes can be attached to
    // Multiple chunks, we need to track which hitboxes have already been
    // Damaged by this explosion. explosion id must be unique per explosion.
    val explosionId = xc.nextExplosionId()

    // Get chunks intersecting with explosion radius
    val cxmin = floor(location.x - maxDistance).toInt() shr 4
    val cxmax = ceil(location.x + maxDistance).toInt() shr 4
    val cymin = floor(location.y - maxDistance).toInt() shr 4
    val cymax = ceil(location.y + maxDistance).toInt() shr 4
    val czmin = floor(location.z - maxDistance).toInt() shr 4
    val czmax = ceil(location.z + maxDistance).toInt() shr 4

    for ( cx in cxmin..cxmax ) {
        for ( cy in cymin..cymax ) {
            for ( cz in czmin..czmax ) {
                val coord = ChunkCoord3D(cx, cy, cz)
                val hitboxesInChunk = hitboxes[coord] ?: continue

                for ( hitbox in hitboxesInChunk ) {
                    // Skip hitboxes that have already been damaged by this explosion
                    if ( hitbox.lastExplosionId == explosionId ) {
                        continue
                    }
                    hitbox.lastExplosionId = explosionId

                    val target = hitbox.entity

                    // Distance from hitbox center to explosion location,
                    // With hitbox min radius subtracted
                    val distance = (hitbox.distance(location.x.toFloat(), location.y.toFloat(), location.z.toFloat()) - hitbox.radiusMin).toDouble()

                    // Base damage based on explosion model.
                    // If base damage > 0.0, then apply damage to entity target
                    val baseDamage = baseExplosionDamage(damage, distance, radius, falloff)

                    if (baseDamage > 0.0 && target is LivingEntity) {
                        // Emit event for external plugins to read
                        try {
                            val damageEvent = XCExplosionDamageEvent(
                                target = target,
                                damage = baseDamage,
                                damageType = damageType,
                                distance = distance,
                                source = source,
                                weaponType = weaponType,
                                weaponId = weaponId,
                                weaponMaterial = weaponMaterial,
                            )
                            Bukkit.getPluginManager().callEvent(damageEvent)
                            if (damageEvent.isCancelled()) {
                                return
                            }
                        } catch ( err: Exception ) {
                            xc.logger.warning("Error calling XCExplosionDamageEvent for ${target}: ${err.message}")
                            return
                        }

                        val finalDamage = xc.explosionDamageAfterArmor(
                            baseDamage,
                            target,
                            armorReduction,
                            blastProtReduction,
                        )

                        if ( target is Player ) {
                            // Mark player entering combat
                            xc.addPlayerToCombatLogging(target)

                            // Player died
                            if ( target.getHealth() > 0.0 && finalDamage >= target.getHealth() ) {
                                xc.deathEvents[target.getUniqueId()] = XcPlayerDeathEvent(
                                    player = target,
                                    killer = source,
                                    weaponType = weaponType,
                                    weaponId = weaponId,
                                    weaponMaterial = weaponMaterial,
                                )
                            }
                        }

                        target.damage(finalDamage, null)
                        target.setNoDamageTicks(0)

                        if ( fireTicks > 0 ) {
                            target.setFireTicks(fireTicks)
                        }
                    }
                }
            }
        }
    }

    // Create block explosion if power > 0
    if ( xc.config.blockDamageExplosion && blockDamagePower > 0.0f ) {
        location.world?.createExplosion(
            location.x,
            location.y,
            location.z,
            blockDamagePower,
        )
    }

    // Queue explosion particle effect
    if ( particles != null ) {
        xc.particleExplosionQueue.add(ParticleExplosion(
            particles,
            location.world,
            location.x,
            location.y,
            location.z,
            true,
        ))
    }
}