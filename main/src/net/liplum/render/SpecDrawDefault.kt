package net.liplum.render

import arc.graphics.g2d.Draw
import arc.util.Eachable
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.draw.DrawBlock
import net.liplum.lib.TRs
import net.liplum.utils.atlasX

class SpecDrawDefault : DrawBlock() {
    override fun load(block: Block) = block.run {
        region.set(this.atlasX())
    }

    override fun draw(build: Building) = build.run {
        Draw.rect(block.region, build.x, build.y, build.drawrot())
    }

    override fun drawPlan(block: Block, plan: BuildPlan?, list: Eachable<BuildPlan?>?) {
        block.drawDefaultPlanRegion(plan, list)
    }

    override fun icons(block: Block): TRs = block.run {
        return arrayOf(region)
    }
}