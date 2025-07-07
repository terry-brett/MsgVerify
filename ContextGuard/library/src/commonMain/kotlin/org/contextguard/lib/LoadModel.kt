package org.contextguard.lib

expect class LoadModel {
    suspend fun loadModelFromAssets(): String
}
