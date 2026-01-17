fun main() {

    val message = "Hey Yash!, your account has been locked"
    val sender = "yash.soni@hmrc.com"

    val labeller = MessageReasoningLabels(message, sender)

    println(labeller.addLabels())
}