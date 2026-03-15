import AppIntents
import ComposeApp

struct VerifyMessageIntent: AppIntent {
    static var title: LocalizedStringResource = "Verify Message"
    static var description = IntentDescription("Check a message for phishing or scam content using MsgVerify.")
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Message Text", description: "The message you want to verify.")
    var text: String

    @MainActor
    func perform() async throws -> some IntentResult {
        MainViewControllerKt.setSharedText(text: text)
        return .result()
    }
}
