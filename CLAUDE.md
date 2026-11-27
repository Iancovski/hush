# Hush

Hush is an open-source app for Android that blocks incoming phone calls from numbers that are not saved in the user's contacts.

`README.md` defines the app's scope: requirements, permissions, the setup wizard, features (call blocking, history, statistics), and settings (allowlist, notifications). It is the reference for what to build. Do not add features or settings that are not described there. Update it first when the scope changes.

## Tech Stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.4, compiled by AGP 9 built-in Kotlin |
| UI | Jetpack Compose (BOM), Material 3, fixed dark brand theme, Inter typography |
| Navigation | Navigation 3 (`androidx.navigation3`), routes are `@Serializable` `NavKey`s |
| Persistence | Room 2.8 with KSP (blocked call history, allowlist) |
| Preferences | DataStore Preferences (settings) |
| Async | Kotlin Coroutines and Flow |
| Background work | WorkManager (`work-runtime-ktx`) for the daily summary notification |
| Architecture | Single module, MVVM, unidirectional data flow |
| DI | Manual, through `AppContainer` owned by `HushApplication`. No Hilt or Koin. |
| Tests | JUnit 4, `kotlinx-coroutines-test`, Compose UI test, AndroidX Test |
| Build | Gradle Kotlin DSL, version catalog in `gradle/libs.versions.toml` |
| SDK | `minSdk 29`, `targetSdk 37`, `compileSdk` 37.2 (minor API level, required by Compose 1.13), JVM target 17 |

## Structure

```
app/src/main/java/com/iancovski/hush/
├── HushApplication.kt   # creates AppContainer
├── MainActivity.kt      # single activity, hosts Compose
├── AppContainer.kt      # manual DI: database, repositories, settings, app scope
├── screening/           # CallScreeningService and blocking decision logic
├── permissions/         # runtime check of the call screening role and contacts access
├── notifications/       # blocked call notifications and the daily summary
├── data/                # Room entities, DAOs, database, repositories, DataStore
└── ui/
    ├── theme/           # Material 3 theme: Color.kt, Type.kt, Theme.kt
    ├── navigation/      # NavKeys and NavDisplay setup
    ├── components/      # composables shared by several screens (the notification mode selector)
    └── setup/ home/ history/ allowlist/ notifications/   # one package per screen
app/src/test/            # JVM unit tests
app/src/androidTest/     # instrumented tests
app/schemas/             # exported Room schemas (committed)
third_party/             # licenses of bundled assets (Inter font)
```

Each screen package holds its `XScreen.kt` (stateless composables and previews), `XViewModel.kt`, and `XUiState.kt`. `setup/` holds the first-run wizard. `home/` shows the blocking switch, the permission warnings, and the 7-day blocked call counter.

## Commands

Run from the repository root (Windows: `gradlew.bat`, Git Bash: `./gradlew`):

```bash
./gradlew assembleDebug              # build debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug               # install on a connected device/emulator
./gradlew testDebugUnitTest          # JVM unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device required)
./gradlew lint                       # Android lint
```

Gradle runs on the JDK bundled with Android Studio (`JAVA_HOME` points to `Android Studio\jbr`). The Android SDK is located through `local.properties` (`sdk.dir`), which is machine-specific and not committed.

## Rules and Best Practices

### Kotlin

- Follow the official Kotlin coding conventions: 4-space indentation, trailing commas, no wildcard imports.
- Prefer `val` and read-only types (`List`, `Map`) in public APIs. Use `data class` for state, `sealed interface` for closed hierarchies (events, results), and `enum class` for fixed options.
- Never use `!!`. Handle nullability explicitly with `?.`, `?:`, or early returns.
- Keep declarations `private` by default and widen visibility only when needed.
- Use named arguments for booleans and ambiguous parameters.
- Write small, focused functions. Comments explain *why*, never restate the code.

### Architecture

