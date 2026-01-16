class MessageReasoningLabels (val message: String, val sender: String) {

    private val rawMessage: String = message
    private val cleanMessage: String = normalise(message)
    private val messageSender: String = sender
    private val labels: MutableList<String> = mutableListOf()

    fun checkImpersonation(){
        if (checkImpersonation(message, sender)){
            labels.add("Impersonation")
        }
    }

    fun checkMarketing(){

    }

    fun checkAdultContent(){
        if (hasAdultContentPatterns(message)){
            labels.add("Adult Content")
        }
    }

    fun checkUrgency(){

    }

    fun checkLinks(){
        if (containsUrl(rawMessage)){
            labels.add("Link Click Pressure")
        }
    }

    fun checkPersonalFinancialRequest(){

    }

    fun checkTooGoodToBeTrue(){

    }

    fun checkCredentialVerificationRequest(){

    }

    fun addLabels() : MutableList<String>{
        checkImpersonation()
        checkMarketing()
        checkAdultContent()
        checkUrgency()
        checkLinks()
        checkPersonalFinancialRequest()
        checkTooGoodToBeTrue()
        checkCredentialVerificationRequest()

        if (labels.isEmpty()) {
            labels.add("Uncategorized Spam")
        }

        return labels;
    }
}
