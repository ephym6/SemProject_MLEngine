package io.github.ephym6.plantid.ui.care

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.ephym6.plantid.PlantIdApplication
import io.github.ephym6.plantid.R
import io.github.ephym6.plantid.domain.plant.CareData
import io.github.ephym6.plantid.domain.plant.CareRecommendations
import io.github.ephym6.plantid.domain.plant.ContentSource
import io.github.ephym6.plantid.domain.plant.Plant
import io.github.ephym6.plantid.ui.about.AboutScreen
import io.github.ephym6.plantid.ui.care.CareGuideViewModel.UiState
import io.github.ephym6.plantid.ui.theme.PlantIDTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Spec §6.8 acceptance for the care guide, plus the About plant list and credits. */
@RunWith(AndroidJUnit4::class)
class CareGuideScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val source = ContentSource("Test source", "https://example.org/data", "CC BY-NC 4.0")
    private var retries = 0

    private fun str(@StringRes id: Int): String = composeRule.activity.getString(id)

    private fun show(state: UiState) {
        composeRule.setContent {
            PlantIDTheme { CareGuideScreen(state = state, onBack = {}, onRetry = { retries++ }) }
        }
    }

    private fun plant(care: CareData?) =
        Plant("p", "P", "Test Plant", "Test species (fictional)", "Testaceae", care)

    @Test
    fun completeCareGuideShowsEveryTipTheDisclaimerAndTheSource() {
        val care = CareData(lightLux = 500..3000, temperatureCelsius = 15..30, humidityPercent = 50, wateringIntervalDays = 7)
        show(UiState.Ready(plant(care), CareRecommendations.tipsFor(care), source))

        composeRule.onNodeWithText("Test Plant").assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.care_general_title)).performScrollTo().assertIsDisplayed()
        listOf(
            R.string.care_light_title,
            R.string.care_watering_title,
            R.string.care_temperature_title,
            R.string.care_humidity_title,
        ).forEach { composeRule.onNodeWithText(str(it)).performScrollTo().assertIsDisplayed() }
        composeRule.onNodeWithText(
            composeRule.activity.resources.getQuantityString(R.plurals.care_watering_interval, 7, 7),
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.care_temperature_range, 15, 30))
            .performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.care_source_with_license, source.name, source.license),
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun missingCareValuesAreLeftOutWithoutBreakingTheScreen() {
        val care = CareData(wateringIntervalDays = 14)
        show(UiState.Ready(plant(care), CareRecommendations.tipsFor(care), source))

        composeRule.onNodeWithText(str(R.string.care_watering_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.care_light_title)).assertDoesNotExist()
        composeRule.onNodeWithText(str(R.string.care_temperature_title)).assertDoesNotExist()
        composeRule.onNodeWithText(str(R.string.care_humidity_title)).assertDoesNotExist()
    }

    @Test
    fun noCareRecordShowsAnHonestEmptyState() {
        show(UiState.NoCareGuide)

        composeRule.onNodeWithText(str(R.string.care_none_title)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.care_watering_title)).assertDoesNotExist()
    }

    @Test
    fun unreadableDataCanBeRetried() {
        show(UiState.Unavailable)

        composeRule.onNodeWithText(str(R.string.action_try_again)).performClick()
        assertEquals(1, retries)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aboutListsTheSupportedPlantsAndCreditsTheSource() {
        val repository = (composeRule.activity.application as PlantIdApplication).container.plantRepository
        val catalog = runBlocking { repository.catalog() }
        composeRule.setContent { PlantIDTheme { AboutScreen(versionName = "test", catalog = catalog) } }

        val plants = catalog.getOrThrow().plants
        composeRule.onNodeWithText(
            composeRule.activity.resources.getQuantityString(R.plurals.about_supported_intro, plants.size, plants.size),
        ).performScrollTo().assertIsDisplayed()
        plants.forEach { plant ->
            composeRule.waitUntilExactlyOneExists(hasText("${plant.commonName} (${plant.scientificName})", substring = true))
        }
        composeRule.onNodeWithText(str(R.string.about_sources_title)).performScrollTo().assertIsDisplayed()
    }
}
