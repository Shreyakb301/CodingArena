import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Build the shared Koin graph once, before any Compose view exists.
        // Kotlin top-level functions land in Swift on a <FileName>Kt object.
        MainViewControllerKt.setupKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
