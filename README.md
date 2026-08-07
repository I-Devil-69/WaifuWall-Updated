Here is the README content in plain text. You can copy and paste this into README.md.

# Waifu Walls

Waifu Walls is a native Android wallpaper app focused on anime / "waifu" wallpapers. It uses modern Android tooling (Jetpack Compose, Material You), supports multiple wallpaper sources, advanced search filters, offline Room favorites, and wallpaper setup.

---

## Key features
- Material You / Compose-first UI
- Multi-source wallpaper API integration
- Advanced search & filtering
- Offline favorites using Room
- Set wallpapers from the app
- Deep link support: `waifuwalls://wallpaper`

---

## Requirements
- JDK 11 (project is configured for Java 11 compatibility)
- Android Studio (Flamingo or later recommended)
- Android SDK: compile/target SDK 36
- Gradle (wrapper included)
- Minimum supported Android: API 24

---

## Quick start

1. Clone the repository
   git clone https://github.com/MohitEvil/WaifuWall.git
   cd WaifuWall

2. Open in Android Studio
   - Open the project in Android Studio and let it sync Gradle.
   - Install any missing SDK components when prompted.

3. Build & run
   - From Android Studio: Run the app on a device or emulator.
   - From command line:
     ./gradlew assembleDebug
     ./gradlew installDebug   # installs to a connected device/emulator

4. Tests
   - Unit tests and Robolectric tests:
     ./gradlew test
   - Android instrumented tests:
     ./gradlew connectedAndroidTest

---

## Project structure (high level)
- `app/` — Android application module (Compose UI, Room, network)
- `assets/` — static assets (if any)
- `gradle/`, `build.gradle.kts`, `settings.gradle.kts` — build config
- `.env.example` — example secrets file (currently contains placeholders)
- `metadata.json` — project metadata / feature summary

---

## Configuration
- This repository includes an `.env.example` file used by the Secrets Gradle Plugin if you choose to adopt secret-backed configuration. It is optional — the app can be built without providing secret values.

---

## Deep linking
- The app declares a deep link in the manifest:
  - Scheme: `waifuwalls://wallpaper`

---

## Notes & optional features
- Several dependencies (camera, location, credentials) are present as commented lines in `app/build.gradle.kts` and can be enabled together if needed.

---

## Contributing
Contributions are welcome:
1. Open an issue to discuss major changes or features.
2. Create a branch for your work and open a pull request with a clear description and testing instructions.
3. Keep changes small and focused when possible.

I can add a CONTRIBUTING.md if you'd like.

---

## License
No license file detected in the repository. If you plan to publish or allow contributions, please add a LICENSE file (e.g., MIT, Apache 2.0) to clarify usage and contribution terms.

---

## Maintainer
Repository: https://github.com/MohitEvil/WaifuWall
Owner: MohitEvil (I-Devil-69)
