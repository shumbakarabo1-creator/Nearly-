# Nearly V1 - Everyday-Life Discovery in Mpumalanga

> Turn what you need into where to go with seamless Google Maps integration.

Nearly is a modern, mobile-first everyday-life discovery application. When people face an immediate real-world need ("I need a phone charger", "Where can I print documents?", "Fix my phone", "I need a nearby barber"), they often don't know the exact business name or which category to browse. Nearly bridges that gap by interpreting natural-language requests and instantly presenting verified, nearby open businesses, services, and shops with Google Maps navigation.

---

## 1. What Nearly Is

Nearly solves the everyday discovery problem:
- Traditional maps require you to already know business names or exact keywords.
- **Mpumalanga is the base province** of Nearly V1, featuring 10 major urban hubs and towns:
  - **Mbombela (Nelspruit)** [Capital & Economic Hub]
  - **eMalahleni (Witbank)** [Industrial & Energy Hub]
  - **Middelburg** [Mining & Commercial Hub]
  - **Secunda** [Retail & Petrochemical Hub]
  - **White River** [Lowveld Gateway]
  - **Barberton** [Heritage & Tourism]
  - **Hazyview** [Safari Gateway]
  - **Ermelo** [Agricultural Crossroad]
  - **Standerton** [Lekwa Hub]
  - **Lydenburg (Mashishing)** [Panorama Route Hub]
- Directly integrates **Google Maps** for turn-by-turn driving, walking, and transit navigation.

---

## 2. Google Maps Integration & Features

- **Google Maps Navigation**:
  - One-tap turn-by-turn navigation directly into the Google Maps Android app (`google.navigation:q=lat,lng&mode=d`).
  - Google Maps Search intent (`geo:0,0?q=...`) for exploring business surroundings.
  - Multi-modal travel time estimation: 🚗 Driving, 🚶 Walking, and 🚌 Transit.
  - Browser/Universal fallback link for desktop and web browsers.
- **Interactive Map Explorer**:
  - Toggle between **List View** and **Map View** in the Search tab.
  - Interactive radar map plotting all nearby places around your selected Mpumalanga town.
  - Tapping a pin inspects place information with instant directions.
  - Dedicated "Open in Google Maps" button to explore entire category searches on Google Maps.
- **Natural Language Intent Matching**: Type everyday queries (e.g. "phone charger", "print documents", "haircut", "school shoes", "headache pills", "solar geyser", "locksmith").
- **Dynamic Haversine Distance Calculation**: Switch your active town and all distances dynamically calculate using actual geographic coordinates.
- **Full Category Filter**: Food, Shopping, Services, Repairs, Transport, Health, Education, Printing, Beauty, Electronics, Other.
- **Deep Result Cards**:
  - Business name & verified rating
  - Category badge with icon & theme accent
  - Real-time Open/Closed indicator with hours
  - Dynamic distance from your chosen location
  - One-tap phone dialer (`tel:`)
  - One-tap Google Maps directions
  - One-tap bookmark save/unsave toggle
  - Full details modal bottom sheet with services list, hours, tags, and sharing.
- **Local Persistence (Zero-Account Required)**:
  - Room SQLite Database on Android; LocalStorage on Web.
- **User Profile & Customization**:
  - Editable name and default Mpumalanga town.
  - Dark mode and Light mode toggle.
- **Smooth 4-Tab Navigation**: Home, Search, Saved, Profile.

---

## 3. Project Structure

This project delivers both a **native Android Jetpack Compose** application and a **standalone mobile-first web app**:

```
.
├── app/                                    # Android Native Application
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt                 # Android Host Activity with dynamic theme
│   │   ├── data/
│   │   │   ├── MockData.kt                 # Realistic South African places dataset
│   │   │   ├── model/
│   │   │   │   ├── Category.kt             # Category enum & icon definitions
│   │   │   │   └── Place.kt                # Place data model & distance logic
│   │   │   ├── local/
│   │   │   │   ├── NearlyDatabase.kt       # Room Database (Version 1)
│   │   │   │   ├── dao/NearlyDao.kt        # Reactive Flow DAOs for Room
│   │   │   │   └── entity/                 # Room Entities (Saved places, history, profile)
│   │   │   └── repository/
│   │   │       └── NearlyRepository.kt     # Natural-language search & persistence
│   │   └── ui/
│   │       ├── NearlyApp.kt                # Root Scaffold & Bottom Navigation
│   │       ├── NearlyViewModel.kt          # M3 StateFlow ViewModel
│   │       ├── components/                 # PlaceCard, CategoryChip, Sheets
│   │       ├── screens/                    # Home, Search, Saved, Profile screens
│   │       └── theme/                      # Material 3 colors, typography, theme
│   └── build.gradle.kts                    # App dependencies (Room, Compose, Material 3)
│
├── index.html                              # Web MVP Entry point
├── style.css                               # Mobile-first responsive CSS with dark/light themes
├── app.js                                  # Web client logic, NLP search & LocalStorage engine
├── data.js                                 # Web mock dataset of South African hubs
├── database-schema.md                      # Production database architecture for V2
└── README.md                               # Project documentation
```

