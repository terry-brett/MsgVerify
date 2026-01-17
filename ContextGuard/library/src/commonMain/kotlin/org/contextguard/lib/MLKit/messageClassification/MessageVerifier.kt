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
    ): FloatArray {

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
            data = ByteArray(2 * 4),
            shape = TensorShape(1, 2),
            dataType = TensorDataType.Float32
        )

        interpreter.run(
            inputs = listOf(
                attentionMaskTensor,
                inputIdsTensor
            ),
            outputs = listOf(outputTensor)
        )

        val rawLogits = byteArrayToFloatArray(outputTensor.data)

        return softmax(rawLogits)
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val exp = logits.map { kotlin.math.exp(it.toDouble()) }
        val sum = exp.sum()
        return exp.map { (it / sum).toFloat() }.toFloatArray()
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

fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
    if (byteArray.size % 4 != 0) {
        throw IllegalArgumentException("ByteArray size must be a multiple of 4 for Float32 conversion.")
    }
    val floatArray = FloatArray(byteArray.size / 4)
    for (i in floatArray.indices) {
        val byteIndex = i * 4
        val intBits = (byteArray[byteIndex + 3].toInt() and 0xFF shl 24) or // MSB
                (byteArray[byteIndex + 2].toInt() and 0xFF shl 16) or
                (byteArray[byteIndex + 1].toInt() and 0xFF shl 8) or
                (byteArray[byteIndex].toInt() and 0xFF)               // LSB

        // Convert the 32-bit integer representation into a Float.
        floatArray[i] = Float.fromBits(intBits)
    }
    return floatArray
}
