package com.iancovski.hush

import android.app.Application

class HushApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
