package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.MyPostsQuery
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PublicConfig
import com.nestorian87.eter.domain.model.UpdatePostPayload
import java.io.File

interface PostRepository {
    suspend fun getPublicConfig(): PublicConfig

    suspend fun getCategories(search: String? = null): List<PostCategory>

    suspend fun createPost(audioFile: File): Post

    suspend fun getPost(postId: Long): Post

    suspend fun updatePost(postId: Long, payload: UpdatePostPayload): Post

    suspend fun replacePostAudio(postId: Long, audioFile: File): Post

    suspend fun getMyPosts(query: MyPostsQuery = MyPostsQuery()): MyPostsPage

    suspend fun deletePost(postId: Long)
}
