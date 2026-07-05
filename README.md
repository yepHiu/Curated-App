# Curated App

Curated App is a native Android client for a Curated media server. It focuses on a quiet, dark-first media library experience for browsing movies, opening rich movie and actor detail pages, and playing media through the Curated HTTP API.

The app is in active development. The backend server is expected to be provided separately and must implement the API contract documented in [doc/API.md](doc/API.md).

## Features

- Native Android UI built with Kotlin and Jetpack Compose.
- Configurable Curated server connection using the shared `/api` backend contract.
- PIN unlock support with `curated_auth` cookie persistence through the Android networking layer.
- Home recommendations from the Curated backend.
- Movie library browsing with search and paginated loading.
- Movie detail pages with covers, metadata, preview images, resume information, and direct playback entry.
- Actor listing and actor detail pages backed by Curated library APIs.
- Playback history and resume progress using Curated playback progress endpoints.
- Media3 / ExoPlayer playback using Curated playback descriptors, including direct stream and HLS URLs.
- Local privacy protection options, including visual protection overlay, screenshot and recent-task protection, system media mute, and player internal mute.
- Dark-first Curated visual style with light and system theme support.
- Floating bottom navigation for the primary Home, My media, and Settings destinations, with secondary destinations kept in the drawer.
- High-refresh display preference on supported Android devices.
- Open-source component credits shown in the About page without hiding dependency attribution.

## Requirements

- Android Studio with the bundled JDK, or another JDK compatible with the Android Gradle Plugin used by this repository.
- Android SDK with compile SDK 36 installed.
- Android device or emulator running Android 9.0 or newer, matching the project min SDK 28.
- A running Curated backend server that implements [doc/API.md](doc/API.md).

## Quick Start

Clone the repository:

```powershell
git clone git@github.com:yepHiu/Curated-App.git
cd Curated-App
```

Build a debug APK on Windows:

```powershell
.\gradlew.bat --console=plain :app:phone:assembleLibreDebug
```

Build on macOS or Linux:

```bash
./gradlew --console=plain :app:phone:assembleLibreDebug
```

The default Android application id is `dev.curated.app`. Debug builds use the `.debug` application id suffix.

## Development Commands

Compile the main Android app:

```powershell
.\gradlew.bat --console=plain :app:phone:compileLibreDebugKotlin
```

Run unit tests for the libre debug variant:

```powershell
.\gradlew.bat --console=plain :app:phone:testLibreDebugUnitTest
```

Create a debug APK:

```powershell
.\gradlew.bat --console=plain :app:phone:assembleLibreDebug
```

## Repository Layout

| Path | Purpose |
| --- | --- |
| `app/phone/` | Android phone application entry point, navigation, activities, and app-level UI. |
| `core/` | Shared app core, preferences, theme, resources, privacy helpers, and utility code. |
| `data/` | Data layer, Curated API models, repositories, URL resolution, and networking support. |
| `modes/film/` | Film-mode business UI and media-domain presentation components. |
| `player/` | Playback-related modules, including local Media3 / ExoPlayer integration. |
| `settings/` | Settings screens, preferences, About page, and privacy option presentation. |
| `setup/` | Initial setup, server management, address entry, and login flow. |
| `doc/` | API contract, project memory, architecture notes, and long-lived agent documentation. |

## Curated API Contract

All Curated HTTP endpoints live under the `/api` prefix. Common base URLs include:

| Scenario | API base URL |
| --- | --- |
| Local backend development | `http://127.0.0.1:8080/api` |
| Packaged local backend | `http://127.0.0.1:8081/api` |
| Android or LAN client | `http://<server-ip>:<port>/api` |

Important client rules are captured in [doc/API.md](doc/API.md):

- Store the base URL as a configurable value and trim trailing `/` before saving.
- Persist and return the `curated_auth` cookie for PIN-protected sessions.
- Resolve relative media URLs against the backend origin.
- URL-encode path parameters such as movie IDs, actor names, frame IDs, and HLS file names.
- Treat video, image, export download, and large upload endpoints as binary or streaming responses instead of JSON.
- Expect successful API responses to return DTO bodies directly, not `{ "ok": true, "data": ... }` envelopes.

## Privacy Protection

Curated App includes local privacy features intended to reduce accidental exposure when the app enters or leaves the foreground:

- A visual protection overlay with a dark scrim and platform blur where available.
- Optional `FLAG_SECURE` screenshot, screen recording, and recent-task preview protection.
- Optional system media stream muting during privacy transitions.
- Optional internal player volume muting during privacy mute states.
- Settings-page toggles for gaze protection, auto mute, secure screen, and player internal mute.

These features are local device protections. They do not replace backend authentication, network security, device lock settings, or operating-system account controls.

## Visual Style

The Android UI follows the Curated style guide in [doc/2026-06-08-curated-android-ui-color-style-guide.md](doc/2026-06-08-curated-android-ui-color-style-guide.md). The default direction is dark, content-first, low-distraction media browsing with a pink brand accent.

Core tokens include:

| Token | Value |
| --- | --- |
| Brand primary | `#FE628E` |
| Dark background | `#0D0F1A` |
| Dark surface | `#141826` |
| Light background | `#F4F6FC` |
| Light surface | `#FFFFFF` |

Kotlin and Compose UI code should prefer semantic theme tokens, `MaterialTheme.colorScheme`, or project theme wrappers instead of scattering hardcoded hex values through feature code.

## Status

This repository contains the Android client. It is not a standalone media server. For API behavior, backend assumptions, DTO shapes, and endpoint details, treat [doc/API.md](doc/API.md) as the source of truth.

## License

This project is licensed under [GPLv3](LICENSE).
