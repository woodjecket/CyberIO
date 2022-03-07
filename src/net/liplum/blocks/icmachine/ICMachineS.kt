package net.liplum.blocks.icmachine

import arc.graphics.g2d.Draw
import arc.math.Mathf
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.ui.Bar
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.blocks.AniedCrafter
import net.liplum.utils.*
import kotlin.math.sqrt

private typealias AniStateMS = AniState<ICMachineS, ICMachineS.ICMachineSBuild>

private val P2ALeft = (3 - sqrt(3f)) / 6
private val P2ARight = (3 + sqrt(3f)) / 6
private val P2A: FUNC = {
    if (it in P2ALeft..P2ARight)
        1f
    else
        -6 * it * it + 6 * it
}

open class ICMachineS(name: String) : AniedCrafter<ICMachineS, ICMachineS.ICMachineSBuild>(name) {
    @ClientOnly lateinit var IdleState: AniStateMS
    @ClientOnly lateinit var WorkingState: AniStateMS
    @ClientOnly var phase = 3
    @ClientOnly lateinit var Baffle: TR
    @ClientOnly lateinit var processIcons: Array<Item>
    @ClientOnly @JvmField var baffleMinAlpha = 0.65f
    @ClientOnly @JvmField var baffleMaxAlpha = 1f
    override fun genAniState() {
        IdleState = addAniState("Idle") { _, build ->
            Draw.alpha(build.baffleAlpha)
            Draw.rect(Baffle, build.x, build.y)
        }
        WorkingState = addAniState("Working") { _, build ->
            Draw.alpha(1f)
            val animProgress = build.progress * phase
            val curIndex = animProgress.toInt().coerceIn(0, processIcons.size - 1)
            val curTR = processIcons[curIndex].fullIcon
            val progressInCurPeriod = build.progress % (1f / phase) / (1f / phase)
            Draw.alpha(P2A(progressInCurPeriod))
            Draw.rect(curTR, build.x, build.y)
            Draw.alpha(build.baffleAlpha)
            Draw.rect(Baffle, build.x, build.y)
        }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.add<ICMachineSBuild>(
                R.Bar.AlphaN
            ) {
                Bar(
                    { R.Bar.Alpha.bundle(it.baffleAlpha.percentI) },
                    { Pal.powerBar },
                    { it.baffleAlpha / 1f }
                )
            }
        }
    }

    override fun load() {
        super.load()
        Baffle = this.subA("baffle")
    }

    override fun icons() = arrayOf(
        region, Baffle
    )

    override fun genAniConfig() {
        config {
            defaultState(IdleState)
            From(IdleState) To WorkingState When { _, build ->
                !Mathf.zero(build.progress) && !Mathf.zero(build.power.status)
            }
            From(WorkingState) To IdleState When { _, build ->
                Mathf.zero(build.progress) || Mathf.zero(build.power.status)
            }
            build()
        }
    }

    open inner class ICMachineSBuild : AniedCrafter<ICMachineS, ICMachineSBuild>.AniedCrafterBuild() {
        @ClientOnly open var baffleAlpha = baffleMinAlpha
            set(value) {
                field = value.coerceIn(baffleMinAlpha, baffleMaxAlpha)
            }
        var processEffectShown = true
        override fun onAniStateMUpdate() {
            when (aniStateM.curState) {
                WorkingState -> {
                    baffleAlpha -= 0.01f * delta()
                }
                IdleState -> baffleAlpha += 0.01f * delta()
            }
        }

        override fun updateTile() {
            super.updateTile()
            if (progress % 0.1 < 0.01f) {
                if (!processEffectShown) {
                    craftEffect.at(this)
                    processEffectShown = true
                }
            } else {
                processEffectShown = false
            }
        }
    }
}