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
        if (hasMarketingPatterns(message)){
            labels.add("Marketing")
        }
    }

    fun checkAdultContent(){
        if (hasAdultContentPatterns(message)){
            labels.add("Adult Content")
        }
    }

    fun checkUrgency(){
        if (hasUrgencyOrIntimidationPatterns(message)){
            labels.add("Urgency/Intimidation")
        }
    }

    fun checkLinks(){
        if (containsUrl(rawMessage)){
            labels.add("Link Click Pressure")
        }
    }

    fun checkPersonalFinancialRequest(){

    }

    fun checkTooGoodToBeTrue(){
        if(hasTooGoodToBeTruePatterns(rawMessage)){
            labels.add("Too Good to Be True")
        }
    }

    fun checkCredentialVerificationRequest(){
        if(hasCredentialVerificationPatterns(message)){
            labels.add("Credential Verification Request")
        }
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