---

## 4. How to Run & Preview

### In Google AI Studio:
1. The **Streaming Android Emulator** automatically compiles and runs the native Android APK.
2. You can interact directly with the app in the preview panel:
   - Tap the search bar or prompt chips.
   - Tap the location pill (top right) to switch between Rosebank, Braamfontein, Sandton, Cape Town, etc.
   - Tap **Call** to test dialer intent, or **Directions** to test maps.
   - Tap the bookmark icon to save places, then navigate to the **Saved** tab.
   - Go to **Profile** to edit your name or switch to Dark Mode.

### Exporting / Downloading the Project:
1. In Google AI Studio, click the project menu (or settings icon) in the upper corner.
2. Select **Export to ZIP** or **Push to GitHub**.
3. To open in Android Studio:
   - Extract the ZIP.
   - Open Android Studio and select **Open existing project**.
   - Sync Gradle and click **Run** on any Android device or emulator.
4. To run the web version locally:
   - Open `index.html` in any web browser, or run `npx serve .` in the root folder.

---

## 5. How to Test on Android

1. **Search Test**: Type `"I need a phone charger"` into the search bar. Observe that Matrix Warehouse Rosebank, Gadget World, and iStore appear first under Electronics.
2. **Category Filter Test**: Tap the **Repairs** chip. Observe Dr. Phone Fix and QuickFix Mobile appear.
3. **Distance Filter Test**: Tap **Filters** in the search tab, select `< 1 km`. Observe only places within walking distance appear.
4. **Saving Test**: Tap the bookmark icon on any card. Check that the badge on the **Saved** bottom navigation updates immediately, and the place appears in the **Saved** tab.
5. **Area Switcher Test**: Tap the area pill on the Home screen and choose **Cape Town CBD**. Notice distances recalculate relative to Cape Town.
6. **Dark Mode Test**: Tap the theme toggle in the header or go to Profile &gt; Appearance &gt; Dark.

---

## 6. How to Replace Mock Data with a Real Backend

To transition from the local mock dataset to a live cloud API:

1. **Set Up the Backend API**:
   - Provision a PostgreSQL / Supabase or Firebase database using the schemas detailed in `database-schema.md`.
2. **Implement Retrofit / Ktor in Android**:
   - In `app/src/main/java/com/example/data/`, add an API interface:
     ```kotlin
     interface NearlyApiService {
         @GET("places/search")
         suspend fun searchPlaces(
             @Query("q") query: String,
             @Query("category") category: String?,
             @Query("lat") lat: Double,
             @Query("lng") lng: Double
         ): List<PlaceDto>
     }
     ```
3. **Repository Swap**:
   - In `NearlyRepository.kt`, replace `allMockPlaces` with calls to `NearlyApiService`.
   - Keep Room as the offline cache and local bookmark store.
4. **Web Version**:
   - In `app.js`, replace `PLACES_DATA` lookups with `fetch('/api/places?q=' + encodeURIComponent(query))`.

---

## 7. Future V2 Features

- **Real GPS Location**: Auto-detect user coordinates via Android FusedLocationProviderClient.
- **WhatsApp Direct Messaging**: In South Africa, many local artisans and shops operate over WhatsApp; add a "WhatsApp Chat" button directly on the card.
- **User Reviews & Photo Uploads**: Allow users to post reviews and photos of nearby discoveries.
- **Merchant Claim & Onboarding Portal**: Allow local businesses to verify their listing, adjust hours, and offer exclusive daily deals.
- **Community Tips**: "Load shedding status" indicator showing if a nearby cafe or shop has backup power / solar generators.
