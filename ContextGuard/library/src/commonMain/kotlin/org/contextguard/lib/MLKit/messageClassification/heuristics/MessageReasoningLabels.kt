import org.contextguard.lib.MLKit.messageClassification.heuristics.asksForFinancialOrPersonalInfo
import org.contextguard.lib.MLKit.messageClassification.heuristics.checkImpersonation
import org.contextguard.lib.MLKit.messageClassification.heuristics.containsUrl
import org.contextguard.lib.MLKit.messageClassification.heuristics.hasAdultContentPatterns
import org.contextguard.lib.MLKit.messageClassification.heuristics.hasCredentialVerificationPatterns
import org.contextguard.lib.MLKit.messageClassification.heuristics.hasMarketingPatterns
import org.contextguard.lib.MLKit.messageClassification.heuristics.hasTooGoodToBeTruePatterns
import org.contextguard.lib.MLKit.messageClassification.heuristics.hasUrgencyOrIntimidationPatterns
import org.contextguard.lib.MLKit.messageClassification.heuristics.normalise
import org.contextguard.models.Reason

class MessageReasoningLabels(val message: String, val sender: String) {

    private val rawMessage: String = message
    private val cleanMessage: String = message.normalise()
    private val labels: MutableList<Reason> = mutableListOf()

    fun checkImpersonation(){
        if (message.checkImpersonation(sender)){
            labels.add(Reason("Impersonation"))
        }
    }

    fun checkMarketing(){
        if (message.hasMarketingPatterns()){
            labels.add(Reason("Marketing"))
        }
    }

    fun checkAdultContent(){
        if (message.hasAdultContentPatterns()){
            labels.add(Reason("Adult Content"))
        }
    }

    fun checkUrgency(){
        if (message.hasUrgencyOrIntimidationPatterns()){
            labels.add(Reason("Urgency/Intimidation"))
        }
    }

    fun checkLinks(){
        if (rawMessage.containsUrl()){
            labels.add(Reason("Link Click Pressure"))
        }
    }

    fun checkPersonalFinancialRequest(){
        if (rawMessage.asksForFinancialOrPersonalInfo()){
            labels.add(Reason("Financial or Personal Information"))
        }
    }

    fun checkTooGoodToBeTrue(){
        if(rawMessage.hasTooGoodToBeTruePatterns()){
            labels.add(Reason("Too Good to Be True"))
        }
    }

    fun checkCredentialVerificationRequest(){
        if(message.hasCredentialVerificationPatterns()){
            labels.add(Reason("Credential Verification Request"))
        }
    }

    fun addLabels() : MutableList<Reason>{
        checkImpersonation()
        checkMarketing()
        checkAdultContent()
        checkUrgency()
        checkLinks()
        checkPersonalFinancialRequest()
        checkTooGoodToBeTrue()
        checkCredentialVerificationRequest()

        if (labels.isEmpty()) {
            labels.add(Reason("Uncategorized Spam"))
        }

        return labels;
    }
}
