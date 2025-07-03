# Jewellery Billing App

An offline Android application for jewellery shops to create detailed bills for customers, functioning like a shopping cart system.

## Features

### Core Functionality
- **Rate Input Screen**: Enter daily Gold and Silver rates
- **Main Billing Screen**: Add multiple items to a single bill
- **Automatic Price Calculations**: Calculate prices with 10% wastage and making charges
- **Live Totals Display**: Real-time subtotals for Silver, Gold, and Grand Total
- **Bill Summary**: Detailed itemized bill with complete breakdown

### Technical Features
- **Offline Operation**: All data stored locally using Room database
- **Modern UI**: Built with Jetpack Compose and Material 3 design
- **MVVM Architecture**: Clean separation of concerns
- **Input Validation**: Comprehensive error handling and validation
- **Navigation**: Smooth flow between screens

## User Workflow

1. **Rate Input**: Enter current day's Gold and Silver rates
2. **Add Items**: Use "Add Gold Item" or "Add Silver Item" buttons
3. **Item Details**: Input name/ID, weight, quantity, and making charge
4. **Live Calculations**: See real-time price calculations with wastage
5. **Generate Bill**: Create final bill with detailed breakdown
6. **New Bill**: Start fresh for next customer

## Price Calculation Formula

For each item:
1. **Total Weight** = Weight × Quantity
2. **Wastage** = Total Weight × 10%
3. **Final Weight** = Total Weight + Wastage
4. **Total Price** = (Final Weight × Metal Rate) + Making Charge

### Example Calculation
- Silver Item: 10g × 4 qty = 40g + 4g wastage = 44g × ₹108 + ₹250 = ₹5,002
- Gold Item: 15g × 1 qty = 15g + 1.5g wastage = 16.5g × ₹9,100 + ₹1,500 = ₹151,650

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Architecture**: MVVM with StateFlow
- **Navigation**: Compose Navigation
- **Design**: Material 3

## Project Structure

```
app/src/main/java/com/jewellery/billing/
├── data/
│   ├── BillingDatabase.kt      # Room database
│   ├── BillingItemDao.kt       # Data access object
│   ├── BillingModels.kt        # Data models
│   └── BillingRepository.kt    # Repository pattern
├── ui/
│   ├── screens/
│   │   ├── RateInputScreen.kt  # Rate entry screen
│   │   ├── BillingScreen.kt    # Main billing screen
│   │   └── BillSummaryScreen.kt # Bill summary screen
│   ├── components/
│   │   ├── AddItemDialog.kt    # Add item dialog
│   │   ├── ItemCard.kt         # Item display card
│   │   └── BillItemCard.kt     # Bill item card
│   └── theme/                  # Material 3 theme
├── navigation/
│   └── Screen.kt               # Navigation routes
├── BillingViewModel.kt         # MVVM ViewModel
└── MainActivity.kt             # Main activity
```

## Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on Android device/emulator (API level 24+)

## Requirements

- Android 7.0 (API level 24) or higher
- Android Studio with Kotlin support
- Gradle 8.0+

## Building

```bash
./gradlew build
```

## Testing

The app includes unit tests for calculation logic:

```bash
./gradlew test
```

## License

This project is for educational and commercial use in jewellery shops.
