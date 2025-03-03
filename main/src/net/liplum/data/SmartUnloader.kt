package net.liplum.data

import arc.func.Prov
import arc.graphics.Color
import arc.math.Mathf
import arc.math.geom.Point2
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.Structs
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.type.Item
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.*
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.common.Changed
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.common.util.DoMultipleBool
import net.liplum.lib.Serialized
import net.liplum.lib.assets.TR
import net.liplum.mdt.*
import net.liplum.mdt.animation.anims.Animation
import net.liplum.mdt.animation.anims.AnimationObj
import net.liplum.mdt.animation.anis.AniState
import net.liplum.mdt.animation.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.ui.bars.removeItemsInBar
import net.liplum.mdt.utils.*
import net.liplum.util.*
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.log2

private typealias AniStateU = AniState<SmartUnloader, SmartUnloader.SmartUnloaderBuild>
private typealias SmartDIS = SmartDistributor.SmartDistributorBuild

open class SmartUnloader(name: String) : AniedBlock<SmartUnloader, SmartUnloader.SmartUnloaderBuild>(name) {
    /**
     * The lager the number the slower the unloading speed. Belongs to [0,+inf)
     */
    @JvmField var unloadSpeed = 1f
    @JvmField var maxConnection = 5
    @ClientOnly lateinit var ShrinkingAnim: Animation
    @ClientOnly lateinit var CoverTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var powerUsePerItem = 2.5f
    @JvmField var powerUsePerConnection = 2f
    @JvmField var powerUseBasic = 1.5f
    @JvmField val TransferTimer = timers++
    @JvmField var unloaderComparator: Comparator<Building> = Structs.comparingBool { it.block.highUnloadPriority }
    @JvmField var boost2Count: (Float) -> Int = {
        if (it <= 1.1f)
            1
        else if (it in 1.1f..2.1f)
            2
        else if (it in 2.1f..3f)
            3
        else
            Mathf.round(log2(it + 5.1f))
    }
    @ClientOnly @JvmField var SendingTime = 60f
    @ClientOnly @JvmField var UnloadTime = 60f
    @ClientOnly @JvmField var ShrinkingAnimFrames = 13
    @ClientOnly @JvmField var ShrinkingAnimDuration = 120f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f

    init {
        buildType = Prov { SmartUnloaderBuild() }
        solid = true
        update = true
        hasPower = true
        hasItems = true
        group = BlockGroup.transportation
        noUpdateDisabled = true
        unloadable = false
        canOverdrive = true
        unloadable = false
        itemCapacity = 50
        schematicPriority = 20
        allowConfigInventory = false
        configurable = true
        saveConfig = true
        acceptsItems = false
        /**
         * For connect
         */
        config(Integer::class.java) { obj: SmartUnloaderBuild, receiverPackedPos ->
            obj.addReceiverFromRemote(receiverPackedPos.toInt())
        }
        configClear<SmartUnloaderBuild> {
            it.clearReceivers()
        }
        /**
         * For schematic
         */
        config(Array<Point2>::class.java) { obj: SmartUnloaderBuild, relatives ->
            obj.resolveRelativePosFromRemote(relatives)
        }
    }

    open fun initPowerUse() {
        consumePowerDynamic<SmartUnloaderBuild> {
            (powerUseBasic
                    + powerUsePerItem * it.needUnloadItems.size
                    + powerUsePerConnection * it.connectedReceivers.size)
        }
    }

