package com.yadar.app

import android.app.Application
import com.yadar.app.di.AppContainer

class YadarApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.getInstance(this)
    }
}
