import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        let context = ComposeApp.PlatformContext()
        ComposeApp.KoinKt.doInitKoin(platformContext: context)
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
