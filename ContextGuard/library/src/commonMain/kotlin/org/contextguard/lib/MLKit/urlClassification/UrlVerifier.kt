package org.contextguard.lib.MLKit.urlClassification

import dev.kursor.ktensorflow.api.Interpreter
import dev.kursor.ktensorflow.api.InterpreterOptions
import dev.kursor.ktensorflow.api.ModelDesc
import dev.kursor.ktensorflow.api.Tensor
import dev.kursor.ktensorflow.api.TensorDataType
import dev.kursor.ktensorflow.api.TensorShape
import dev.kursor.ktensorflow.api.typedData
import kotlin.math.exp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.contextguard.lib.MLKit.urlClassification.constants.Constants

class UrlVerifier {

    // One interpreter is created on first use and reused for every prediction. Creating one per
    // prediction leaked native memory on iOS, where Interpreter.close() is a no-op in
    // ktensorflow 0.2. The mutex serialises access, since an interpreter must not run
    // concurrently (the reason the previous code created a fresh one each time).
    private val mutex = Mutex()
    private var interpreter: Interpreter? = null

    suspend fun makePrediction(
        modelDesc: ModelDesc,
        data: ByteArray
    ): Float = mutex.withLock {
        val interpreter = interpreter ?: Interpreter(
            modelDesc = modelDesc,
            options = InterpreterOptions(
                numThreads = 4,
                useXNNPACK = true
            )
        ).also { interpreter = it }

        val input = Tensor(
            data = data,
            shape = TensorShape(Constants.MODEL_INPUT_SIZE),
            dataType = TensorDataType.Float32
        )

        val output = Tensor(
            shape = TensorShape(1),
            dataType = TensorDataType.Float32
        )
        interpreter.run(listOf(input), listOf(output))
        sigmoid(output.typedData<FloatArray>().first()) * 100
    }

    companion object {
        private var instance: UrlVerifier? = null

        fun getInstance(): UrlVerifier {
            if (instance == null) {
                instance = UrlVerifier()
            }
            return instance!!
        }
    }
}

private fun sigmoid(x: Float): Float = (1 / (1 + exp(-x)))