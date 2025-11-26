package org.contextguard.lib.MLKit.messageClassification

import dev.kursor.ktensorflow.api.Interpreter
import dev.kursor.ktensorflow.api.InterpreterOptions
import dev.kursor.ktensorflow.api.ModelDesc
import dev.kursor.ktensorflow.api.Tensor
import dev.kursor.ktensorflow.api.TensorDataType
import dev.kursor.ktensorflow.api.TensorShape

class MessageVerifier {

    fun makePrediction(
        modelDesc: ModelDesc,
        inputIds: IntArray,
        attentionMask: IntArray
    ): ByteArray {

        val interpreter = Interpreter(
            modelDesc = modelDesc,
            options = InterpreterOptions(
                numThreads = 4,
                useXNNPACK = true
            )
        )

        val inputIdsTensor = Tensor(
            data = intArrayToByteArrayNativeOrder(inputIds),
            shape = TensorShape(1, MAX_LEN),
            dataType = TensorDataType.Int32
        )

        val attentionMaskTensor = Tensor(
            data = intArrayToByteArrayNativeOrder(attentionMask),
            shape = TensorShape(1, MAX_LEN),
            dataType = TensorDataType.Int32
        )

        val outputTensor = Tensor(
            data = ByteArray(3 * 4),
            shape = TensorShape(1, 3),
            dataType = TensorDataType.Float32
        )

        interpreter.run(
            inputs = listOf(
                inputIdsTensor,
                attentionMaskTensor
            ),
            outputs = listOf(outputTensor)
        )

        return outputTensor.data
    }
}

private const val MAX_LEN = 128

private fun intArrayToByteArrayNativeOrder(ints: IntArray): ByteArray {
    val byteArray = ByteArray(ints.size * 4)
    for (i in ints.indices) {
        val value = ints[i]
        byteArray[i * 4] = (value and 0xFF).toByte()
        byteArray[i * 4 + 1] = ((value shr 8) and 0xFF).toByte()
        byteArray[i * 4 + 2] = ((value shr 16) and 0xFF).toByte()
        byteArray[i * 4 + 3] = ((value shr 24) and 0xFF).toByte()
    }
    return byteArray
}
