package dev.curated.app.presentation.curated

import org.junit.Assert.assertEquals
import org.junit.Test

class CuratedMoviesLoadMoreTriggerTest {
    @Test
    fun gridRequestsNextPageNearTheEndOnlyWhenMoreMoviesExist() {
        assertEquals(
            false,
            curatedMoviesShouldRequestNextPage(
                lastVisibleItemIndex = 2,
                totalItemCount = 20,
                canLoadMore = true,
            ),
        )
        assertEquals(
            true,
            curatedMoviesShouldRequestNextPage(
                lastVisibleItemIndex = 15,
                totalItemCount = 20,
                canLoadMore = true,
            ),
        )
        assertEquals(
            false,
            curatedMoviesShouldRequestNextPage(
                lastVisibleItemIndex = 15,
                totalItemCount = 20,
                canLoadMore = false,
            ),
        )
    }
}
