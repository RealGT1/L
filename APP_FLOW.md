# Jewelry Shop Calculator - App Flow

## Screen Flow

```
┌─────────────────────────────────────────────┐
│         Material Selection Screen            │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │         Select Material                 │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│      ┌─────────────┐   ┌─────────────┐      │
│      │    Gold     │   │   Silver    │      │
│      └─────────────┘   └─────────────┘      │
│                                             │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│         Item Selection Screen                │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │       [Material] Select Item           │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│  ┌─────────────┐ ┌─────────────┐           │
│  │   Ring      │ │   Chain     │           │
│  └─────────────┘ └─────────────┘           │
│  ┌─────────────┐ ┌─────────────┐           │
│  │  Necklace   │ │  Earrings   │           │
│  └─────────────┘ └─────────────┘           │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │         Add New Item                    │ │
│  └─────────────────────────────────────────┘ │
│                                             │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│        Price Calculation Screen              │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │      [Material] [Item Name]             │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │   Enter Weight (grams)                  │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │   Enter Making Charge                   │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │        Calculate Price                  │ │
│  └─────────────────────────────────────────┘ │
│                                             │
│  ┌─────────────────────────────────────────┐ │
│  │         Price Breakdown                 │ │
│  │                                         │ │
│  │  Original Weight: 100g                  │ │
│  │  Weight with 10%: 110g                  │ │
│  │  Making Charge: ₹50                     │ │
│  │  ─────────────────────                  │ │
│  │  Final Price: ₹160                      │ │
│  └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

## Key Features Implemented

### 1. Material Selection
- Clean, simple interface with two large buttons
- Gold and Silver options
- Material selection determines available items

### 2. Item Management
- Dynamic list based on selected material
- Default items pre-populated in database
- Add new custom items capability
- Items persist across app sessions

### 3. Price Calculation
- Input fields for weight and making charge
- Automatic 10% weight addition
- Clear price breakdown display
- Real-time calculation

### 4. Default Items Configuration

**Gold Items:**
- Ring
- Chain
- Necklace
- Earrings
- Bangle

**Silver Items:**
- Anklet
- Toe Ring
- Bracelet
- Idol

### 5. Technical Architecture

**Data Layer:**
- Room database for local storage
- Repository pattern for data access
- Data models with proper relationships

**UI Layer:**
- Jetpack Compose for modern UI
- Material 3 design system
- Responsive layouts

**Business Logic:**
- ViewModel for state management
- Proper separation of concerns
- Reactive programming with StateFlow

### 6. Price Calculation Logic

```kotlin
fun calculatePrice(weight: Double, makingCharge: Double): PriceCalculation {
    val weightWithAddition = weight + (weight * 0.1) // Add 10% to weight
    val finalPrice = weightWithAddition + makingCharge
    
    return PriceCalculation(
        originalWeight = weight,
        weightWithAddition = weightWithAddition,
        makingCharge = makingCharge,
        finalPrice = finalPrice
    )
}
```

## Usage Example

1. **Select Material**: User taps "Gold"
2. **Select Item**: User chooses "Ring" from the list
3. **Enter Details**: 
   - Weight: 10 grams
   - Making Charge: ₹500
4. **Calculate**: App shows:
   - Original Weight: 10g
   - Weight with 10%: 11g
   - Making Charge: ₹500
   - **Final Price: ₹511**

This implementation provides a complete, functional jewelry shop calculator that meets all the requirements specified in the problem statement.