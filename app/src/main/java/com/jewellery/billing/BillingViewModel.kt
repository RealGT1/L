package com.jewellery.billing

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewellery.billing.data.BillingItem
import com.jewellery.billing.data.BillingRepository
import com.jewellery.billing.data.BillSummary
import com.jewellery.billing.data.MetalRates
import com.jewellery.billing.data.MetalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingViewModel(private val repository: BillingRepository) : ViewModel() {
    
    private val _metalRates = MutableStateFlow<MetalRates?>(null)
    val metalRates: StateFlow<MetalRates?> = _metalRates.asStateFlow()
    
    private val _billSummary = MutableStateFlow<BillSummary?>(null)
    val billSummary: StateFlow<BillSummary?> = _billSummary.asStateFlow()
    
    val currentBillItems = repository.getCurrentBillItems()
    
    fun setMetalRates(rates: MetalRates) {
        _metalRates.value = rates
    }
    
    fun addItem(
        name: String,
        weight: Double,
        quantity: Int,
        makingCharge: Double,
        metalType: MetalType
    ) {
        viewModelScope.launch {
            val metalRate = when (metalType) {
                MetalType.GOLD -> _metalRates.value?.goldRate ?: 0.0
                MetalType.SILVER -> _metalRates.value?.silverRate ?: 0.0
            }
            
            repository.addItem(
                name = name,
                weight = weight,
                quantity = quantity,
                makingCharge = makingCharge,
                metalType = metalType,
                metalRate = metalRate
            )
        }
    }
    
    fun deleteItem(item: BillingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
    
    fun generateBill(items: List<BillingItem>) {
        _metalRates.value?.let { rates ->
            val summary = repository.calculateBillSummary(items, rates)
            _billSummary.value = summary
        }
    }
    
    fun startNewBill() {
        viewModelScope.launch {
            repository.clearCurrentBill()
            _billSummary.value = null
            _metalRates.value = null
        }
    }
}