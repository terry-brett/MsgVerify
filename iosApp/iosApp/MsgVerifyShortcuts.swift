import AppIntents

struct MsgVerifyShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: VerifyMessageIntent(),
            phrases: [
                "Verify a message with \(.applicationName)",
                "Check a message with \(.applicationName)",
                "Scan a message with \(.applicationName)"
            ],
            shortTitle: "Verify Message",
            systemImageName: "checkmark.shield"
        )
    }
}
