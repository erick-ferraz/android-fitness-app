package com.erickferraz.appfitness.model

import android.app.Application
import com.erickferraz.appfitness.model.AppDatabase

class App : Application() {

    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
    }
}