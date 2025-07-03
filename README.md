# Jewelry Shop Calculator

An offline Android application for calculating jewelry prices for a jewelry shop.

## Features

- **Material Selection**: Choose between Gold or Silver materials
- **Item Management**: Select from pre-defined jewelry items or add custom items
- **Price Calculation**: Calculate final prices with automatic 10% weight addition and making charges
- **Offline Storage**: All data stored locally using Room database

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room
- **Architecture**: MVVM (Model-View-ViewModel)

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

## Price Calculation Logic

1. Original weight is entered by the user
2. 10% is automatically added to the weight
3. Making charge is added by the user
4. Final price = (Original weight + 10%) + Making charge

## How to Use

1. Select material (Gold or Silver)
2. Choose an item from the list or add a new item
3. Enter the weight of the item in grams
4. Enter the making charge
5. Calculate the final price with breakdown

## Project Structure

```
app/
├── src/main/java/com/jewelryshop/calculator/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── JewelryItem.kt
│   │   ├── JewelryItemDao.kt
│   │   ├── JewelryDatabase.kt
│   │   └── JewelryRepository.kt
│   ├── ui/
│   │   ├── JewelryCalculatorApp.kt
│   │   └── theme/
│   └── viewmodel/
│       └── JewelryViewModel.kt
└── src/test/java/com/jewelryshop/calculator/
    └── JewelryCalculatorTest.kt
```

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or device

## Requirements

- Android API 24 (Android 7.0) or higher
- Kotlin 1.9.10
- Android Gradle Plugin 8.1.4
