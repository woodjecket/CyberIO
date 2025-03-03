package net.liplum.api.holo

import mindustry.game.EventType
import mindustry.gen.Healthc
import net.liplum.annotations.SubscribeEvent

interface IHoloEntity {
    fun killThoroughly()
    val minHealthProportion: Float
    var restRestore: Float

    companion object {
        @JvmStatic
        @SubscribeEvent(EventType.BlockBuildEndEvent::class)
        fun registerHoloEntityInitHealth(e: EventType.BlockBuildEndEvent) {
            val he = e.tile.build
            if (he is IHoloEntity) {
                he.restRestore = he.maxHealth
                he.health = he.maxHealth * he.minHealthProportion * 0.9f
            }
        }

        val <T> T.minHealth: Float where T : Healthc, T : IHoloEntity
            get() = maxHealth() * minHealthProportion
    }
}