/*
 * Copyright (C) 2020 Hamidreza Etebarian & Ali Modares.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package meow

import android.app.Application
import android.content.res.ColorStateList
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import meow.core.api.MeowSession
import meow.core.data.MeowSharedPreferences
import meow.core.ui.MeowActivity
import meow.ktx.MeowContextWrapper
import meow.ktx.getField
import meow.ktx.setField

/**
 * 🐈 This CAT can control configurations and UI properties in an Application. Just trust it.
 *
 * @author  Hamidreza Etebarian
 * @version 1.0.0
 * @since   2020-03-01
 */

lateinit var controller: MeowController

fun MeowApp.bindMeow(controllerBlock: (controller: MeowController) -> Unit) =
    MeowController().apply {
        controllerBlock(this)
    }.bindApp(this)

class MeowController(
    val meowSession: MeowSession = MeowSession()
) {
    lateinit var app: Application

    var isDebugMode: Boolean = true
    var isLogTagNative: Boolean = true
    var apiSuccessRange: IntRange = 200..200
    var onException: (exception: Exception) -> Unit = {}

    var dpi: Float = 1f
    var language: String = ""
    var currency: Currency = Currency.USD
    var calendar: Calendar = Calendar.GEORGIAN
    var rootFolderName: String = "meow_app"

    var onColorGet: (color: Int) -> Int = { color -> color }
    internal var onColorStateListGet: (colorStateList: ColorStateList) -> ColorStateList =
        { color ->
            color.apply {
                val colors = getField<ColorStateList, IntArray>("mColors")!!
                colors.forEachIndexed { index, it ->
                    colors[index] = onColorGet(it)
                }
                setField("mDefaultColor", onColorGet(defaultColor))
                setField("mColors", colors)
            }
            ColorStateList.valueOf(onColorGet(color.defaultColor))
        }

    var defaultTypefaceResId: Int = 0
    var toastTypefaceResId: Int = 0

    var theme = Theme.DAY
    val isNightMode
        get() = theme == Theme.NIGHT

    val isRtl
        get() = language == "fa" || language == "ar"
    val isPersian
        get() = language == "fa"

    fun swapTheme(
        activity: MeowActivity<*>,
        updateConfig: Boolean = true
    ) = updateTheme(activity, if (isNightMode) Theme.DAY else Theme.NIGHT, updateConfig)

    fun updateLanguage(activity: MeowActivity<*>, language: String) {
        this.language = language
        activity.setLanguage(language)
    }

    fun updateTheme(
        activity: MeowActivity<*>,
        newTheme: Theme? = null,
        updateConfig: Boolean = true
    ) {
        if (theme == newTheme)
            return

        theme = newTheme ?: controller.theme

        val nightMode = when (theme) {
            Theme.DAY -> AppCompatDelegate.MODE_NIGHT_NO
            Theme.NIGHT -> AppCompatDelegate.MODE_NIGHT_YES
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)

        if (updateConfig) {
            val uiMode = when (theme) {
                Theme.DAY -> Configuration.UI_MODE_NIGHT_NO
                Theme.NIGHT -> Configuration.UI_MODE_NIGHT_YES
            }
            activity.applicationContext.resources.configuration.uiMode = uiMode
            activity.recreate()
        }
    }

    fun bindApp(app: MeowApp) {
        controller = this
        controller.app = app
        language = app.getLanguage(app)
        theme = app.getTheme(app)
        dpi = app.resources.displayMetrics.density

        MeowContextWrapper.wrap(app, "en")
        MeowSharedPreferences(app, "meow_shared").put("test_key", "test_value")
        MeowContextWrapper.wrap(app, controller.language)
    }

    enum class Theme {
        DAY, NIGHT
    }

    enum class Calendar {
        GEORGIAN, JALALI
    }

    enum class Currency(val default: String) {
        USD("$0.00"),
        RIAL("0"),
        TOMAN("0")
    }

}