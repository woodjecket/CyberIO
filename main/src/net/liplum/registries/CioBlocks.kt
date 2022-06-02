package net.liplum.registries

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.Texture
import arc.struct.Seq
import arc.util.Time
import mindustry.Vars
import mindustry.content.*
import mindustry.entities.bullet.LaserBulletType
import mindustry.entities.bullet.LightningBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.pattern.ShootAlternate
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import mindustry.world.blocks.defense.BaseShield
import mindustry.world.blocks.defense.DirectionalForceProjector
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.payloads.*
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.blocks.production.HeatCrafter
import mindustry.world.blocks.sandbox.PowerSource
import mindustry.world.draw.DrawDefault
import mindustry.world.draw.DrawLiquidTile
import mindustry.world.draw.DrawMulti
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.api.brain.UT
import net.liplum.api.brain.Upgrade
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.bomb.ZipBomb
import net.liplum.blocks.cloud.Cloud
import net.liplum.blocks.ddos.DDoS
import net.liplum.blocks.debugonly.AdjustableOverdrive
import net.liplum.blocks.decentralizer.Decentralizer
import net.liplum.blocks.deleter.Deleter
import net.liplum.blocks.icmachine.ICMachine
import net.liplum.blocks.icmachine.ICMachineS
import net.liplum.blocks.jammer.Jammer
import net.liplum.blocks.jammer.JammingLaser
import net.liplum.blocks.power.WirelessTower
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.prism.PrismObelisk
import net.liplum.blocks.stream.StreamClient
import net.liplum.blocks.stream.StreamHost
import net.liplum.blocks.stream.StreamServer
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.blocks.tmtrainer.TMTRAINER
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.brains.*
import net.liplum.bullets.BBulletType
import net.liplum.bullets.ItemBulletType
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.data.*
import net.liplum.holo.*
import net.liplum.mdt.ui.DynamicContentInfoDialog.Companion.registerDynamicInfo
import net.liplum.render.SpecDrawConstruct
import net.liplum.render.SpecDrawDefault
import net.liplum.seffects.StaticFx
import net.liplum.utils.globalAnim

