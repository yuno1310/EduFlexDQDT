# Project conventions

- Keep the native Android app in Java with XML layouts. Use the existing Material 3
  theme and shared color resources for UI changes.
- Preserve user changes and existing navigation IDs when updating screens.
- UI work must include usable loading, empty, and failure states where data is loaded.
- Keep continuous delivery working. Before finishing app or build changes, run
  `./gradlew --no-daemon :app:lintDebug :app:assembleDebug :app:assembleRelease :app:bundleRelease`
  with JDK 17 or 21 and Android SDK 35 configured. Fix failures; do not disable lint
  or release shrinking to make checks pass. If the environment prevents verification,
  state the exact limitation rather than claiming a successful build.
- For backend or pipeline changes, also run `cd backend && ./mvnw --batch-mode test`.
- Keep Firebase configuration and release signing material untracked. Development
  builds must work without Firebase. Never describe unsigned artifacts as store-ready.
- Update `.github/workflows/verify.yml` and README build instructions together when
  changing the build or delivery process.
