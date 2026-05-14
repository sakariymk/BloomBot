# Team 36

## 👥 Team members
- Therese  Siil
- Solveig Nygård
- Haris Malik
- Sakariye Mahamed Kahiye
- Viktoria Maria Gritsenko-Hofton
- Sverre Vilhelm Skofteland Schultz


## About the app
WIP

## 📦 How to run the app

1. **Download Android Studio** - [Guide](https://developer.android.com/studio/install)
2. **Clone the project to Android studio and open it** - clone the project via GitHub [Guide](https://www.geeksforgeeks.org/git/how-to-clone-android-project-from-github-in-android-studio/)

5. **Permissions** - The app needs location if the user wants to save locations based on live location. The app will ask for location permissions, so make sure that your emulator or physical device has live location enabled.

6. **Run the app**  - Use Android Studio's built in emulator or use a physical Android device (minSDK 33 / Android 13). [Guide](https://developer.android.com/studio/run/emulator) - **Make sure that you have internet connection, the app requires it to search up addresses to save a location!**

7. **Troubleshooting:** If an error occurs, make sure that you're running the latest Android Studio version. Run **Gradle sync**, then **Build -> Clean project.**


---

## 📚 Libaries

### 📍 Map and location
- **Mapbox Maps SDK** - Interactive map in the app.

- **Mapbox Maps Compose** - Compose integration for MapBox map, so the map can be used as a Composable.

- **Mapbox Search SDK** - Fetches place suggestions based on text search

- **Accompanist Permissions** - Runtime permissions for Jetpack Compose, used for requesting live location permission from the user and save it

### 🌐 Networks
- **Retrofit** - HTTP-client for REST API calls.

- **Retrofit Kotlinx Serialization Converter** - Connects Retrofit to Kotlinx Serialization for automatic JSON parsing.
- **OkHttp** - HTTP client that is used by retrofit


- **OkHttp SSE** - Enables the client to listen to a continuous stream of data from the server for live updates.



- **Ktor** - HTTP client to build asynchronous servers and clients in Kotlin

- **Kotlinx Serialization** - JSON-serializing and deserializing of data classes


### 🤖 AI / MCP
- **MCP Kotlin SDK Client** - Client for MCP, allows AI-models to communicate with MET's MCP server to fetch data


### 🗃️ Database
- **Room** - Local SQLite-database for saving plants and places in the app


### 📱 UI 
- **Jetpack Compose + Material3** - UI-framework.

- **Material Icons Extended** - Icon library for Compose

- **Coil** - For loading and showing images in Compose, used for loading SVG weather icons in this project

- **Navigation Compose** - Navigation between screens in Compose

### ⚙️ Architecture
- **Lifecycle** - Part of Jetpack Compose, allows components to observe and respond to state in the app.

- **Kotlinx Coroutines** - For asynchronous operations.

### 🪛 Testing
- **JUnit** - Library that is used for writing unit tests and testing them.

- **MockK** - For creating mock objects in unit tests, for example mock repositories that does not affect the real repositories.

- **Kotlinx Coroutines Test** - For testing code that uses coroutines.



## Picture sources

APA 7 style
