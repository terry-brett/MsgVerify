package org.contextguard.lib

import dev.kursor.ktensorflow.api.ModelDesc

/**
 * Expect declaration for loading a model from assets.
 * The constructor takes a platform-specific context (e.g., Android's Context)
 * which will be cast to the appropriate type in the actual implementation.
 */
expect class LoadModel {
    /**
     * Suspended function to get the model path from assets.
     * @return A Model description representing the loaded model's information
     */
    suspend fun getModelDesc(): ModelDesc
}
