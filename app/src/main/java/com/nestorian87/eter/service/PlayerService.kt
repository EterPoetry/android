package com.nestorian87.eter.service

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlayerService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = null

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

}
