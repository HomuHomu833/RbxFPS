package com.homuhomu833.rbxfps

import android.app.Application
import com.google.android.material.color.DynamicColors

class RbxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
