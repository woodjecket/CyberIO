package net.liplum.welcome

import arc.Core
import arc.scene.ui.Label
import mindustry.Vars
import mindustry.ui.BorderImage
import mindustry.ui.dialogs.BaseDialog
import net.liplum.*
import net.liplum.common.util.ReferBundleWrapper
import net.liplum.common.Res
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.DesktopOnly
import net.liplum.mdt.Else
import net.liplum.common.util.loadDynamicAsync

@ClientOnly
object FirstLoaded {
    var bundle = ReferBundleWrapper.create()
    @JvmStatic
    fun load() {
        loadBundle()
    }
    @JvmStatic
    fun loadBundle() {
        bundle.loadMoreFrom("installations")
        if (Core.settings.getString("local") != "en") {
            bundle.linkParent("installations")
        }
    }
    /**
     * Record the time of firstly installing Cyber IO.
     */
    @JvmStatic
    fun tryRecord() {
        if (Settings.FirstInstallationTime < 0) {
            Settings.FirstInstallationTime = System.currentTimeMillis()
        }
    }
    @JvmStatic
    val playerName: String
        get() {
            if (Vars.steam)
                return Vars.steamPlayerName
            val onlineName = Core.settings.getString("name", "")
            if (onlineName.isNotEmpty())
                return onlineName
            return bundle["default-name"]
        }
    @JvmStatic
    fun showDialog() {
        BaseDialog(bundle["subject"]).apply {
            val modIconImg = BorderImage()
            Core.app.post {
                Res("icon.png").tryReadAsStream()?.let {
                    loadDynamicAsync(it) { icon ->
                        modIconImg.setDrawable(icon)
                    }
                }
            }
            cont.add(modIconImg).row()
            cont.table {
                it.left()
                val indent = if (Vars.mobile) 5f else 40f
                fun addMailText(text: String, indentTimes: Int = 0) =
                    it.add(Label(text).apply {
                        setWrap(true)
                        DesktopOnly {
                            setFontScale(1.1f)
                        }
                    }).apply {
                        growX()
                        padLeft(if (Vars.mobile) 50f else 150f + indent * indentTimes).row()
                        row()
                    }
                addMailText(bundle["from"])
                addMailText(bundle.format("to", playerName))
                addMailText(bundle.format("welcome", Meta.DetailedVersion))
                addMailText(bundle["content"], indentTimes = 1)
                addMailText(bundle["tip"])
                addMailText(bundle["address"])
            }.apply {
                DesktopOnly {
                    width(Core.scene.width / 5f * 4f)
                }.Else {
                    width(Core.scene.width / 5f * 2.5f)
                }
                fillX()
                padBottom(4f)
                row()
            }
            cont.button(bundle["read"]) {
                hide()
            }.size(200f, 50f)
        }.show()
    }
}