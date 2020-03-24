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

package sample

import meow.MeowApp
import meow.MeowController
import meow.meowModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.BuildConfig
import org.kodein.di.android.x.androidXModule
import org.kodein.di.direct
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.singleton
import sample.data.DataSource
import sample.di.apiModule
import sample.di.appModule
import sample.di.mvvmModule

/**
 * Sample Application class.
 *
 * @author  Hamidreza Etebarian
 * @version 1.0.0
 * @since   2020-03-01
 */

class App : MeowApp(), KodeinAware {

    val dataSource: DataSource by instance()
    override var controller = MeowController(this).apply {
        onColorGet = {
            when (it) {
//                getColorCompat(R.color.primary, false) -> Color.RED
//                getColorCompat(R.color.primary_dark, false) -> Color.BLACK
                else -> it
            }
        }
        language = "fa"
        isDebugMode = isDebug
        isLogTagNative = false
        defaultTypefaceResId = R.font.iransans_mobile_regular
        toastTypefaceResId = R.font.iransans_mobile_regular
        isPersian = true
    }

    override val kodein = Kodein.lazy {
        bind() from singleton { kodein.direct }
        bind() from singleton { this@App }
        bind() from singleton { this@App.controller }
        import(androidXModule(this@App))
        import(meowModule)
        import(appModule)
        import(apiModule)
        import(mvvmModule)
    }

    private val isDebug = BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()
        controller.bindApp(this)
    }

}
