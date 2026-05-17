package com.nestorian87.eter.ui.navigation

import android.content.Intent
import android.net.Uri

object EterDeepLink {
    private const val SCHEME = "eter"
    private const val HOST_POST = "post"
    private const val QUERY_FOCUS_COMMENTS = "focusComments"

    fun postUri(postId: Long, focusComments: Boolean = false): Uri = Uri.Builder()
        .scheme(SCHEME)
        .authority(HOST_POST)
        .appendPath(postId.toString())
        .appendQueryParameter(QUERY_FOCUS_COMMENTS, focusComments.toString())
        .build()

    fun parsePostRequest(intent: Intent?): PostRequest? {
        val data = intent?.data ?: return null
        if (data.scheme != SCHEME || data.authority != HOST_POST) {
            return null
        }
        val postId = data.pathSegments.firstOrNull()?.toLongOrNull() ?: return null
        val focusComments = data.getBooleanQueryParameter(QUERY_FOCUS_COMMENTS, false)
        return PostRequest(
            postId = postId,
            focusComments = focusComments,
        )
    }

    data class PostRequest(
        val postId: Long,
        val focusComments: Boolean,
    )
}
