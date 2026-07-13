package org.contextguard.lib.MLKit.urlClassification

import dev.kursor.ktensorflow.api.Interpreter
import dev.kursor.ktensorflow.api.InterpreterOptions
import dev.kursor.ktensorflow.api.ModelDesc
import dev.kursor.ktensorflow.api.Tensor
import dev.kursor.ktensorflow.api.TensorDataType
import dev.kursor.ktensorflow.api.TensorShape
import dev.kursor.ktensorflow.api.typedData
import kotlin.math.exp
import org.contextguard.lib.MLKit.urlClassification.constants.Constants

class UrlVerifier {
    private var cachedInterpreter: Interpreter? = null
    private var cachedModelDesc: ModelDesc? = null

    private fun getOrCreateInterpreter(modelDesc: ModelDesc): Interpreter {
        if (cachedInterpreter != null && cachedModelDesc == modelDesc) {
            return cachedInterpreter!!
        }
        cachedInterpreter = Interpreter(
            modelDesc = modelDesc,
            options = InterpreterOptions(
                numThreads = 4,
                useXNNPACK = true
            )
        )
        cachedModelDesc = modelDesc
        return cachedInterpreter!!
    }

    fun makePrediction(
        modelDesc: ModelDesc,
        data: ByteArray
    ): Float {
        val interpreter = getOrCreateInterpreter(modelDesc)

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
        return sigmoid(output.typedData<FloatArray>().first()) * 100
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