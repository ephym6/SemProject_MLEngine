package io.github.ephym6.plantid.di

import android.content.Context
import io.github.ephym6.plantid.data.history.HistoryPhotoStore
import io.github.ephym6.plantid.data.history.PlantIdDatabase
import io.github.ephym6.plantid.data.history.RoomHistoryRepository
import io.github.ephym6.plantid.data.identification.mock.DemoScenarioController
import io.github.ephym6.plantid.data.identification.mock.MockIdentificationService
import io.github.ephym6.plantid.data.image.CachePhotoStore
import io.github.ephym6.plantid.data.image.PhotoStore
import io.github.ephym6.plantid.data.plant.AssetPlantRepository
import io.github.ephym6.plantid.domain.history.HistoryRepository
import io.github.ephym6.plantid.domain.identification.IdentificationService
import io.github.ephym6.plantid.domain.plant.PlantRepository

/**
 * Manual dependency container (DECISIONS D5): one instance per app process,
 * owned by [io.github.ephym6.plantid.PlantIdApplication]. Add app-wide
 * dependencies here as lazy properties.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val photoStore: PhotoStore by lazy { CachePhotoStore(appContext) }

    val plantRepository: PlantRepository by lazy { AssetPlantRepository(appContext) }

    private val database by lazy { PlantIdDatabase.create(appContext) }

    val historyRepository: HistoryRepository by lazy {
        RoomHistoryRepository(database.historyDao(), HistoryPhotoStore(appContext))
    }

    // Until Sprint 7 the only identifier is the demo mock.
    private val mockIdentification by lazy { MockIdentificationService(plantRepository) }

    val identificationService: IdentificationService get() = mockIdentification

    /** Non-null only while the demo identifier is in use; drives the "Demo mode" picker. */
    val demoScenarios: DemoScenarioController? get() = mockIdentification
}
