package com.nestorian87.eter.service

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.nestorian87.eter.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaSessionService() {
    @Inject
    lateinit var postPlayerManager: PostPlayerManager

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        addSession(postPlayerManager.session)
        setMediaNotificationProvider(EterMediaNotificationProvider(this))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = postPlayerManager.session

    override fun onDestroy() {
        removeSession(postPlayerManager.session)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!postPlayerManager.isPlaybackOngoing) {
            stopSelf()
        }
    }
}

@UnstableApi
private class EterMediaNotificationProvider(
    service: PlayerService,
) : DefaultMediaNotificationProvider(service) {
    private val context = service.applicationContext

    init {
        setSmallIcon(R.drawable.ic_notification_small)
    }

    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: Player.Commands,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        showPauseButton: Boolean,
    ): ImmutableList<CommandButton> {
        val buttons = mutableListOf<CommandButton>()
        if (playerCommands.contains(Player.COMMAND_SEEK_BACK)) {
            buttons += notificationButton(
                icon = CommandButton.ICON_SKIP_BACK_10,
                playerCommand = Player.COMMAND_SEEK_BACK,
                compactViewIndex = 0,
                displayName = context.getString(R.string.player_seek_back),
                slot = CommandButton.SLOT_BACK,
            )
        }
        if (playerCommands.contains(Player.COMMAND_PLAY_PAUSE)) {
            buttons += notificationButton(
                icon = if (showPauseButton) CommandButton.ICON_PAUSE else CommandButton.ICON_PLAY,
                playerCommand = Player.COMMAND_PLAY_PAUSE,
                compactViewIndex = 1,
                displayName = context.getString(
                    if (showPauseButton) R.string.player_pause else R.string.player_resume,
                ),
                slot = CommandButton.SLOT_CENTRAL,
            )
        }
        if (playerCommands.contains(Player.COMMAND_SEEK_FORWARD)) {
            buttons += notificationButton(
                icon = CommandButton.ICON_SKIP_FORWARD_10,
                playerCommand = Player.COMMAND_SEEK_FORWARD,
                compactViewIndex = 2,
                displayName = context.getString(R.string.player_seek_forward),
                slot = CommandButton.SLOT_FORWARD,
            )
        }
        return ImmutableList.copyOf(buttons)
    }

    private fun notificationButton(
        icon: Int,
        playerCommand: Int,
        compactViewIndex: Int,
        displayName: String,
        slot: Int,
    ): CommandButton = CommandButton.Builder(icon)
        .setPlayerCommand(playerCommand)
        .setDisplayName(displayName)
        .setSlots(slot)
        .setExtras(
            Bundle().apply {
                putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, compactViewIndex)
            },
        )
        .build()
}
