# Hush

Hush is an Android-only app that silently blocks incoming phone calls from numbers that are not saved in the user's contacts. Blocked calls never ring and never produce a notification. Inside the app, the user can see the history of blocked calls and some statistics about them.

The goal is **simple and effective**: one job, done reliably, with a small and readable codebase. iOS is out of scope (it ships a native "Silence Unknown Callers" feature).

## How blocking works

Blocking is built on the official Android Telecom API, not on workarounds:

- `HushCallScreeningService` extends `android.telecom.CallScreeningService`. The system calls `onScreenCall()` for every incoming call **before** the phone rings.
- On Android 10+ (API 29), any app can become the call screening app by holding `RoleManager.ROLE_CALL_SCREENING`. The user grants this role through a system dialog requested with `RoleManager.createRequestRoleIntent()`. Hush does **not** need to be the default dialer.
- To decide, the service looks the number up with `ContactsContract.PhoneLookup` (needs `READ_CONTACTS`). Unknown numbers are answered with a `CallResponse` that sets:
  - `setDisallowCall(true)` and `setRejectCall(true)`: the call is rejected and never rings. Depending on the carrier, the caller hears a busy tone or reaches voicemail.
  - `setSkipNotification(true)`: no missed-call notification.
  - `setSkipCallLog(...)`: configurable. Hidden from the system call log by default.
- Every blocked call is stored in Hush's own Room database. That database drives the History and Stats screens.
- Since Android 10 the screening app also sees outgoing calls. Only act on `Call.Details.CALL_DIRECTION_INCOMING`, and allow everything else untouched.
- `onScreenCall()` must respond fast (the system enforces a short timeout). Keep the lookup and the decision synchronous and cheap. Do the database write off the main thread and never let it delay `respondToCall()`.
- If anything fails (missing permission, lookup error), **allow the call**. A missed blocking is better than a missed legitimate call.

### Rules (planned settings)

- Block private/hidden numbers (no caller ID): on by default.
- Repeated caller: allow a number that calls twice within 3 minutes (emergencies, deliveries).
- Allowlist: numbers the user explicitly allows without saving them as contacts.
- Master on/off switch.

### Known limitations

- Requires Android 10+ (`minSdk = 29`). Older versions would require replacing the default dialer, which is out of scope.
- Only screens regular telephony calls. VoIP calls (WhatsApp, Telegram, etc.) do not go through Telecom and cannot be blocked.
- Emergency calls are never screened by the system.

## Permissions policy

- Required: `READ_CONTACTS` and the `ROLE_CALL_SCREENING` role.
- **Never** add `READ_CALL_LOG`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS` or `CALL_PHONE`. Hush keeps its own history, so it does not need them. `READ_CALL_LOG` is a Play Store restricted permission that requires a formal declaration.
- No network access. The app never sends data off the device, and the manifest has no `INTERNET` permission.

## Screens

1. **Home**: on/off switch, role and permission status with a call to action to grant them, count of calls blocked today.
2. **History**: list of blocked calls (number, date and time, reason) with quick actions: call back, save as contact, add to allowlist.
3. **Stats**: totals for today, week and month, a 30-day bar chart, most persistent numbers and peak hours. Charts are drawn with Compose `Canvas`, with no chart library.
4. **Settings**: the rules listed above.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.4 (Android Gradle Plugin built-in Kotlin support) |
| UI | Jetpack Compose (BOM), Material 3, dynamic color on Android 12+ |
| Navigation | Navigation 3 (`androidx.navigation3`), routes are `@Serializable` `NavKey`s |
| Persistence | Room 2.8 (blocked call history, allowlist) with KSP |
| Preferences | DataStore Preferences (settings) |
| Async | Kotlin Coroutines and Flow |
| Architecture | Single module, MVVM: Compose screen, then `ViewModel` exposing `StateFlow`, then repository, then Room/DataStore |
| DI | Manual, through a small `AppContainer` owned by `HushApplication`. No Hilt/Koin. |
| Build | Gradle (Kotlin DSL) with a version catalog in `gradle/libs.versions.toml` |
| SDK | `minSdk 29`, `targetSdk 37`, `compileSdk` 37.2 (minor API level, required by Compose 1.13), Java/JVM target 17 |

AGP 9 compiles Kotlin by itself (built-in Kotlin). Do **not** apply `org.jetbrains.kotlin.android`. The Kotlin version comes from the `kotlin` entry in the version catalog, through the Compose compiler and serialization plugins.

Keep the dependency list short. Ask before adding a new library, especially anything that pulls in network, analytics or ads.

## Project layout

```
app/src/main/java/com/iancovski/hush/
├── HushApplication.kt        # creates AppContainer
├── MainActivity.kt           # single activity, hosts Compose
├── AppContainer.kt           # manual DI: database, repositories, settings
├── screening/                # CallScreeningService and blocking decision logic
├── data/                     # Room entities, DAOs, database, repositories, DataStore
└── ui/
    ├── theme/                # Material 3 theme
    ├── navigation/           # NavKeys and NavDisplay setup
    ├── home/
    ├── history/
    ├── stats/
    └── settings/
```

Keep the blocking decision logic (`screening/`) in plain Kotlin that does not depend on Android framework types where possible, so it can be unit-tested on the JVM.

## Commands

Run from the repository root (Windows: `gradlew.bat`, Git Bash: `./gradlew`):

```bash
./gradlew assembleDebug          # build debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug           # install on a connected device/emulator
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device required)
./gradlew lint                   # Android lint
```

Gradle runs on the JDK bundled with Android Studio (`JAVA_HOME` points to `Android Studio\jbr`). The Android SDK is located through `local.properties` (`sdk.dir`), which is machine-specific and not committed.

### Testing call screening manually

An emulator can simulate incoming calls: Extended controls, then Phone, then "Call device", or run `adb emu gsm call <number>`. Grant the call screening role in the app first. Test with a number that is in the emulator's contacts and with one that is not.

## Conventions

- All user-facing text lives in `res/values/strings.xml` (English, default), with a Brazilian Portuguese translation in `res/values-pt-rBR/strings.xml`. Never hardcode UI strings in Kotlin.
- Store phone numbers as received, and also in E.164-normalized form (`PhoneNumberUtils.formatNumberToE164` with the SIM/network country) for grouping in stats.
- Store timestamps as epoch milliseconds (UTC). Convert to local time only in the UI.
- Icons: Material Symbols added as vector drawables in `res/drawable`. Do not add `material-icons-extended`.
- Compose: stateless screen composables that receive state and callbacks. The `ViewModel` is wired in at the navigation entry. Provide `@Preview`s for screens.
