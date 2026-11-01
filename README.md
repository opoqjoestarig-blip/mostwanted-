# Most Wanted MOD Launcher (starter project)

Android launcher shell for selecting a user-owned NFS Most Wanted game image through Android Storage Access Framework.

## What is implemented
- Android file picker (`ACTION_OPEN_DOCUMENT` via `OpenDocument`) for `.iso` / `.xiso` files.
- Persistable read permission and remembered URI.
- Displays selected filename and size.
- Does not copy, modify, patch, or delete the selected image or game saves.

## Important limitation
This is **not a playable game APK**. It is a launcher starter project only. The original game's Android engine/recompiled game code and the actual Bonus Marker / Pink Slip gameplay patch have not been integrated. Selecting an ISO does not run it. A compatible game engine, asset/layout knowledge, and native build integration are still required. Do not advertise this starter as a completed MOD.

## Build
Open this folder in Android Studio (JDK 17, Android SDK 35), allow Gradle sync, then Build > Build APK(s).

Package: `com.example.nfsmwmod`
Version: `0.1.0`
