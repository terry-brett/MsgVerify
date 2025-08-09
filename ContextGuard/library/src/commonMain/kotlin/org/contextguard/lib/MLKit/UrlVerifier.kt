package org.contextguard.lib.MLKit

import dev.kursor.ktensorflow.api.Interpreter
import dev.kursor.ktensorflow.api.InterpreterOptions
import dev.kursor.ktensorflow.api.ModelDesc
import dev.kursor.ktensorflow.api.Tensor
import dev.kursor.ktensorflow.api.TensorDataType
import dev.kursor.ktensorflow.api.TensorShape
import dev.kursor.ktensorflow.api.gpu.GpuDelegate
import dev.kursor.ktensorflow.api.typedData
import kotlin.math.exp

class UrlVerifier {
    fun makePrediction(
        modelDesc: ModelDesc,
        data: ByteArray
    ): Float {


        val interpreter = Interpreter(
            modelDesc = modelDesc,
            options = InterpreterOptions(
                numThreads = 4,
                useXNNPACK = true,
                delegates = listOf(GpuDelegate())
            )
        )

        val input = Tensor(
            data = data,
            shape = TensorShape(25),
            dataType = TensorDataType.Float32
        )

        val output = Tensor(
            shape = TensorShape(1),
            dataType = TensorDataType.Float32
        )
        interpreter.run(listOf(input), listOf(output))
        return sigmoid(output.typedData<FloatArray>().first()) * 100
    }
}

private fun sigmoid(x: Float): Float = (1 / (1 + exp(-x)))