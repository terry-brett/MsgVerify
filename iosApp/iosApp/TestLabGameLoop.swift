import Foundation
import UIKit
import ComposeApp

/// Runs the benchmark on Firebase Test Lab as a Game Loop test (see benchmark/testlab/README.md).
///
/// Test Lab opens `firebase-game-loop://?scenario=N` once per scenario. Each scenario is one
/// benchmark run of the same protocol as benchmark/run_ios.sh: 10 passes after a 60 s wait. When
/// the CSV is written, it is copied to `Documents/GameLoopResults`, which Test Lab uploads, Test Lab
/// is told that the loop is complete, and the app exits so that the next scenario starts a new
/// process with its own cold start.
///
/// The `firebase-game-loop` scheme is registered only in the Test Lab build
/// (benchmark/testlab/build_ios.sh), so this code never runs in a normal build.
/// For a local check, the optional query items `passes` and `startDelayS` override the protocol.
/// The simulator asks for confirmation before it opens a custom URL, so a local check passes the
/// URL as a launch argument instead, which goes to the same handler:
/// `xcrun simctl launch booted <bundle-id> -testLabGameLoopURL "firebase-game-loop://?scenario=1&passes=1&startDelayS=0"`.
enum TestLabGameLoop {
    private enum State { case idle, running, finished }
    private static var state = State.idle
    private static var timer: Timer?

    /// Local check only: handles a Game Loop URL given as the `-testLabGameLoopURL` launch argument.
    static func handleLaunchArgument() {
        guard let text = UserDefaults.standard.string(forKey: "testLabGameLoopURL"),
              let url = URL(string: text) else { return }
        _ = handle(url)
    }

    /// Returns true if [url] is a Game Loop URL and was handled.
    static func handle(_ url: URL) -> Bool {
        guard url.scheme == "firebase-game-loop" else { return false }
        let items = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
        func int(_ name: String) -> Int? { items.first(where: { $0.name == name })?.value.flatMap { Int($0) } }
        let scenario = int("scenario") ?? 1
        let passes = int("passes") ?? 10
        let startDelayS = int("startDelayS") ?? 60
        let runId = "testlab-\(machineId())-run\(scenario)"

        switch state {
        case .running:
            return true  // The same URL delivered twice; the run is already going.
        case .finished:
            // Each run must start in a new process (one cold start per run). If Test Lab sends the
            // next scenario to this process before it has exited, record that instead of measuring.
            writeResult(named: "benchmark-\(runId).failed",
                        contents: Data("Scenario \(scenario) reached a process that had already run.\n".utf8))
            finish()
            return true
        case .idle:
            state = .running
        }

        // The protocol keeps the screen on with MsgVerify in the foreground until the run ends.
        UIApplication.shared.isIdleTimerDisabled = true
        writeResult(named: "conditions-\(runId).txt", contents: conditions(runId: runId))
        MainViewControllerKt.startTestLabBenchmark(
            runId: runId, passes: Int32(passes), startDelayMs: Int64(startDelayS) * 1000)
        timer = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { _ in poll(runId: runId) }
        return true
    }

    private static func poll(runId: String) {
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        for name in ["benchmark-\(runId).csv", "benchmark-\(runId).failed"] {
            let file = documents.appendingPathComponent(name)
            guard let data = FileManager.default.contents(atPath: file.path) else { continue }
            timer?.invalidate()
            timer = nil
            state = .finished
            writeResult(named: name, contents: data)
            finish()
            return
        }
    }

    /// Tells Test Lab that the loop is complete, then ends the process at once, so that the next
    /// scenario starts a new process. The background task keeps the app alive until the URL is sent.
    private static func finish() {
        UIApplication.shared.isIdleTimerDisabled = false
        _ = UIApplication.shared.beginBackgroundTask(withName: "GameLoopComplete") { exit(0) }
        guard let url = URL(string: "firebase-game-loop-complete://") else { exit(0) }
        UIApplication.shared.open(url) { _ in exit(0) }
    }

    private static func writeResult(named name: String, contents: Data) {
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let dir = documents.appendingPathComponent("GameLoopResults", isDirectory: true)
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        try? contents.write(to: dir.appendingPathComponent(name), options: .atomic)
    }

    /// Records what the CSV does not: power state at the start of a run.
    private static func conditions(runId: String) -> Data {
        let device = UIDevice.current
        device.isBatteryMonitoringEnabled = true
        let states: [UIDevice.BatteryState: String] = [
            .unknown: "unknown", .unplugged: "unplugged", .charging: "charging", .full: "full",
        ]
        let text = """
            run_id=\(runId)
            device=\(machineId()) (\(device.model)), \(device.systemName) \(device.systemVersion)
            battery_level=\(device.batteryLevel)
            battery_state=\(states[device.batteryState] ?? "unknown")
            low_power_mode=\(ProcessInfo.processInfo.isLowPowerModeEnabled)
            thermal_state=\(ProcessInfo.processInfo.thermalState.rawValue)

            """
        return Data(text.utf8)
    }

    /// Hardware model identifier (for example "iPhone10,4"), lower case, with other characters as "-".
    private static func machineId() -> String {
        var info = utsname()
        uname(&info)
        let machine = withUnsafeBytes(of: &info.machine) { raw in
            String(decoding: raw.prefix(while: { $0 != 0 }), as: UTF8.self)
        }
        return String(machine.lowercased().map { $0.isLetter || $0.isNumber ? $0 : "-" })
    }
}
