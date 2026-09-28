# KMP Desktop Base Project Creation Guide (Skills & Architecture)

This document provides a comprehensive guide to building a Compose Multiplatform (KMP) Desktop application, based on the `BundleToolUI-Desktop` structure. It covers dependencies, folder structure, storage, dependency injection (DI), configuration, and UI/ViewModel integration.

## 1. Dependencies and Libraries
The project utilizes a modern and robust stack for KMP development. Manage these via `gradle/libs.versions.toml`:

### Core Settings
- **Kotlin Integration:** `kotlin` (multiplatform plugin & serialization)
- **Compose Multiplatform:** `org.jetbrains.compose`
- **Coroutines:** `kotlinx-coroutines-core`, `kotlinx-coroutines-swing` (JVM GUI threading)

### UI & Architecture
- **Navigation:** `androidx.navigation:navigation-compose` (Compose Navigation)
- **Lifecycle/ViewModels:** `androidx.lifecycle:lifecycle-viewmodel-compose`, `androidx.lifecycle:lifecycle-runtime-compose`

### Dependency Injection
- **Koin Multiplatform:** 
  - `io.insert-koin:koin-core`
  - `io.insert-koin:koin-compose`
  - `io.insert-koin:koin-compose-viewmodel`
  - `io.insert-koin:koin-compose-viewmodel-navigation`

### Storage
- **DataStore Preferences:** `androidx.datastore:datastore`, `androidx.datastore:datastore-preferences`

### Networking
- **Ktor Client:** `ktor-client-core`, `ktor-client-cio` (for JVM), content negotiation, and serialization.

### Logging
- **Logback & SLF4J:** `ch.qos.logback:logback-classic`, `org.slf4j:slf4j-android`

---

## 2. Folder Structure
The architecture follows a clean package-by-feature / layer approach. For a standard Desktop application:

```text
composeApp/
├── build.gradle.kts    <-- Desktop/app-level build configuration
├── src/
│   ├── commonMain/     <-- Shared logic (if expanding to Android/iOS)
│   ├── jvmMain/
│   │   ├── composeResources/  <-- Images, fonts, strings
│   │   ├── kotlin/
│   │   │   ├── data/          <-- Models, state representations, sealed events
│   │   │   ├── di/            <-- Koin modules definition
│   │   │   ├── local/         <-- DataStore, Storage providers, Preferences
│   │   │   ├── ui/
│   │   │   │   ├── components/ <-- Reusable UI (Buttons, TextFields, Dialogs)
│   │   │   │   ├── navigation/ <-- NavHost and routes
│   │   │   │   ├── theme/      <-- Colors, Typography
│   │   │   │   ├── windows/    <-- Main app screens (Splash, Home)
│   │   │   │   │   └── viewmodel/ <-- ViewModels matching corresponding screens
│   │   │   │   └── App.kt      <-- Root Composable wrapping NavHost and Theme
│   │   │   ├── usecase/       <-- Business logic handlers
│   │   │   ├── utils/         <-- Extension functions, loggers, formatters
│   │   │   └── Main.kt        <-- JVM application entry point `fun main() = application { ... }`
```

---

## 3. Storage Configuration
Local key-value storage uses [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore). 

### Creating the DataStore
You create a singleton provider that identifies the path string (e.g. producing an `.preferences_pb` file):
```kotlin
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
```

### Preference Wrapper
A wrapper (`AppDataStore`) exposes suspend functions to handle primitive types and objects via Kotlinx Serialization:
```kotlin
suspend inline fun <reified T> saveObject(key: Preferences.Key<String>, obj: T) {
    val jsonString = json.encodeToString(obj)
    save(key, jsonString)
}
```

---

## 4. Dependency Injection (DI) with Koin
Define all modules inside `di/Modules.kt` and group them by layer:

```kotlin
fun viewModelModules() = module {
    viewModel { HomeViewModel(get(), get()) }
}

fun storageModules() = module {
    single { StorageProvider.provideDataStore() }
    single { AppDataStore(get()) }
    single { AppPreferences(get()) }
}
```

**Starting DI:**
Wrap your Root Composable in a `KoinContext` and start Koin from the `Main.kt` entry point.
```kotlin
@Composable
fun App() {
    KoinContext { // Needed for injecting directly into Compose Tree
        AppTheme { AppNavHost() }
    }
}
```

---

## 5. ViewModels and State Management
ViewModels use `MutableStateFlow` to manage the UI state, reacting to inputs defined as `Events` (e.g., sealed classes).

### ViewModel Structure
```kotlin
class HomeViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState

    // Handle UI Intention/Action
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Initialize -> loadData()
            is HomeEvent.UpdateValue -> _uiState.update { it.copy(value = event.value) }
        }
    }
}
```

---

## 6. UI Integration (Splash & Main Screens)
We utilize `Navigation Compose` to move from a Splash Screen to the Main Screen.

### Navigation Host (`AppNavHost.kt`)
```kotlin
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SplashRoute) {
        
        composable<Routes.SplashRoute> {
            SplashWindow(onFinish = {
                navController.navigate(Routes.HomeRoute) {
                    popUpTo(Routes.SplashRoute) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        
        composable<Routes.HomeRoute> {
            val viewModel = koinViewModel<HomeViewModel>() // Resolved via Koin
            HomeWindow(viewModel)
        }
    }
}
```

### Splash Screen (`SplashWindow.kt`)
Using `LaunchedEffect` to hold the screen temporarily or await initialization:
```kotlin
@Composable
fun SplashWindow(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1000) // Or await some background setup
        onFinish()
    }
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator() // Plus your app logo here
    }
}
```

### Main Screen (`HomeWindow.kt`)
The UI collects the ViewModel's state lifecycle-safely and pushes user interactions via `.onEvent()`.
```kotlin
@Composable
fun HomeWindow(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(HomeEvent.Initialize)
    }

    Column {
        Text(text = "Welcome!")
        Button(onClick = { viewModel.onEvent(HomeEvent.ActionFired) }) {
            Text(text = "Take Action")
        }
    }
}
```

---

## 7. Configuration & Desktop Packaging
In `composeApp/build.gradle.kts`, the `compose.desktop.application {}` block handles packaging into DMG, MSI, and DEB formats. 
Icon mapping specifically links out to generic OS platform logic:
```kotlin
nativeDistributions {
    targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
    macOS { iconFile.set(file("desktop-icons/launcher.icns")) }
    windows { iconFile.set(file("desktop-icons/launcher.ico")) }
    linux { iconFile.set(file("desktop-icons/launcher.png")) }
}
```
You can also build custom Gradle tasks (e.g., `GenerateBuildConfigTask`) injected direct into `sourceSets` for custom build configurations and build flags mappings.