object CioBlocks {
    @JvmStatic lateinit var icMachine: GenericCrafter
    @JvmStatic lateinit var icAssembler: GenericCrafter
    @JvmStatic lateinit var icMachineSmall: GenericCrafter
    @JvmStatic lateinit var receiver: Receiver
    @JvmStatic lateinit var sender: Sender
    @JvmStatic lateinit var virus: Virus
    @JvmStatic lateinit var landProjector: LandProjector
    @JvmStatic lateinit var holoFloor: HoloFloor
    @JvmStatic lateinit var underdriveProjector: UnderdriveProjector
    @JvmStatic lateinit var antiVirus: AntiVirus
    @JvmStatic lateinit var cloud: Cloud
    @JvmStatic lateinit var prism: Prism
    @JvmStatic lateinit var prismObelisk: PrismObelisk
    @JvmStatic lateinit var deleter: Deleter
    @JvmStatic lateinit var hyperOverdriveSphere: AdjustableOverdrive
    @JvmStatic lateinit var holoWall: HoloWall
    @JvmStatic lateinit var holoWallLarge: HoloWall
    @JvmStatic lateinit var TMTRAINER: TMTRAINER
    @JvmStatic lateinit var smartDistributor: SmartDistributor
    @JvmStatic lateinit var smartUnloader: SmartUnloader
    @JvmStatic lateinit var streamClient: StreamClient
    @JvmStatic lateinit var streamHost: StreamHost
    @JvmStatic lateinit var streamServer: StreamServer
    @JvmStatic lateinit var jammer: Jammer
    @JvmStatic lateinit var cyberionMixer: GenericCrafter
    @JvmStatic lateinit var holoProjector: HoloProjector
    @JvmStatic lateinit var aquacyberion: Floor
    @JvmStatic lateinit var stealth: Stealth
    @JvmStatic lateinit var wirelessTower: WirelessTower
    @JvmStatic lateinit var heimdall: Heimdall
    @JvmStatic lateinit var eye: Eye
    @JvmStatic lateinit var ear: Ear
    @JvmStatic lateinit var heart: Heart
    @JvmStatic lateinit var decentralizer: Decentralizer
    @JvmStatic lateinit var DDoS: DDoS
    @JvmStatic lateinit var dataCDN: DataCDN
    @JvmStatic lateinit var zipBomb: ZipBomb
    @JvmStatic lateinit var serializer: Serializer
    @DependOn("CioItems.ic")
    fun icMachine() {
        icMachine = ICMachine("ic-machine").apply {
            VanillaSpec {
                requirements(
                    Category.crafting, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.copper, 550),
                        ItemStack(Items.lead, 280),
                        ItemStack(Items.silicon, 150),
                        ItemStack(Items.graphite, 250),
                    )
                )
                health = 2000
                consumeItems(
                    //Total:100
                    ItemStack(Items.copper, 50),  //50%
                    ItemStack(Items.silicon, 20),  //20%
                    ItemStack(Items.metaglass, 30), //30%
                )
                consumePower(10f)
                craftTime = 400f
            }
            ErekirSpec {
                requirements(
                    Category.crafting, arrayOf(
                        ItemStack(CioItems.ic, 8),
                        ItemStack(Items.beryllium, 250),
                        ItemStack(Items.surgeAlloy, 80),
                        ItemStack(Items.tungsten, 100),
                        ItemStack(Items.thorium, 150),
                    )
                )
                health = 1500
                consumeItems( //Total:100
                    ItemStack(Items.tungsten, 40),  //50%
                    ItemStack(Items.beryllium, 20),  //20%
                    ItemStack(Items.silicon, 30) //30%
                )
                consumePower(8f)
                craftTime = 420f
            }
            fogRadius = 3
            outputItem = ItemStack(CioItems.ic, 2)
            size = 3
            buildCostMultiplier = 1.5f
            craftEffect = Fx.smeltsmoke
            itemCapacity = 60
        }
    }
    @DependOn("CioItems.ic")
    fun icMachineSmall() {
        icMachineSmall = ICMachineS("ic-machine-s").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(Items.copper, 150),
                    ItemStack(Items.silicon, 50),
                    ItemStack(Items.graphite, 50),
                )
            )
            health = 600
            craftTime = 1150f
            itemCapacity = 40
            consumeItems(
                ItemStack(Items.copper, 25),
                ItemStack(Items.sand, 40),
                ItemStack(Items.lead, 15),
            )
            consumePower(2f)
            outputItem = ItemStack(CioItems.ic, 1)
            fogRadius = 2
            size = 2
            craftEffect = Fx.formsmoke

            processIcons = arrayOf(
                Items.metaglass,
                Items.silicon,
                CioItems.ic
            )
        }
    }
    @DependOn("CioItems.ic")
    fun icAssembler() {
        icAssembler = GenericCrafter("ic-assembler").apply {
            VanillaSpec {
                requirements(
                    Category.crafting, arrayOf(
                        ItemStack(Items.lead, 60),
                        ItemStack(Items.graphite, 35),
                        ItemStack(Items.metaglass, 50),
                        ItemStack(Items.silicon, 50),
                    )
                )
                health = 250
                consumePower(1.2f)
                itemCapacity = 40
                consumeItems(
                    ItemStack(Items.copper, 20),
                    ItemStack(Items.silicon, 15),
                    ItemStack(Items.metaglass, 5),
                )
                craftTime = 600f
                drawer = DrawMulti(SpecDrawDefault(), SpecDrawConstruct())
            }
            ErekirSpec {
                requirements(
                    Category.crafting, arrayOf(
                        ItemStack(Items.beryllium, 60),
                        ItemStack(Items.graphite, 105),
                        ItemStack(Items.silicon, 50),
                    )
                )
                health = 250
                consumePower(1.1f)
                itemCapacity = 40
                consumeItems(
                    ItemStack(Items.tungsten, 15),
                    ItemStack(Items.beryllium, 20),
                    ItemStack(Items.silicon, 15)
                )
                craftTime = 700f
                drawer = DrawMulti(SpecDrawDefault(), SpecDrawConstruct().apply {
                    stages = 4
                })
            }
            buildType = Prov { GenericCrafterBuild() }
            fogRadius = 2
            size = 2
            outputItem = ItemStack(CioItems.ic, 1)
            craftEffect = Fx.smeltsmoke
        }
    }
    @DependOn("CioItems.ic")
    fun receiver() {
        receiver = Receiver("receiver").apply {
            VanillaSpec {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 1),
                        ItemStack(Items.lead, 150),
                        ItemStack(Items.graphite, 120),
                        ItemStack(Items.metaglass, 60),
                        ItemStack(Items.silicon, 180),
                    )
                )
                health = 200
                maxConnection = 5
                consumePower(0.5f)
            }
            ErekirSpec {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.graphite, 80),
                        ItemStack(Items.tungsten, 30),
                        ItemStack(Items.silicon, 30),
                    )
                )
                health = 150
                maxConnection = 3
                consumePower(0.8f)
            }
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun sender() {
        sender = Sender("sender").apply {
            VanillaSpec {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 1),
                        ItemStack(Items.lead, 150),
                        ItemStack(Items.graphite, 120),
                        ItemStack(Items.metaglass, 60),
                        ItemStack(Items.silicon, 180),
                    )
                )
                health = 200
                consumePower(0.5f)
            }
            ErekirSpec {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.graphite, 120),
                        ItemStack(Items.tungsten, 50),
                        ItemStack(Items.silicon, 50),
                    )
                )
                health = 180
                maxRange = 550f
                consumePower(0.8f)
            }
            replaceable = false
        }
    }
    @DependOn
    fun virus() {
        virus = Virus("virus").apply {
            requirements(
                Category.effect, BuildVisibility.sandboxOnly,
                arrayOf(
                    ItemStack(Items.sporePod, 50),
                    ItemStack(Items.pyratite, 20),
                )
            )
            buildCostMultiplier = 5f
            spreadingSpeed = 200
            maxReproductionScale = 20
            maxGeneration = 100
            inheritChildrenNumber = false
            mutationRate = 10
        }.globalAnim(30f, 3)
    }
    @DependOn("CioItems.ic")
    fun landProjector() {
        landProjector = LandProjector("land-projector").apply {
            requirements(
                Category.effect,
                if (Var.DebugMode)
                    BuildVisibility.sandboxOnly
                else
                    BuildVisibility.hidden, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.graphite, 80),
                    ItemStack(Items.thorium, 100),
                    ItemStack(Items.silicon, 50)
                )
            )
            health = 100
            size = 2
            buildCostMultiplier = 3f
        }
    }
    @DependOn
    fun holoFloor() {
        holoFloor = HoloFloor("holo-floor").apply {
            variants = 3
        }.setUninfectedFloor()
    }
    @DependOn("CioItems.ic")
    fun underdriveProjector() {
        underdriveProjector = UnderdriveProjector("underdrive-projector").apply {
            VanillaSpec {
                requirements(
                    Category.effect, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 8),
                        ItemStack(Items.copper, 300),
                        ItemStack(Items.lead, 20),
                        ItemStack(Items.silicon, 240),
                        ItemStack(Items.plastanium, 10),
                        ItemStack(Items.phaseFabric, 5),
                    )
                )
                health = 300
                powerProduction = 4.5f
                maxPowerEFFBlocksReq = 22
                maxGear = 5
            }
            ErekirSpec {
                requirements(
                    Category.effect, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 3),
                        ItemStack(Items.tungsten, 80),
                        ItemStack(Items.silicon, 50),
                        ItemStack(Items.phaseFabric, 5),
                        ItemStack(Items.surgeAlloy, 10),
                    )
                )
                health = 250
                range = 45f
                powerProduction = 2.8f
                maxPowerEFFBlocksReq = 20
                maxGear = 6
            }
            color = R.C.LightBlue
            maxSlowDownRate = 0.9f
            size = 1
        }
    }
    @DependOn("CioItems.ic")
    fun antiVirus() {
        antiVirus = AntiVirus("anti-virus").apply {
            requirements(
                Category.effect, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 100),
                    ItemStack(Items.graphite, 40),
                    ItemStack(Items.silicon, 25)
                )
            )
            health = 500
            consumePower(0.5f)
            size = 1
        }.setUninfected()
    }
    @DependOn("CioItems.ic")
    fun cloud() {
        cloud = Cloud("cloud").apply {
            requirements(
                Category.effect,
                if (Var.DebugMode)
                    BuildVisibility.sandboxOnly
                else
                    BuildVisibility.hidden, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.titanium, 1000),
                    ItemStack(Items.thorium, 1000),
                )
            )
            size = 3
            buildCostMultiplier = 2f
            health = 500 * size * size
            consumePower(1f)
        }
    }
    @DependOn
    fun hyperOverdriveSphere() {
        hyperOverdriveSphere = AdjustableOverdrive("hyper-overdrive-sphere").apply {
            requirements(
                Category.effect,
                if (Var.DebugMode) BuildVisibility.shown
                else BuildVisibility.sandboxOnly,
                emptyArray()
            )
            size = 3
            maxBoost = 50f
            minBoost = 0.5f
            speedBoost = 50f
            range = 1000f
        }
    }
    @DependOn("CioItems.ic")
    fun prism() {
        prism = Prism("prism").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 15),
                        ItemStack(Items.copper, 250),
                        ItemStack(Items.metaglass, 350),
                        ItemStack(Items.titanium, 50),
                    )
                )
                health = 2500
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 30),
                        ItemStack(Items.carbide, 150),
                        ItemStack(Items.surgeAlloy, 250),
                        ItemStack(Items.tungsten, 150),
                    )
                )
                health = 1500
            }
            buildCostMultiplier = 2f
            size = 4
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.prism"
    )
    fun prismObelisk() {
        prismObelisk = PrismObelisk("prism-obelisk").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 3),
                        ItemStack(Items.copper, 60),
                        ItemStack(Items.plastanium, 120),
                        ItemStack(Items.metaglass, 240),
                        ItemStack(Items.titanium, 10),
                    )
                )
                health = 1000
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 12),
                        ItemStack(Items.beryllium, 60),
                        ItemStack(Items.surgeAlloy, 120),
                        ItemStack(Items.tungsten, 80),
                    )
                )
                health = 750
            }
            size = 2
            prismType = prism
        }
    }
    @DependOn("CioItems.ic")
    fun deleter() {
        deleter = Deleter("deleter").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 9),
                        ItemStack(Items.graphite, 100),
                        ItemStack(Items.silicon, 60),
                        ItemStack(Items.thorium, 250),
                        ItemStack(Items.surgeAlloy, 50),
                    )
                )
                cooldownTime = 20f
                recoil = 5f
                range = 180f
                reload = 15f
                consumePower(3f)
                extraLostHpBounce = 0.005f
                scaledHealth = 200f
                executeProportion = 0.2f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 15),
                        ItemStack(Items.surgeAlloy, 80),
                        ItemStack(Items.tungsten, 120),
                        ItemStack(Items.oxide, 180),
                    )
                )
                cooldownTime = 18f
                recoil = 5f
                range = 200f
                reload = 14f
                consumePower(4f)
                extraLostHpBounce = 0.003f
                scaledHealth = 200f
                scaledHealth = 80f
                executeProportion = 0.18f
            }
            size = 3
            buildCostMultiplier = 1.5f
            shootSound = Sounds.lasershoot
            configBullet {
                damage = 0.5f
                pierceCap = 3
            }
        }
    }
    @DependOn("CioItems.ic")
    fun holoWall() {
        holoWall = HoloWall("holo-wall").apply {
            VanillaSpec {
                requirements(
                    Category.defense, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 1),
                        ItemStack(Items.silicon, 6),
                        ItemStack(Items.titanium, 12),
                        ItemStack(Items.plastanium, 10),
                    )
                )
                scaledHealth = 600f
                restoreReload = 10 * 60f
            }
            ErekirSpec {
                requirements(
                    Category.defense, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 3),
                        ItemStack(Items.oxide, 8),
                        ItemStack(Items.tungsten, 12),
                    )
                )
                scaledHealth = 425f
                restoreReload = 15 * 60f
                needPower = true
                powerCapacity = 300f
                powerUseForChargePreUnit = 0.2f
            }
            size = 1
            buildCostMultiplier = 3.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.holoWall"
    )
    fun holoWallLarge() {
        holoWallLarge = HoloWall("holo-wall-large").apply {
            VanillaSpec {
                requirements(
                    Category.defense, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.silicon, 24),
                        ItemStack(Items.titanium, 48),
                        ItemStack(Items.plastanium, 40),
                    )
                )
                restoreReload = 15 * 60f
                health = 550 * 5
            }
            ErekirSpec {
                requirements(
                    Category.defense, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 6),
                        ItemStack(Items.oxide, 24),
                        ItemStack(Items.tungsten, 40),
                        ItemStack(Items.surgeAlloy, 10),
                    )
                )
                restoreReload = 20 * 60f
                scaledHealth = 450f
                needPower = true
                powerCapacity = 800f
                powerUseForChargePreUnit = 0.3f
            }
            squareSprite = false
            size = 2
            buildCostMultiplier = 4.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBulletTypes.radiationInterference",
        "CioBulletTypes.virus",
    )
    fun TMTRAINER() {
        TMTRAINER = TMTRAINER("TMTRAINER").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.titanium, 100),
                        ItemStack(Items.graphite, 100),
                        ItemStack(Items.silicon, 50),
                    )
                )
                ammo(
                    Items.sporePod, CioBulletTypes.virus,
                    Items.thorium, CioBulletTypes.radiationInterference,
                )
                shoot = ShootAlternate().apply {
                    spread = 4f
                }
                scaledHealth = 250f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.tungsten, 100),
                        ItemStack(Items.graphite, 100),
                        ItemStack(Items.surgeAlloy, 50),
                    )
                )
                //TODO: Create a new bullet instead?
                ammo(
                    Items.tungsten, CioBulletTypes.virus,
                    Items.thorium, CioBulletTypes.radiationInterference,
                )
                shoot = ShootAlternate().apply {
                    spread = 6f
                }
                scaledHealth = 270f
            }
            inaccuracy = 1f
            rotateSpeed = 10f
            maxAmmo = 80
            reload = 5f
            range = 260f
            shootCone = 15f
            size = 4
        }.registerDynamicInfo()
    }
    @Subscribe(Trigger.update, Only.client)
    fun TMTRAINER_RandomName() {
        if (Time.globalTime % Var.UpdateFrequency < 1f && CioMod.ContentLoaded) {
            TMTRAINER.localizedName = RandomName.one(8)
            TMTRAINER.description = RandomName.one(25)
        }
    }
    @DependOn("CioItems.ic")
    fun smartDistributor() {
        smartDistributor = SmartDistributor("smart-distributor").apply {
            VanillaSpec {
                requirements(
                    Category.distribution, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 15),
                        ItemStack(Items.copper, 550),
                        ItemStack(Items.silicon, 210),
                        ItemStack(Items.plastanium, 80),
                        ItemStack(Items.thorium, 140),
                        ItemStack(Items.surgeAlloy, 50),
                    )
                )
                scaledHealth = 800f
            }
            ErekirSpec {
                requirements(
                    Category.distribution, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 30),
                        ItemStack(Items.tungsten, 180),
                        ItemStack(Items.carbide, 80),
                        ItemStack(Items.surgeAlloy, 100),
                        ItemStack(Items.phaseFabric, 50),
                    )
                )
                scaledHealth = 500f
            }
            size = 2
            ArrowsAnimFrames = 4
            ArrowsAnimDuration = 12f
        }
    }
    @DependOn("CioItems.ic")
    fun smartUnloader() {
        smartUnloader = SmartUnloader("smart-unloader").apply {
            VanillaSpec {
                requirements(
                    Category.distribution, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.lead, 350),
                        ItemStack(Items.silicon, 210),
                        ItemStack(Items.graphite, 150),
                        ItemStack(Items.titanium, 50),
                    )
                )
                unloadSpeed = 5f
                scaledHealth = 300f
            }
            ErekirSpec {
                requirements(
                    Category.distribution, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.tungsten, 350),
                        ItemStack(Items.carbide, 80),
                        ItemStack(Items.surgeAlloy, 100),
                    )
                )
                unloadSpeed = 6f
                scaledHealth = 200f
                maxRange = 800f
            }
            size = 2
            ShrinkingAnimFrames = 7
            ShrinkingAnimDuration = 25f
        }
    }
    @DependOn("CioItems.ic")
    fun streamClient() {
        streamClient = StreamClient("stream-client").apply {
            VanillaSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.metaglass, 40),
                        ItemStack(Items.silicon, 20),
                        ItemStack(Items.graphite, 40),
                        ItemStack(Items.titanium, 10),
                    )
                )
                health = 300
                maxConnection = 5
                consumePower(0.7f)
                liquidCapacity = 200f
            }
            ErekirSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 6),
                        ItemStack(Items.beryllium, 40),
                        ItemStack(Items.silicon, 20),
                        ItemStack(Items.tungsten, 40),
                    )
                )
                health = 250
                maxConnection = 3
                consumePower(1f)
                liquidCapacity = 80f
            }
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun streamHost() {
        streamHost = StreamHost("stream-host").apply {
            VanillaSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.lead, 100),
                        ItemStack(Items.metaglass, 600),
                        ItemStack(Items.silicon, 80),
                        ItemStack(Items.graphite, 60),
                        ItemStack(Items.titanium, 40),
                        ItemStack(Items.plastanium, 20),
                    )
                )
                scaledHealth = 500f
                powerUseBase = 1f
                powerUsePerConnection = 1f
                networkSpeed = 3f
                liquidCapacity = 800f
                maxConnection = 5
            }
            ErekirSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 15),
                        ItemStack(Items.tungsten, 200),
                        ItemStack(Items.carbide, 80),
                        ItemStack(Items.beryllium, 100),
                    )
                )
                scaledHealth = 350f
                powerUseBase = 1.4f
                powerUsePerConnection = 1.2f
                networkSpeed = 2.6f
                liquidCapacity = 1200f
                maxConnection = 3
                maxRange = 500f
            }
            size = 2
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun streamServer() {
        streamServer = StreamServer("stream-server").apply {
            VanillaSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 16),
                        ItemStack(Items.copper, 1200),
                        ItemStack(Items.lead, 400),
                        ItemStack(Items.metaglass, 1200),
                        ItemStack(Items.silicon, 320),
                        ItemStack(Items.thorium, 40),
                        ItemStack(Items.phaseFabric, 120),
                    )
                )
                scaledHealth = 500f
                networkSpeed = 15f
                researchCostMultiplier = 0.7f
                powerUseBase = 2f
                maxConnection = 5
                powerUsePerConnection = 2f
                liquidCapacity = 2000f
            }
            ErekirSpec {
                requirements(
                    Category.liquid, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 16),
                        ItemStack(Items.tungsten, 500),
                        ItemStack(Items.carbide, 400),
                        ItemStack(Items.beryllium, 800),
                        ItemStack(Items.surgeAlloy, 320),
                        ItemStack(Items.phaseFabric, 120),
                    )
                )
                scaledHealth = 380f
                networkSpeed = 15f
                liquidCapacity = 3000f
                researchCostMultiplier = 0.6f
                powerUseBase = 2.5f
                maxConnection = 8
                maxRange = 1800f
                powerUsePerConnection = 3.8f
            }
            squareSprite = false
            size = 3
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun jammer() {
        jammer = Jammer("jammer").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 8),
                        ItemStack(Items.lead, 350),
                        ItemStack(Items.thorium, 200),
                        ItemStack(Items.surgeAlloy, 150),
                    )
                )
                scaledHealth = 250f
                coolant = consumeCoolant(0.3f)
                consumePower(15f)
                shootDuration = 180f
                range = 195f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 8),
                        ItemStack(Items.surgeAlloy, 350),
                        ItemStack(Items.thorium, 200),
                        ItemStack(Items.carbide, 150),
                    )
                )
                scaledHealth = 350f
                coolant = consumeCoolant(0.5f)
                consumePower(12f)
                shootDuration = 200f
                range = 165f
            }
            size = 3
            shootEffect = StaticFx
            shootCone = 40f
            recoil = 4f
            shake = 2f
            shootDuration = 150f
            reload = 40f
            firingMoveFract = 1f
            shootSound = CioSounds.jammerPreShoot
            loopSound = CioSounds.tvStatic
            loopSoundVolume = 0.5f
            rotateSpeed = 2f

            shootType = JammingLaser().apply {
                VanillaSpec {
                    damage = 120f
                    length = 220f
                    drawSize = 300f
                }
                ErekirSpec {
                    damage = 180f
                    length = 180f
                    drawSize = 280f
                }
                divisions = 5
                hitEffect = StaticFx
                hitColor = Color.white
                status = CioSEffects.static
                incendChance = 0.4f
                incendSpread = 5f
                incendAmount = 1
                ammoMultiplier = 1f
            }
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioLiquids.cyberion"
    )
    fun cyberionMixer() {
        VanillaSpec {
            cyberionMixer = GenericCrafter("cyberion-mixer").apply {
                requirements(
                    Category.crafting, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 4),
                        ItemStack(Items.lead, 100),
                        ItemStack(Items.titanium, 100),
                        ItemStack(Items.metaglass, 50),
                    )
                )
                buildType = Prov { GenericCrafterBuild() }
                scaledHealth = 60f
                liquidCapacity = 20f
                outputLiquid = LiquidStack(CioLiquids.cyberion, 0.3f)
                craftTime = 100f
                consumePower(1.5f)
                consumeItem(Items.thorium, 1)
                consumeLiquid(Liquids.cryofluid, 0.3f)
                // TODO: use default drawer temporarily
                //drawer = CyberionMixerDrawer(S.Hologram, S.HologramDark)
                drawer = DrawMulti(DrawLiquidTile(CioLiquids.cyberion), DrawDefault())
                size = 3
            }
        }
        ErekirSpec {
            cyberionMixer = HeatCrafter("cyberion-mixer").apply {
                requirements(
                    Category.crafting, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.carbide, 80),
                        ItemStack(Items.oxide, 80),
                        ItemStack(Items.surgeAlloy, 80),
                    )
                )
                buildType = Prov { HeatCrafterBuild() }
                scaledHealth = 100f
                liquidCapacity = 50f
                craftTime = 90f
                consumePower(1.8f)
                consumeLiquid(Liquids.slag, 0.35f)
                consumeItem(Items.oxide, 1)
                heatRequirement = 8f
                overheatScale = 1.5f
                outputLiquid = LiquidStack(CioLiquids.cyberion, 0.55f)
                // TODO: use default drawer temporarily
                //drawer = CyberionMixerDrawer(S.Hologram, S.HologramDark)
                drawer = DrawMulti(DrawLiquidTile(CioLiquids.cyberion), DrawDefault())
                size = 3
            }
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioLiquids.cyberion",
        "CioUnitTypes.holoMiner",
        "CioUnitTypes.holoFighter",
        "CioUnitTypes.holoGuardian",
        "CioUnitTypes.holoArchitect",
        "CioUnitTypes.holoSupporter",
    )
    fun holoProjector() {
        holoProjector = HoloProjector("holo-projector").apply {
            VanillaSpec {
                requirements(
                    Category.units, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 16),
                        ItemStack(Items.silicon, 220),
                        ItemStack(Items.graphite, 300),
                        ItemStack(Items.metaglass, 500),
                        ItemStack(Items.thorium, 1200),
                    )
                )
                scaledHealth = 100f
                researchCostMultiplier = 0.8f
                plans = Seq.with(
                    HoloPlan(
                        CioUnitTypes.holoMiner,
                        Requirement(300f),
                        15f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoFighter,
                        Requirement(300f),
                        15f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoGuardian,
                        Requirement(155f),
                        7.5f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoArchitect,
                        Requirement(600f),
                        25f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoSupporter,
                        Requirement(250f),
                        12f * 60f
                    ),
                )
            }
            ErekirSpec {
                requirements(
                    Category.units, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 16),
                        ItemStack(Items.oxide, 220),
                        ItemStack(Items.thorium, 300),
                        ItemStack(Items.carbide, 150),
                        ItemStack(Items.silicon, 800),
                    )
                )
                scaledHealth = 150f
                researchCostMultiplier = 0.75f
                plans = Seq.with(
                    HoloPlan(
                        CioUnitTypes.holoMiner,
                        Requirement(300f),
                        18f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoFighter,
                        Requirement(300f),
                        15f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoGuardian,
                        Requirement(205f),
                        7.5f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoArchitect,
                        Requirement(600f),
                        25f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoSupporter,
                        Requirement(500f),
                        18f * 60f
                    ),
                )
            }
            size = 5
            buildCostMultiplier = 2f
        }
    }
    @DependOn("CioLiquids.cyberion")
    fun aquacyberion() {
        aquacyberion = Floor("aqua-cyberion").apply {
            drownTime = 0f
            status = StatusEffects.freezing
            statusDuration = 240f
            speedMultiplier = 0.1f
            variants = 0
            liquidDrop = CioLiquids.cyberion
            liquidMultiplier = 0.1f
            isLiquid = true
            cacheLayer = CioCLs.cyberion
            emitLight = true
            lightRadius = 30f
            lightColor = S.Hologram.cpy().a(0.19f)
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBulletTypes.ruvik2"
    )
    fun stealth() {
        stealth = Stealth("stealth").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.titanium, 150),
                        ItemStack(Items.plastanium, 30),
                    )
                )
                scaledHealth = 160f
                range = 260f
                liquidCapacity = 60f
                recoil = 3f
                reload = 15f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.tungsten, 150),
                        ItemStack(Items.surgeAlloy, 50),
                    )
                )
                scaledHealth = 140f
                range = 270f
                liquidCapacity = 50f
                recoil = 3.5f
                reload = 18f
            }
            val ruvikRange = range
            size = 3
            squareSprite = false

            shootType = RuvikBullet().apply {
                VanillaSpec {
                    speed = 2f
                    damage = 100f
                }
                ErekirSpec {
                    speed = 2.5f
                    damage = 120f
                }
                stemVersion = STEM_VERSION.STEM2
                width = 10f
                height = 10f
                hitSize = 10f
                lifetime = 240f
                maxRange = ruvikRange
                frontColor = S.Hologram
                backColor = S.HologramDark
            }
        }
    }
    @DependOn("CioItems.ic")
    fun wirelessTower() {
        wirelessTower = WirelessTower("wireless-tower").apply {
            VanillaSpec {
                requirements(
                    Category.power, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.copper, 310),
                        ItemStack(Items.lead, 20),
                        ItemStack(Items.silicon, 20),
                        ItemStack(Items.graphite, 30),
                    )
                )
                scaledHealth = 150f
                distributeSpeed = 10f
                range = 300f
            }
            ErekirSpec {
                requirements(
                    Category.power, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 10),
                        ItemStack(Items.tungsten, 150),
                        ItemStack(Items.graphite, 100),
                        ItemStack(Items.silicon, 80),
                        ItemStack(Items.carbide, 50),
                    )
                )
                scaledHealth = 100f
                distributeSpeed = 10f
                range = 320f
            }
            size = 2
        }
    }
    @DependOn("CioItems.ic")
    fun heimdall() {
        heimdall = Heimdall("heimdall").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 10),
                        ItemStack(Items.sporePod, 300),
                        ItemStack(Items.thorium, 150),
                        ItemStack(Items.metaglass, 50),
                        ItemStack(Items.copper, 120),
                        ItemStack(Items.silicon, 180),
                        ItemStack(Items.plastanium, 50),
                    )
                )
                scaledHealth = 400f
                range = 175f
                powerUse = 2.5f
                damage = 8f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 10),
                        ItemStack(Items.oxide, 300),
                        ItemStack(Items.carbide, 150),
                        ItemStack(Items.thorium, 150),
                        ItemStack(Items.graphite, 150),
                    )
                )
                scaledHealth = 350f
                range = 145f
                powerUse = 2.4f
                waveSpeed = 1.8f
                damage = 10f
                reloadTime = 75f
            }
            size = 4
            connectedSound = CioSounds.connected
            addFormationPatterns(
                FaceFE, FunnyFaceFE, ForceFieldFE
            )
        }
    }
    @DependOn("CioItems.ic")
    fun eye() {
        eye = Eye("heimdall-eye").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.pyratite, 15),
                        ItemStack(Items.plastanium, 10),
                        ItemStack(Items.metaglass, 40),
                        ItemStack(Items.copper, 60),
                        ItemStack(Items.silicon, 30),
                    )
                )
                range = 165f
                scaledHealth = 300f
                consumePower(3f)
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.surgeAlloy, 15),
                        ItemStack(Items.oxide, 10),
                        ItemStack(Items.tungsten, 40),
                        ItemStack(Items.silicon, 60),
                    )
                )
                range = 175f
                scaledHealth = 250f
                consumePower(2.4f)
            }
            shoot.firstShotDelay = 60f
            size = 2
            moveWhileCharging = false
            shootEffect = BrainFx.eyeShoot
            smokeEffect = Fx.none
            addUpgrade(
                Upgrade(UT.Damage, false, 0.05f),
                Upgrade(UT.ReloadTime, true, -4.5f),
                Upgrade(UT.ControlLine, true, 0.01f),
                Upgrade(UT.ForceFieldRegen, false, 0.3f),
                Upgrade(UT.Range, false, -0.05f),
                Upgrade(UT.ForceFieldRadius, true, -3f),
                Upgrade(UT.WaveWidth, true, -0.1f),
                Upgrade(UT.PowerUse, false, 0.55f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.2f),
            )
            normalSounds = CioSounds.laserWeak
            normalBullet = LightningBulletType().apply {
                VanillaSpec {
                    damage = 90f
                }
                ErekirSpec {
                    damage = 100f
                }
                lightningLength = 25
                collidesAir = false
                ammoMultiplier = 1f
                recoil = 3f
                shootCone = 3f
                accurateDelay = true
                lightningColor = R.C.RedAlert
            }
            improvedSounds = CioSounds.laser
            improvedBullet = LaserBulletType().apply {
                VanillaSpec {
                    damage = 250f
                }
                ErekirSpec {
                    damage = 290f
                }
                colors = arrayOf(R.C.RedAlert.cpy().a(0.4f), R.C.RedAlert, R.C.RedAlertDark)
                lightningColor = R.C.RedAlertDark
                chargeEffect = MultiEffect(BrainFx.eyeCharge, BrainFx.eyeChargeBegin)
                hitEffect = Fx.hitLancer
                hitSize = 4f
                lifetime = 16f
                recoil = 4f
                drawSize = 200f
                shootCone = 3f
                length = 173f
                accurateDelay = true
                ammoMultiplier = 1f
            }
        }
    }
    @DependOn("CioItems.ic")
    fun ear() {
        ear = Ear("heimdall-ear").apply {
            VanillaSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.sporePod, 20),
                        ItemStack(Items.graphite, 5),
                        ItemStack(Items.copper, 60),
                        ItemStack(Items.silicon, 50),
                        ItemStack(Items.plastanium, 10),
                    )
                )
                range = 145f
                scaledHealth = 300f
                damage = 8f
            }
            ErekirSpec {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.graphite, 20),
                        ItemStack(Items.beryllium, 60),
                        ItemStack(Items.silicon, 50),
                        ItemStack(Items.phaseFabric, 20),
                    )
                )
                range = 165f
                scaledHealth = 250f
                damage = 4f
                waveSpeed = 2.2f
            }
            addUpgrade(
                Upgrade(UT.Damage, false, -0.02f),
                Upgrade(UT.Range, false, 0.1f),
                Upgrade(UT.WaveSpeed, true, 0.08f),
                Upgrade(UT.WaveWidth, true, 0.4f),
                Upgrade(UT.ForceFieldRadius, true, 5f),
                Upgrade(UT.ForceFieldMax, false, 0.2f),
                Upgrade(UT.PowerUse, false, 0.35f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.15f),
            )
            buildType = Prov { EarBuild() }
            size = 2
        }
    }
    @DependOn("CioItems.ic")
    fun heart() {
        DebugOnly {
            heart = Heart("heimdall-heart").apply {
                VanillaSpec {
                    requirements(
                        Category.turret, BuildVisibility.shown, arrayOf(
                            ItemStack(CioItems.ic, 4),
                            ItemStack(Items.graphite, 200),
                            ItemStack(Items.metaglass, 500),
                            ItemStack(Items.silicon, 50),
                            ItemStack(Items.blastCompound, 200),
                        )
                    )
                    scaledHealth = 125f
                    convertSpeed = 6f
                }
                ErekirSpec {
                    requirements(
                        Category.turret, BuildVisibility.shown, arrayOf(
                            ItemStack(CioItems.ic, 4),
                            ItemStack(Items.oxide, 200),
                            ItemStack(Items.tungsten, 500),
                            ItemStack(Items.carbide, 50),
                            ItemStack(Items.silicon, 200),
                        )
                    )
                    convertSpeed = 7f
                    scaledHealth = 105f
                }
                size = 4
                blood = Blood()
                heartbeat.apply {
                    shake.config {
                        base = 1.5f
                        upRange = 4.8f - base
                        downRange = 0.9f
                    }
                    reloadTime.config {
                        // Decrease
                        base = 120f
                        upRange = 50f
                        downRange = 80f
                    }
                    powerUse.config {
                        base = 2f
                        upRange = 5f - base
                        downRange = 0f
                    }
                    damage.config {
                        base = 60f
                        upRange = 120f - base
                        downRange = 20f
                    }
                    range.config {
                        base = 165f
                        upRange = 240f - base
                        downRange = 0f
                    }
                    shootNumber.config {
                        base = 22
                        upRange = 34 - 22
                        downRange = 22 - 12
                    }
                    bloodCost.config {
                        base = 50f
                        upRange = 150f - 50f
                        downRange = 0f
                    }
                    systole.config {
                        base = 0.175f
                        upRange = 0.192f - 0.175f
                        downRange = 0.175f - 0.17f
                    }
                    diastole.config {
                        // Decrease
                        base = 3.3f
                        upRange = 3.5f - 3.3f
                        downRange = 3.3f - 3.15f
                    }
                    bulletLifeTime.config {
                        base = 200f
                        upRange = 300f - 200f
                        downRange = 50f
                    }
                    soundGetter = {
                        when (it) {
                            in Float.MIN_VALUE..0.1f -> CioSounds.heartbeat
                            in 0.1f..Float.MAX_VALUE -> CioSounds.heartbeatFaster
                            else -> CioSounds.heartbeat
                        }
                    }
                    offset = 20f // +5f when improved
                }
                bulletType = BBulletType("blood-bullet".Cio).apply {
                    damage = 0f
                    lifetime = 0f
                    hitEffect = Fx.none
                    shootEffect = Fx.none
                    smokeEffect = Fx.none
                    layer = Layer.bullet - 0.1f
                    despawnEffect = BrainFx.bloodBulletHit
                    hitEffect = BrainFx.bloodBulletHit
                    collidesTiles = false
                    filter = Texture.TextureFilter.nearest
                    scale = { 2.4f + it.damage / 80f }
                    hitSize = 20f
                }
            }
        }
    }

    fun decentralizer() {
        decentralizer = Decentralizer("decentralizer").apply {
            requirements(
                Category.crafting, BuildVisibility.shown, arrayOf()
            )
            size = 4
        }
    }
    @DependOn("CioItems.ic")
    fun DDos() {
        DebugOnly {
            DDoS = DDoS("DDoS").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf()
                )
                maxDamage = 120f
                size = 4
                hitSizer = { damage / 60f * 4f }
                bulletType = ItemBulletType().apply {
                    speed = 2f
                    damage = 0f
                    hitSize = 10f
                    drawSizer = { damage / 60f }
                    trailLength = 8
                    lifetime = 180f
                    trailWidth = 4f
                }
            }
        }
    }
    @DependOn("CioItems.ic")
    fun dataCDN() {
        DebugOnly {
            dataCDN = DataCDN("data-cdn").apply {
                requirements(
                    Category.units, BuildVisibility.shown, arrayOf()
                )
                size = 5
            }
        }
    }
    @DependOn("CioItems.ic")
    fun zipBomb() {
        DebugOnly {
            zipBomb = ZipBomb("zip-bomb").apply {
                requirements(
                    Category.effect, BuildVisibility.shown, arrayOf()
                )
                size = 2
                maxSensitive = 5
                buildType = Prov { ZipBombBuild() }
            }
        }
    }
    @DependOn("CioItems.ic")
    fun serializer() {
        DebugOnly {
            serializer = Serializer("serializer").apply {
                requirements(
                    Category.units, BuildVisibility.shown, arrayOf()
                )
                size = 5
                buildType = Prov { SerializerBuild() }
            }
        }
    }
    @DependOn
    fun _overwriteVanilla() {
        DebugOnly {
            Blocks.barrierProjector = DirectionalForceProjector("barrier-projector").apply {
                requirements(Category.effect, ItemStack.with(Items.surgeAlloy, 100, Items.silicon, 125))
                size = 3
                width = 50f
                length = 36f
                shieldHealth = 2000f
                cooldownNormal = 3f
                cooldownBrokenBase = 0.35f
                consumePower(4f)
            }
            Blocks.shieldProjector = BaseShield("shield-projector").apply {
                requirements(Category.effect, BuildVisibility.shown, arrayOf())
                size = 3
                consumePower(5f)
            }
            (Blocks.powerSource as PowerSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            Blocks.itemSource.buildVisibility = BuildVisibility.shown
            Blocks.liquidSource.buildVisibility = BuildVisibility.shown
            Blocks.payloadSource.buildVisibility = BuildVisibility.shown

            Blocks.powerVoid.buildVisibility = BuildVisibility.shown
            Blocks.itemVoid.buildVisibility = BuildVisibility.shown
            Blocks.liquidVoid.buildVisibility = BuildVisibility.shown
            Blocks.payloadVoid.buildVisibility = BuildVisibility.shown
            val blockSize = 10f
            (Blocks.payloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.payloadLoader as PayloadLoader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadRouter as PayloadRouter).payloadLimit = blockSize
            (Blocks.payloadUnloader as PayloadUnloader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadPropulsionTower as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.payloadMassDriver as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.reinforcedPayloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.reinforcedPayloadRouter as PayloadConveyor).payloadLimit = blockSize
            UnitTypes.evoke.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.incite.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.emanate.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            /*val coreBlock = Blocks.coreShard as CoreBlock
            coreBlock.unitType = CioUnitTypes.holoFighter
            coreBlock.solid = false*/
            Liquids.neoplasm.hidden = false
        }
        ExperimentalOnly {
            Blocks.conveyor.sync = true
            Blocks.titaniumConveyor.sync = true
            Blocks.armoredConveyor.sync = true
            Blocks.plastaniumConveyor.sync = true
        }
    }
}