- Unidirectional data flow: state flows down, events flow up. Layers are Compose UI, then `ViewModel`, then repository, then Room/DataStore. The UI never touches DAOs or DataStore directly.
- Repositories are the single source of truth. They expose `Flow` for observable data and `suspend` functions for one-shot operations.
- ViewModels extend plain `ViewModel` (never `AndroidViewModel`) and never hold a `Context`, `View`, or composable.
- Each ViewModel exposes one `uiState: StateFlow<XUiState>`, built with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`. Never expose a `MutableStateFlow`. User actions are public functions on the ViewModel.
- Dependencies are passed through constructors. `AppContainer` creates singletons (database, repositories, application `CoroutineScope`) lazily. ViewModels are created with `viewModelFactory { initializer { ... } }` using the container.
- Add an interface only where a test needs a fake implementation. No speculative abstractions.

### Coroutines and Flow

- Inject `CoroutineDispatcher`s as constructor parameters with a default value. Do not hardcode `Dispatchers.IO` inside classes.
- Suspend functions must be main-safe: the callee switches dispatcher with `withContext`. Room and DataStore are already main-safe.
- Never use `GlobalScope`. UI work runs in `viewModelScope`. Work that must outlive a screen or the service uses the application scope from `AppContainer` (`SupervisorJob()` plus an injected dispatcher).
- Never swallow `CancellationException`. Catch specific exceptions instead of `Exception`/`Throwable` in suspend code.
- Transform streams with Flow operators (`map`, `combine`, `flatMapLatest`) instead of collecting and re-emitting.

### Jetpack Compose

- Screens are stateless: `XScreen(uiState, onAction..., modifier)`. The ViewModel is obtained at the Navigation 3 entry, which collects state with `collectAsStateWithLifecycle()` and passes it down.
- Every composable that emits UI takes `modifier: Modifier = Modifier` as its first optional parameter and applies it once, to its root.
- Unit-returning composables use PascalCase nouns (`BlockedCallItem`, not `showBlockedCall`).
- Hoist state to the lowest common owner. Use `remember`/`rememberSaveable` only for UI element state (expanded, scroll), never for business data.
- Composables are free of side effects. Use `LaunchedEffect`/`DisposableEffect` with correct keys when needed.
- Performance: UI state uses immutable types; give `LazyColumn` items a stable `key`; wrap expensive calculations in `remember`; use `derivedStateOf` for values derived from fast-changing state; defer fast-changing reads with lambda modifiers (`Modifier.offset { }`, `drawBehind { }`).
- Use `MaterialTheme.colorScheme` and `MaterialTheme.typography`. Never hardcode colors or text sizes.
- Edge-to-edge is mandatory on `targetSdk` 35+. Call `enableEdgeToEdge()` with `SystemBarStyle.dark` (light bar icons, since the app is always dark) and apply `Scaffold` inner padding and window insets.
- Accessibility: meaningful icons get a `contentDescription` from string resources (decorative ones get `null`), touch targets are at least 48dp, and information is never conveyed by color alone.
- Provide `@Preview`s for every screen, wrapped in `HushTheme`, covering the main states (empty, loaded).
- Icons are Material Symbols added as vector drawables in `res/drawable`. Do not add `material-icons-extended`.

### Design

- Hush is always dark. There is no light theme and no dynamic color: `HushTheme` takes no theme parameters and always applies the brand palette.
- Brand palette, mapped to the Material 3 color scheme in `ui/theme/Color.kt`:

  | Color | Hex | Use | Color scheme role |
  | --- | --- | --- | --- |
  | Main purple | `#7B5CFF` | Buttons and highlights | `primary` |
  | Lilac | `#A886FF` | Gradients | `secondary`, `tertiary` |
  | Dark background | `#1C1B24` | Background | `background`, `surface` |
  | Soft white | `#F5F5FA` | Text | `onBackground`, `onSurface` |

- Status colors, for icons that show a state (always next to text that says the same):

  | Color | Hex | Use | Color scheme role |
  | --- | --- | --- | --- |
  | Green | `#4ADE80` | Positive state, such as active protection | `MaterialTheme.colorScheme.success` (extension) |
  | Red | `#F87171` | Negative state and errors, such as inactive protection | `error` |

- Gradients use `MaterialTheme.colorScheme.brandGradient` (purple to lilac). Do not build gradients from raw colors.
- Highlighted cards (such as the Home protection card) use `cardGradient` as background (a faint lilac tint at the top, 15% over `surfaceContainer`, fading down to `surfaceContainer`) and `cardHighlight` as a 1dp border (lilac at low opacity: brightest at the top edge, fading to a faint outline on the sides and bottom) for a sense of elevation. Keep the tint subtle: text and controls on the card must keep their contrast. Other cards (such as the Home menu with the history button) are flat: `surfaceContainerLow`, no gradient and no border.
- Text on `primary` uses `onPrimary` (pure white, the best contrast available on the main purple). For text links or colored text on the background, prefer `secondary` (lilac): `primary` on the dark background stays below the 4.5:1 text contrast ratio. This includes `TextButton`s (such as dialog buttons), which default to `primary`: pass `ButtonDefaults.textButtonColors(contentColor = secondary)`.
- Typography is Inter, bundled as a variable font in `res/font/inter_variable.ttf` (license in `third_party/inter/`) and applied to the whole Material 3 type scale in `ui/theme/Type.kt`. Never use downloadable fonts.

### Navigation 3

