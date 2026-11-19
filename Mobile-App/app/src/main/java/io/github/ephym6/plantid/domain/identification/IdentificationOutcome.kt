package io.github.ephym6.plantid.domain.identification

/**
 * Product-level result of one identification attempt (spec §6.5 statuses).
 *
 * How raw model output maps to these statuses (thresholds, uncertainty policy)
 * is an AI-integration decision (DECISIONS Q2). The UI must not invent one.
 */
sealed interface IdentificationOutcome {
    val model: ModelInfo

    /** A supported class passed the agreed decision rule. */
    data class Identified(
        override val model: ModelInfo,
        /** Classifier label, e.g. a training class folder name. */
        val classId: String,
        val plant: PlantSummary,
        /** Raw scores if the model provides them. Not shown in the UI until Q2 is decided. */
        val scores: List<ClassScore> = emptyList(),
    ) : IdentificationOutcome

    /**
     * The model returned something, but the decision rule says it is not
     * confident. Must never be displayed as a species identification.
     */
    data class Uncertain(
        override val model: ModelInfo,
        val scores: List<ClassScore> = emptyList(),
    ) : IdentificationOutcome

    /** No acceptable supported class: an unsupported plant, or not a plant at all. */
    data class Unsupported(
        override val model: ModelInfo,
    ) : IdentificationOutcome

    /** The photo couldn't be processed or inference failed. */
    data class Failed(
        override val model: ModelInfo,
        val reason: FailureReason,
    ) : IdentificationOutcome
}

/** A raw model score for one class. Not necessarily a calibrated probability. */
data class ClassScore(val classId: String, val score: Float)

enum class FailureReason {
    /** The stored photo is missing or couldn't be decoded. */
    PhotoUnavailable,

    /** The identifier couldn't be loaded. */
    ModelUnavailable,

    /** Preprocessing or inference failed. */
    ProcessingFailed,

    /** No result within the app's time limit. */
    TimedOut,
}

/**
 * The plant information shown on a result. Separate from care guidance, which
 * maps a plant record to curated content (spec §7.3, Sprint 4).
 * Optional fields may be missing and the UI must cope.
 */
data class PlantSummary(
    val plantId: String,
    val commonName: String?,
    val scientificName: String?,
    val description: String?,
)
