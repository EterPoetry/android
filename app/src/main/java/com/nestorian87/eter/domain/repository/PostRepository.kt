package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.MyPostsQuery
import com.nestorian87.eter.domain.model.ListenEndResult
import com.nestorian87.eter.domain.model.PostFeedPage
import com.nestorian87.eter.domain.model.PostListPage
import com.nestorian87.eter.domain.model.ListenProgressResult
import com.nestorian87.eter.domain.model.ListenStartResult
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostComment
import com.nestorian87.eter.domain.model.PostCommentsPage
import com.nestorian87.eter.domain.model.PostCommentsQuery
import com.nestorian87.eter.domain.model.PostSearchQuery
import com.nestorian87.eter.domain.model.PopularPostsPage
import com.nestorian87.eter.domain.model.PublicConfig
import com.nestorian87.eter.domain.model.UpdatePostPayload
import java.io.File

interface PostRepository {
    suspend fun getPublicConfig(): PublicConfig

    suspend fun getCategories(search: String? = null): List<PostCategory>

    suspend fun createPost(audioFile: File): Post

    suspend fun getPost(postId: Long): Post

    suspend fun getPopularPosts(
        snapshotId: String? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): PopularPostsPage

    suspend fun searchPosts(query: PostSearchQuery = PostSearchQuery()): PostListPage

    suspend fun getSubscriptionFeed(
        cursor: String? = null,
        limit: Int? = null,
    ): PostFeedPage

    suspend fun getLikedPosts(
        offset: Int? = null,
        limit: Int? = null,
    ): PostListPage

    suspend fun updatePost(postId: Long, payload: UpdatePostPayload): Post

    suspend fun replacePostAudio(postId: Long, audioFile: File): Post

    suspend fun getMyPosts(query: MyPostsQuery = MyPostsQuery()): MyPostsPage

    suspend fun startListen(
        postId: Long,
        sessionId: String,
    ): ListenStartResult

    suspend fun updateListenProgress(
        postId: Long,
        token: String,
        positionMs: Int,
    ): ListenProgressResult

    suspend fun endListen(
        postId: Long,
        token: String,
        positionMs: Int,
        sessionId: String? = null,
    ): ListenEndResult

    suspend fun getPostComments(
        postId: Long,
        query: PostCommentsQuery = PostCommentsQuery(),
    ): PostCommentsPage

    suspend fun getCommentReplies(
        commentId: Long,
        limit: Int? = null,
        cursor: String? = null,
    ): PostCommentsPage

    suspend fun createComment(
        postId: Long,
        commentText: String,
        replyToCommentId: Long? = null,
    ): PostComment

    suspend fun likePost(postId: Long): Int

    suspend fun unlikePost(postId: Long): Int

    suspend fun likeComment(commentId: Long): Int

    suspend fun unlikeComment(commentId: Long): Int

    suspend fun deleteComment(commentId: Long)

    suspend fun deletePost(postId: Long)
}