- Routes are `@Serializable` `data object`s or `data class`es implementing `NavKey`, defined in `ui/navigation/`. Pass only IDs and primitives, never full objects.
- Create the back stack with `rememberNavBackStack(...)` and render it with `NavDisplay` plus the `entryProvider { entry<Key> { ... } }` DSL.
- Add `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()` so each entry gets its own saved state and ViewModel scope.
- Screens receive navigation as callbacks (`onOpenSettings: () -> Unit`), never the back stack itself.
- The back stack starts at the setup wizard until setup is completed, and at Home after that. Finishing the wizard replaces the back stack, so Back never returns to it.

### Room

- DAO functions return `Flow` for observed queries and are `suspend` for one-shot reads and writes. No blocking DAO functions, no `allowMainThreadQueries()`.
- Keep `exportSchema = true` and commit `app/schemas/`. Every version bump ships a `Migration` or `AutoMigration`. Never use `fallbackToDestructiveMigration()`: the user's history must survive updates.
- Do aggregation (such as the 7-day blocked call count and the daily summary count) in SQL, not in Kotlin over full lists. The Home chart counts each of the last seven local days in one query: Kotlin computes the day boundaries with `java.time` (so daylight saving days keep their real length) and SQL sums the calls between them.
- Index columns used in `WHERE`, `GROUP BY`, and `ORDER BY` (timestamp, normalized number).
- Use `@Transaction` for multi-step writes. The database is a single instance created in `AppContainer`.

### DataStore

- Exactly one `DataStore<Preferences>` instance, created with the top-level `preferencesDataStore` delegate and accessed only through the settings repository.
- Define keys in one place. Expose a `Flow<Settings>` with defaults applied, and recover from `IOException` with `emptyPreferences()`.
- Write only inside `edit { }`, which is transactional.
- The settings are: blocking enabled (default `true`), notification mode (an `enum class` with `EVERY_CALL` as the default, `DAILY_SUMMARY`, and `NEVER`), and setup completed (default `false`). DataStore also keeps the internal "last summary at" timestamp for the daily summary. The allowlist lives in Room, not in DataStore.

### Call screening

- Only act on `Call.Details.DIRECTION_INCOMING`. Allow everything else untouched.
- Decision order: if blocking is off, allow. If the number is in the contacts or in the allowlist, allow. A hidden or private number (no number, or a presentation other than `TelecomManager.PRESENTATION_ALLOWED`) can never match, so it is blocked. Otherwise block. An allowed call is never recorded.
- A blocked call is rejected with `setDisallowCall(true)`, `setRejectCall(true)`, `setSkipNotification(true)`, and `setSkipCallLog(true)`. Android ignores the last one for rejected calls and always logs them as `CallLog.Calls.BLOCKED_TYPE`, so they show in the dialer history marked as blocked. That is outside the app's control.
- The allowlist is kept in the same in-memory snapshot, as a set of E.164 numbers, so the check never queries Room during the call. It is checked before the contacts, since it costs no disk query. The incoming number is normalized with the same `PhoneNumberNormalizer` used when numbers are added, so both sides match.
- `onScreenCall()` must lead to `respondToCall()` fast (the system enforces a short timeout). The service launches the screening in the application scope on the injected IO dispatcher (the contact lookup is a disk query) and calls `respondToCall()` from there, inside a `finally` that answers Allow if screening throws. Contacts are matched with `PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI`. Settings and the allowlist come from an in-memory snapshot (`ScreeningSnapshot`) kept up to date in the application scope, not from DataStore or Room reads during the call. Most calls arrive with the process dead, so the snapshot may still be loading: it waits for the first read for at most 1 second, then the call is allowed.
- Write the blocked call to the database in the application scope, off the main thread. It must never delay `respondToCall()`. In `EVERY_CALL` mode, post the Hush notification from there too.
- **Fail open**: on any error or missing permission, allow the call. A missed block is better than a missed legitimate call.
- Keep the decision logic in `screening/` as plain Kotlin without Android types, so it runs in JVM tests. The service is a thin adapter around it.

### Notifications

