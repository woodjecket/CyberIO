package net.liplum.holo

import mindustry.game.Team

val Team2HoloCapacity: HashMap<Team, Int> = HashMap()
val Team.holoCapacity: Int
    get() = Team2HoloCapacity.getOrPut(this) { this.updateHoloCapacity() }

fun Team.updateHoloCapacity(): Int {
    var count = 0
    val buildings = data().buildings
    if (buildings.isEmpty) return 0
    buildings.each {
        if (it is HoloProjector.HoloPBuild) {
            val block = it.block
            if (block is HoloProjector) {
                count += block.holoUnitCapacity
            }
        }
    }
    Team2HoloCapacity[this] = count
    return count
}

fun HoloUnitType.canCreateHoloUnitIn(team: Team): Boolean {
    val total = team.data().countType(this)
    return total < team.holoCapacity
}

fun Team.getStringHoloCap(): String {
    val cap = this.holoCapacity
    return if (cap >= Int.MAX_VALUE - 1) "∞" else cap.toString() + ""
}

fun HoloUnitType.pctOfTeamOwns(team: Team) =
    team.data().countType(this).toFloat() / team.holoCapacity