@file:Suppress("unused")

package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.FollowProfilesPage
import com.nestorian87.eter.domain.model.FollowProfilesQuery
import com.nestorian87.eter.domain.model.PublicProfile

interface ProfileRepository {
    suspend fun getPublicProfile(userId: Long): PublicProfile

    suspend fun followProfile(userId: Long): PublicProfile

    suspend fun unfollowProfile(userId: Long): PublicProfile

    suspend fun getMyFollowers(query: FollowProfilesQuery = FollowProfilesQuery()): FollowProfilesPage

    suspend fun getMyFollowing(query: FollowProfilesQuery = FollowProfilesQuery()): FollowProfilesPage
}