- Hush posts its notifications on its own two channels (`blocked_calls` and `daily_summary`, created on every app start), with text from string resources. Notifications use `VISIBILITY_PRIVATE`, so numbers stay hidden on a locked screen. Tapping a notification opens the history: the `PendingIntent` targets `MainActivity` with an extra (`MainActivity.openHistoryIntent`), and the navigation turns it into a back stack of Home and History.
- All posting goes through the `Notifier` interface (`AndroidNotifier` in the app, a fake in tests). It checks the notification permission before every post and silently posts nothing without it, so Hush behaves as `NEVER` without changing the stored setting. In `EVERY_CALL` mode, `BlockedCallAlerts` posts one notification per blocked call, after the call is recorded.
- The daily summary targets 20:00 local time. It is a `CoroutineWorker` enqueued as unique one-time work (`enqueueUniqueWork`), with an initial delay computed with `java.time` up to the next 20:00. Do not use periodic work, because its run time drifts over the days.
- Mode changes go through `NotificationModeController` (from the setup wizard and the notifications screen). When the mode changes to `DAILY_SUMMARY`, store "last summary at" as now and enqueue with `ExistingWorkPolicy.REPLACE`. When it changes to anything else, cancel the work. On app start (`HushApplication.onCreate`), if the mode is `DAILY_SUMMARY`, enqueue with `ExistingWorkPolicy.KEEP` in case the work was lost.
- The worker is a thin adapter around `DailySummaryRunner` (plain Kotlin, JVM-tested). It counts blocked calls since "last summary at" with a SQL query, so a late run never skips or double-counts calls. It posts nothing if the count is zero or the notification permission is missing. In every case it then stores "last summary at" as now and enqueues the next run for the following 20:00 with `ExistingWorkPolicy.APPEND_OR_REPLACE`: `REPLACE` would cancel the run that is still in progress. A run that starts after the mode changed does nothing.
- Android can delay the run (Doze, App Standby Buckets, battery saver). That is acceptable. Never use exact alarms (`SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`).
- Create the `WorkManager` dependency in `AppContainer` and pass it to the code that schedules work. Test the worker with `work-testing` (`TestListenableWorkerBuilder`).

### Permissions and privacy

- Required: `READ_CONTACTS` and the `ROLE_CALL_SCREENING` role. Check both at runtime every time (users can revoke them) and request them with Activity Result APIs. They are first requested in the setup wizard, which does not move on until both are granted. When Android no longer shows a dialog, open the matching system settings screen instead: default apps (`ACTION_MANAGE_DEFAULT_APPS_SETTINGS`) for the role, app info for contacts.
- Optional: `POST_NOTIFICATIONS` (Android 13+). Request it only when the user picks a notification mode other than `NEVER`. If it is not granted, behave as `NEVER` without changing the stored setting.
- **Never** add `READ_CALL_LOG`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, or `CALL_PHONE`. Hush keeps its own history. `READ_CALL_LOG` is a restricted Play Store permission.
- No network access: the manifest has no `INTERNET` permission and no data leaves the device.
- Never log phone numbers or contact data.

### Resources and data

- All user-facing text lives in `res/values/strings.xml` (English, default) with a Brazilian Portuguese translation in `res/values-pt-rBR/strings.xml`. Keep both files in sync. Never hardcode UI strings in Kotlin. Use `pluralStringResource` for counts.
- Store phone numbers as received and also E.164-normalized (`PhoneNumberUtils.formatNumberToE164`, through `PhoneNumberNormalizer`: a number without a country code is read as a number of the first country where it is valid, trying the SIM country first (the home country, which does not change when roaming), then the network, then the device locale). Allowlist matching uses the normalized form, which is unique in the table: the same number typed in another format is rejected as already added. A typed number that cannot be normalized is rejected as invalid, since it could never match. Allowlist entries have an optional name.
- Store timestamps as epoch milliseconds (UTC). Convert to local time with `java.time` only in the UI.

### Build and dependencies

- AGP 9 compiles Kotlin by itself. Do **not** apply `org.jetbrains.kotlin.android`. The Kotlin version comes from the `kotlin` catalog entry, through the Compose compiler and serialization plugins.
- Every dependency and plugin version lives in the version catalog. No hardcoded versions in build scripts.
- Keep the dependency list short. Ask before adding a library, especially anything that pulls in network, analytics, or ads.

## Testing

- **Unit tests** (`src/test`, JVM) cover the screening decision logic, repositories, and ViewModels. These are the priority.
- Use `runTest` from `kotlinx-coroutines-test` and inject a `StandardTestDispatcher`. ViewModel tests replace `Dispatchers.Main` with a `MainDispatcherRule`.
- Prefer fakes (in-memory implementations) over mocks. Do not add a mocking library.
- **Instrumented tests** (`src/androidTest`) cover DAOs with `Room.inMemoryDatabaseBuilder` and Compose screens with `createComposeRule`. Find nodes by semantics or text from resources, not by hardcoded strings.
- Name JVM tests with backticks describing behavior (`` `unknown number is blocked when rule is on` ``) and structure them as given, when, then. Instrumented tests use camelCase names (`unknownNumberIsBlocked`), because D8 rejects spaces in method names below API 30 and `minSdk` is 29.
- Before finishing a task, run `testDebugUnitTest` and `lint`, and make sure `assembleDebug` succeeds.
- **Manual call screening test**: on an emulator, grant the call screening role in the app, then simulate a call via Extended controls > Phone > "Call device" or `adb emu gsm call <number>`. Test with a number in the contacts and one that is not.
