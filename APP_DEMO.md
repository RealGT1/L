# Jewellery Billing App Demo

This document demonstrates the complete user workflow of the Jewellery Billing App.

## Demo Scenario

Let's walk through a complete billing scenario for a customer buying jewelry items.

### Step 1: Rate Input Screen

When the app launches, the user sees the Rate Input Screen:

```
┌─────────────────────────────────────┐
│        Enter Today's Rates          │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ Gold Rate (per gram)            │ │
│  │ 9100                            │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │ Silver Rate (per gram)          │ │
│  │ 108                             │ │
│  └─────────────────────────────────┘ │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │          Continue               │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

**User Action**: Enter Gold Rate: ₹9,100 and Silver Rate: ₹108, then tap Continue.

### Step 2: Main Billing Screen

The user is now on the main billing screen where they can add items:

```
┌─────────────────────────────────────┐
│        Jewellery Billing            │
│                                     │
│ ┌─────────────┐ ┌─────────────────┐ │
│ │ Add Gold    │ │ Add Silver      │ │
│ │ Item        │ │ Item            │ │
│ └─────────────┘ └─────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Silver Total:          ₹0.00   │ │
│ │ Gold Total:            ₹0.00   │ │
│ │ ─────────────────────────────── │ │
│ │ Grand Total:           ₹0.00   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │      Generate Bill              │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Step 3: Adding Silver Item 1

**User Action**: Tap "Add Silver Item" to see the Add Item Dialog:

```
┌─────────────────────────────────────┐
│        Add SILVER Item              │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Item Name/ID                    │ │
│ │ s1                              │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Weight (grams)                  │ │
│ │ 10                              │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Quantity                        │ │
│ │ 4                               │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Making Charge                   │ │
│ │ 250                             │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │      Price Calculation          │ │
│ │ Total Weight: 40.00g            │ │
│ │ Wastage (10%): 4.00g            │ │
│ │ Final Weight: 44.00g            │ │
│ │ Rate: ₹108.00/g                 │ │
│ │ Making Charge: ₹250.00          │ │
│ │ ─────────────────────────────── │ │
│ │ Total: ₹5,002.00                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────┐ ┌─────────────────────┐ │
│ │ Cancel  │ │     Add Item        │ │
│ └─────────┘ └─────────────────────┘ │
└─────────────────────────────────────┘
```

**User Action**: Tap "Add Item" to add the silver item.

### Step 4: Adding Silver Item 2

**User Action**: Tap "Add Silver Item" again and add:
- Item Name: s2
- Weight: 25g
- Quantity: 3
- Making Charge: ₹350

**Calculation**: 25g × 3 = 75g + 7.5g wastage = 82.5g × ₹108 + ₹350 = ₹9,260

### Step 5: Adding Gold Item

**User Action**: Tap "Add Gold Item" and add:
- Item Name: g1
- Weight: 15g
- Quantity: 1
- Making Charge: ₹1,500

**Calculation**: 15g × 1 = 15g + 1.5g wastage = 16.5g × ₹9,100 + ₹1,500 = ₹151,650

### Step 6: Updated Billing Screen

Now the billing screen shows all items with live totals:

```
┌─────────────────────────────────────┐
│        Jewellery Billing            │
│                                     │
│ ┌─────────────┐ ┌─────────────────┐ │
│ │ Add Gold    │ │ Add Silver      │ │
│ │ Item        │ │ Item            │ │
│ └─────────────┘ └─────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ s1              Delete [X]      │ │
│ │ 10g × 4 = 40.00g               │ │
│ │ Making: ₹250.00                 │ │
│ │ Total: ₹5,002.00                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ s2              Delete [X]      │ │
│ │ 25g × 3 = 75.00g               │ │
│ │ Making: ₹350.00                 │ │
│ │ Total: ₹9,260.00                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ g1              Delete [X]      │ │
│ │ 15g × 1 = 15.00g               │ │
│ │ Making: ₹1,500.00               │ │
│ │ Total: ₹151,650.00              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Silver Total:      ₹14,262.00   │ │
│ │ Gold Total:       ₹151,650.00   │ │
│ │ ─────────────────────────────── │ │
│ │ Grand Total:      ₹165,912.00   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │      Generate Bill              │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Step 7: Bill Summary Screen

**User Action**: Tap "Generate Bill" to see the detailed bill summary:

```
┌─────────────────────────────────────┐
│           Bill Summary              │
│                                     │
│           Silver Items              │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ s1                     SILVER   │ │
│ │ Weight: 10g                     │ │
│ │ Quantity: 4                     │ │
│ │ Total Weight: 40.00g            │ │
│ │ Wastage (10%): 4.00g            │ │
│ │ Final Weight: 44.00g            │ │
│ │ ─────────────────────────────── │ │
│ │ Metal Price: ₹4,752.00          │ │
│ │ Making Charge: ₹250.00          │ │
│ │ ─────────────────────────────── │ │
│ │ Total: ₹5,002.00                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ s2                     SILVER   │ │
│ │ Weight: 25g                     │ │
│ │ Quantity: 3                     │ │
│ │ Total Weight: 75.00g            │ │
│ │ Wastage (10%): 7.50g            │ │
│ │ Final Weight: 82.50g            │ │
│ │ ─────────────────────────────── │ │
│ │ Metal Price: ₹8,910.00          │ │
│ │ Making Charge: ₹350.00          │ │
│ │ ─────────────────────────────── │ │
│ │ Total: ₹9,260.00                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Silver Total: ₹14,262.00        │ │
│ └─────────────────────────────────┘ │
│                                     │
│            Gold Items               │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ g1                      GOLD    │ │
│ │ Weight: 15g                     │ │
│ │ Quantity: 1                     │ │
│ │ Total Weight: 15.00g            │ │
│ │ Wastage (10%): 1.50g            │ │
│ │ Final Weight: 16.50g            │ │
│ │ ─────────────────────────────── │ │
│ │ Metal Price: ₹150,150.00        │ │
│ │ Making Charge: ₹1,500.00        │ │
│ │ ─────────────────────────────── │ │
│ │ Total: ₹151,650.00              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Gold Total: ₹151,650.00         │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Grand Total: ₹165,912.00        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │         New Bill                │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Step 8: Start New Bill

**User Action**: Tap "New Bill" to clear the current bill and start fresh for the next customer.

## Key Features Demonstrated

1. **Rate Input**: Easy entry of daily metal rates
2. **Item Management**: Add multiple items with different metals
3. **Live Calculations**: Real-time price calculations with wastage
4. **Visual Feedback**: Clear display of calculations and totals
5. **Detailed Bill**: Complete breakdown for customer transparency
6. **Workflow**: Smooth navigation between screens
7. **Data Persistence**: Items are saved locally
8. **Error Handling**: Input validation and error messages

## Calculation Accuracy

The app correctly implements the business logic:
- **Total Weight** = Weight × Quantity
- **Wastage** = 10% of Total Weight
- **Final Weight** = Total Weight + Wastage
- **Item Price** = (Final Weight × Metal Rate) + Making Charge

All calculations are verified and match the expected results from the problem statement.