package org.contextguard.lib.MLKit.messageClassification

import dev.kursor.ktensorflow.api.Interpreter
import dev.kursor.ktensorflow.api.InterpreterOptions
import dev.kursor.ktensorflow.api.ModelDesc
import dev.kursor.ktensorflow.api.Tensor
import dev.kursor.ktensorflow.api.TensorDataType
import dev.kursor.ktensorflow.api.TensorShape
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MessageVerifier {

    // One interpreter is created on first use and reused for every prediction. Creating one per
    // prediction leaked native memory on iOS, where Interpreter.close() is a no-op in
    // ktensorflow 0.2. The mutex serialises access, since an interpreter must not run
    // concurrently (the reason the previous code created a fresh one each time).
    private val mutex = Mutex()
    private var interpreter: Interpreter? = null

    suspend fun makePrediction(
        modelDesc: ModelDesc,
        inputIds: IntArray,
        attentionMask: IntArray
    ): FloatArray = mutex.withLock {
        val interpreter = interpreter ?: Interpreter(
            modelDesc = modelDesc,
            options = InterpreterOptions(
                numThreads = 4,
                useXNNPACK = true
            )
        ).also { interpreter = it }

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

        softmax(byteArrayToFloatArray(outputTensor.data))
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val exp = logits.map { kotlin.math.exp(it.toDouble()) }
        val sum = exp.sum()
        return exp.map { (it / sum).toFloat() }.toFloatArray()
    }

    companion object {
        private var instance: MessageVerifier? = null

        fun getInstance(): MessageVerifier {
            if (instance == null) {
                instance = MessageVerifier()
            }
            return instance!!
        }
    }
}

internal const val MAX_LEN = 128

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

private fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
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

        floatArray[i] = Float.fromBits(intBits)
    }
    return floatArray
}
