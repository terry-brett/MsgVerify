import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        let context = ComposeApp.PlatformContext()
        ComposeApp.KoinKt.doInitKoin(platformContext: context)
        MsgVerifyShortcuts.updateAppShortcutParameters()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    guard url.scheme == "msgverify",
                          url.host == "share",
                          let text = url.queryValue(for: "text") else { return }
                    MainViewControllerKt.setSharedText(text: text)
                }
        }
    }
}

private extension URL {
    func queryValue(for key: String) -> String? {
        URLComponents(url: self, resolvingAgainstBaseURL: false)?
            .queryItems?
            .first(where: { $0.name == key })?
            .value
    }
}
