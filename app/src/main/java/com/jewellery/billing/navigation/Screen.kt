package com.jewellery.billing.navigation

sealed class Screen(val route: String) {
    object RateInput : Screen("rate_input")
    object Billing : Screen("billing")
    object BillSummary : Screen("bill_summary")
}