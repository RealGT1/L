# Jewellery Calculator App

A local, offline Android application for a jewellery shop to calculate the price of items based on material type, weight, and making charges.

## Features

### Core Functionality
- **Material Selection**: Choose between Gold and Silver
- **Dynamic Item Lists**: Different default items based on material selection
- **Price Calculation**: Automatic calculation with 10% wastage inclusion
- **Custom Items**: Add new items not available in default lists
- **Offline Operation**: Works completely offline with local database

### Technical Features
- **Modern Architecture**: MVVM pattern with ViewModels
- **Room Database**: Local storage for custom items
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Latest Material Design components
- **Kotlin**: 100% Kotlin implementation

## Default Items

### Gold Items
- Ring
- Chain  
- Necklace
- Earrings
- Bangle

### Silver Items
- Anklet
- Toe Ring
- Bracelet
- Idol

## Price Calculation Formula

```
Final Price = (Weight + 10% Wastage) × Rate + Making Charge
```

Where:
- **Weight**: Weight of the item in grams
- **Wastage**: Automatically calculated as 10% of weight
- **Rate**: Current rate per gram for the selected material
- **Making Charge**: Additional charges for crafting

## User Interface Flow

1. **Material Selection Screen**: User selects Gold or Silver
2. **Price Calculation Screen**: 
   - Enter current rate for selected material
   - Select item from dropdown or add new item
   - Enter weight and making charge
   - View calculated price with breakdown

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)

## Project Structure

```
app/
├── src/main/java/com/jewellerycalculator/
│   ├── MainActivity.kt                 # Main entry point
│   ├── data/
│   │   ├── JewelleryItem.kt           # Data models
│   │   ├── JewelleryItemDao.kt        # Database access
│   │   ├── JewelleryDatabase.kt       # Room database
│   │   └── JewelleryRepository.kt     # Data repository
│   ├── ui/
│   │   ├── MaterialSelectionScreen.kt # Material selection UI
│   │   ├── PriceCalculationScreen.kt  # Price calculation UI
│   │   └── theme/                     # UI theming
│   └── viewmodel/
│       └── JewelleryViewModel.kt      # Business logic
└── src/main/res/
    ├── values/
    │   ├── strings.xml                # String resources
    │   ├── colors.xml                 # Color definitions
    │   └── themes.xml                 # App themes
    └── drawable/
        └── ic_launcher_foreground.xml # App icon
```

## Building the Project

1. **Prerequisites**:
   - Android Studio Arctic Fox or later
   - Android SDK 24 or higher
   - JDK 8 or higher

2. **Setup**:
   ```bash
   git clone <repository-url>
   cd L
   ./gradlew clean build
   ```

3. **Running**:
   - Open project in Android Studio
   - Run on emulator or physical device
   - Or use command line: `./gradlew installDebug`

## Database Schema

### JewelleryItem Table
- `id`: Primary key (auto-generated)
- `name`: Item name (e.g., "Ring", "Chain")
- `material`: Material type ("Gold" or "Silver")
- `isDefault`: Whether it's a default item or user-added

## Usage Instructions

1. **Launch App**: Open the Jewellery Calculator app
2. **Select Material**: Choose Gold or Silver from the main screen
3. **Enter Rate**: Input current market rate per gram
4. **Select Item**: Choose from dropdown or add new item
5. **Enter Details**: Input weight and making charge
6. **Calculate**: Tap "Calculate Price" to see the result
7. **View Breakdown**: See detailed calculation breakdown

## Features in Detail

### Material Selection
- Gold button with golden theme
- Silver button with silver theme
- Clear visual distinction between materials

### Price Calculation
- Real-time input validation
- Automatic wastage calculation (10%)
- Detailed price breakdown display
- Formula explanation for transparency

### Item Management
- Pre-populated default items
- Add custom items functionality
- Items persist in local database
- Sort by default items first, then alphabetically

## Offline Capability

The app is designed to work completely offline:
- All data stored locally using Room database
- No internet connection required
- All calculations performed on-device
- Custom items saved permanently

## Future Enhancements

Potential features for future versions:
- Export calculations to PDF/Excel
- Historical price tracking
- Multiple currency support
- Backup/restore functionality
- Advanced reporting features

## License

This project is created as a technical implementation for a jewellery shop price calculation system.