    override fun init() {
        initPowerUse()
        super.init()
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        ShrinkingAnim = this.autoAnim("shrink", ShrinkingAnimFrames, ShrinkingAnimDuration)
        CoverTR = this.sub("cover")
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.powerUse)
        addPowerUseStats()
        addLinkRangeStats(maxRange)
        addMaxReceiverStats(1)
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        drawPlacingMaxRange(x, y, maxRange, R.C.Sender)
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            removeItemsInBar()
        }
        DebugOnly {
            addReceiverInfo<SmartUnloaderBuild>()
            AddBar<SmartUnloaderBuild>("last-unloading",
                { "Last Unload: ${lastUnloadTime.toInt()}" },
                { Pal.bar },
                { lastUnloadTime / UnloadTime }
            )
            AddBar<SmartUnloaderBuild>("last-sending",
                { "Last Send: ${lastSendingTime.toInt()}" },
                { Pal.bar },
                { lastSendingTime / SendingTime }
            )
            AddBar<SmartUnloaderBuild>("queue",
                { "Queue: ${queue.size}" },
                { Pal.bar },
                { queue.size.toFloat() / maxReceiverConnection }
            )
        }
    }

    open inner class SmartUnloaderBuild : AniedBlock<SmartUnloader, SmartUnloaderBuild>.AniedBuild(),
        IDataSender {
        override val maxRange = this@SmartUnloader.maxRange
        @Serialized
        var receivers = OrderedSet<Int>()
        /**
         * When this smart unloader was restored by schematic, it should check whether the receiver was built.
         *
         * It contains absolute points.
         */
        var queue = LinkedList<Point2>()
        var nearby: Seq<Building> = Seq()
        var trackers: Array<Tracker> = Array(ItemTypeAmount()) {
            Tracker(maxConnection)
        }
        var needUnloadItems: OrderedSet<Item> = OrderedSet(ItemTypeAmount())
        var unloadedNearbyIndex = 0
            set(value) {
                field = (if (nearby.isEmpty)
                    0
                else
                    value.absoluteValue % nearby.size)
            }
        var unloadTimer = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly var lastUnloadTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly var lastSendingTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly val isSending: Boolean
            get() = lastSendingTime < SendingTime
        @ClientOnly val isUnloading: Boolean
            get() = lastUnloadTime < UnloadTime
        @ClientOnly lateinit var shrinkingAnimObj: AnimationObj
        var justRestored = false
        @ClientOnly
        var lastSenderColor = Changed.empty<Color>()
        @ClientOnly
        var targetSenderColor = R.C.Sender
        @ClientOnly
        override val senderColor: Color
            get() = transitionColor(lastSenderColor, targetSenderColor)

        init {
            ClientOnly {
                shrinkingAnimObj = ShrinkingAnim.gen()
            }
        }

        open fun updateUnloaded() {
            nearby.clear()
            for (b in proximity) {
                if (b.block.unloadable) {
                    nearby.add(b)
                }
            }
            nearby.sort(unloaderComparator)
            unloadedNearbyIndex = 0
        }

        fun genRelativeAllPos(): Array<Point2> {
            return receivers.map {
                it.unpack().apply {
                    x -= tile.x
                    y -= tile.y
                }
            }.toTypedArray()
        }

        override fun config(): Any? {
            return genRelativeAllPos()
        }

        open fun checkQueue() {
            if (queue.isNotEmpty()) {
                val it = queue.iterator()
                while (it.hasNext()) {
                    val pos = it.next()
                    val dr = pos.dr()
                    if (dr != null) {
                        connectToSync(dr)
                        it.remove()
                    }
                }
            }
        }
        var lastTileChange = -2

        override fun updateTile() {
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkReceiversPos()
            }
            if (justRestored) {
                updateTracker()
                justRestored = false
            }
            checkQueue()
            ClientOnly {
                lastUnloadTime += Time.delta
                lastSendingTime += Time.delta
            }
            if (receivers.isEmpty) return
            if (efficiency <= 0f) return

            unloadTimer += delta()
            if (unloadTimer >= unloadSpeed) {
                unloadTimer %= unloadSpeed
                val unloaded = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::unload)
                ClientOnly {
                    if (unloaded) {
                        lastUnloadTime = 0f
                    }
                }
            }
            val sent = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::sendData)
            ClientOnly {
                if (sent) {
                    lastSendingTime = 0f
                }
            }
        }

        open fun Item.canBeUnloadedToThis(): Boolean {
            return items.get(this) < getMaximumAccepted(this)
        }

        open fun unload(): Boolean {
            var unloaded = false
            if (!nearby.isEmpty) {
                val container = nearby[unloadedNearbyIndex]
                for (item in needUnloadItems) {
                    val containerItems = container.items ?: continue
                    if (!item.canBeUnloadedToThis()) {
                        continue
                    }
                    if (containerItems.has(item)) {
                        containerItems.remove(item, 1)
                        this.items.add(item, 1)
                        unloaded = true
                    }
                }
                unloadedNearbyIndex++
            }
            return unloaded
        }

        open fun sendData(): Boolean {
            var sent = false
            for (item in needUnloadItems) {
                val tracker = trackers[item.ID]
                if (tracker.receivers.isEmpty() || !this.items.has(item))
                    continue
                val receiver = tracker.receivers[tracker.curIndex]
                if (receiver.getAcceptedAmount(this, item).isAccepted()) {
                    this.items.remove(item, 1)
                    this.sendDataTo(receiver, item, 1)
                    sent = true
                }
                tracker.curIndex++
            }
            return sent
        }

        open fun clearTrackers() {
            trackers.forEach {
                it.clear()
            }
        }

        open fun onReceiverRequirementsUpdated(receiver: IDataReceiver) {
            updateTracker()
        }

        open fun updateTracker() {
            clearTrackers()
            needUnloadItems.clear()
            for (receiverPos in receivers) {
                val receiver = receiverPos.dr()
                if (receiver != null) {
                    val reqs = receiver.requirements
                    if (reqs != null) {
                        for (req in reqs) {
                            val tracker = trackers[req.ID]
                            if (tracker.canAddMore()) {
                                needUnloadItems.add(req)
                                tracker.add(receiver)
                            }
                        }
                    }
                }
            }
            ClientOnly {
                val c = Color.gray.cpy()
                var hasAny = false
                for (tracker in trackers) {
                    val color = tracker.genMixedColor()
                    if (color != null) {
                        hasAny = true
                        c.lerp(color, 0.5f)
                    }
                }
                lastSenderColor = Changed(old = targetSenderColor)
                targetSenderColor = if (hasAny) c else R.C.Sender
            }
            DebugOnly {
                needUnloadItemsText = genNeedUnloadItemsText()
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateUnloaded()
            updateTracker()
        }
        @DebugOnly
        var needUnloadItemsText: String = ""
        @DebugOnly
        fun genNeedUnloadItemsText() = needUnloadItems.genText()
        override fun drawSelect() {
            this.drawDataNetGraph()
            DebugOnly {
                if (needUnloadItemsText.isNotEmpty()) {
                    drawPlaceText(needUnloadItemsText, tileX(), tileY(), true)
                }
            }
            drawSelectedMaxRange()
        }

        override fun drawConfigure() {
            super.drawConfigure()
            this.drawDataNetGraph()
            drawConfiguringMaxRange()
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            val pos = other.pos()
            if (pos in receivers) {
                pos.dr()?.let { disconnectFromSync(it) }
                return false
            }
            if (other is IDataReceiver) {
                if (maxRange > 0f && other.dst(this) >= maxRange) {
                    postOverRangeOn(other)
                } else {
                    if (canHaveMoreReceiverConnection) {
                        if (other.acceptConnectionTo(this)) {
                            connectToSync(other)
                        } else {
                            postFullSenderOn(other)
                        }
                    } else {
                        postFullReceiverOn(other)
                    }
                }
                return false
            }
            return true
        }
        @CalledBySync
        fun resolveRelativePosFromRemote(relatives: Array<Point2>) {
            for (relative in relatives) {
                val rel = relative.cpy()
                rel.x += tile.x
                rel.y += tile.y
                val abs = rel
                val dr = abs.dr()
                if (dr != null) {
                    dr.onConnectTo(this)
                    this.connectReceiver(dr)
                } else {
                    queue.add(abs)
                }
            }
        }
        @CalledBySync
        open fun addReceiverFromRemote(pos: Int) {
            if (pos in receivers) {
                pos.dr()?.let {
                    disconnectReceiver(it)
                    it.onDisconnectFrom(this)
                }
            } else {
                pos.dr()?.let {
                    connectReceiver(it)
                    it.onConnectTo(this)
                }
            }
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            resubscribeRequirementUpdated()
        }

        open fun resubscribeRequirementUpdated() {
            receivers.forEach { pos ->
                pos.dr()?.let {
                    it.onRequirementUpdated += ::onReceiverRequirementsUpdated
                }
            }
        }
        @CalledBySync
        open fun connectReceiver(receiver: IDataReceiver) {
            if (receivers.add(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated += ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun disconnectReceiver(receiver: IDataReceiver) {
            if (receivers.remove(receiver.building.pos())) {
                updateTracker()
                receiver.onRequirementUpdated -= ::onReceiverRequirementsUpdated
            }
        }
        @CalledBySync
        open fun clearReceivers() {
            receivers.forEach { pos ->
                pos.dr()?.let {
                    it.onDisconnectFrom(this)
                    it.onRequirementUpdated -= ::onReceiverRequirementsUpdated
                }
            }
            receivers.clear()
            updateTracker()
        }
        @SendDataPack
        override fun connectToSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos !in receivers) {
                configure(pos)
            }
        }
        @SendDataPack
        override fun disconnectFromSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos in receivers) {
                configure(pos)
            }
        }

        override fun beforeDraw() {
            if (canConsume() && isUnloading && isSending) {
                shrinkingAnimObj.spend(delta())
            }
        }

        override val maxReceiverConnection = maxConnection
        override fun acceptItem(source: Building, item: Item) = false
        override val connectedReceivers: OrderedSet<Int> = receivers
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            receivers.read(read)
            justRestored = true
        }

        override fun write(write: Writes) {
            super.write(write)
            receivers.write(write)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IDataReceiver) connectToSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = buildAt(p1, p2)
                    if (receiver is IDataReceiver) connectToSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }
    }

    @ClientOnly lateinit var UnloadingAni: AniStateU
    @ClientOnly lateinit var NoPowerAni: AniStateU
    @ClientOnly lateinit var BlockedAni: AniStateU
    override fun genAniConfig() {
        config {
            From(NoPowerAni) To UnloadingAni When {
                canConsume()
            }

            From(UnloadingAni) To NoPowerAni When {
                !canConsume()
            } To BlockedAni When {
                !isUnloading || !isSending
            }

            From(BlockedAni) To NoPowerAni When {
                !canConsume()
            } To UnloadingAni When {
                isUnloading && isSending
            }
        }
    }

    override fun genAniState() {
        UnloadingAni = addAniState("Unloading") {
            shrinkingAnimObj.draw(x, y)
        }
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        BlockedAni = addAniState("Blocked") {
            shrinkingAnimObj.draw(R.C.Stop, x, y)
        }
    }
}