import SwiftUI

@main
struct iOSApp: App {

    init() {
        KoinHelperKt.startKoin()
    }


    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}