package com.nestorian87.eter.ui.screens.search

import androidx.annotation.StringRes
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostSearchSortBy

enum class SearchSortOption(
    @param:StringRes val labelRes: Int,
    val apiValue: PostSearchSortBy,
) {
    POPULAR(R.string.feed_sort_popular, PostSearchSortBy.POPULAR),
    NEWEST(R.string.feed_sort_newest, PostSearchSortBy.NEWEST),
    OLDEST(R.string.feed_sort_oldest, PostSearchSortBy.OLDEST),
}
