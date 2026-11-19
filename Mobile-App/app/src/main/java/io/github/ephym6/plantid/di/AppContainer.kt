package io.github.ephym6.plantid.di

import android.content.Context
import io.github.ephym6.plantid.data.image.CachePhotoStore
import io.github.ephym6.plantid.data.image.PhotoStore

/**
 * Manual dependency container (DECISIONS D5): one instance per app process,
 * owned by [io.github.ephym6.plantid.PlantIdApplication]. Add app-wide
 * dependencies here as lazy properties.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val photoStore: PhotoStore by lazy { CachePhotoStore(appContext) }
}
