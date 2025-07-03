package com.jewelryshop.calculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jewelryshop.calculator.data.JewelryRepository
import com.jewelryshop.calculator.data.JewelryItem
import com.jewelryshop.calculator.data.MaterialType
import com.jewelryshop.calculator.data.PriceCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JewelryViewModel(private val repository: JewelryRepository) : ViewModel() {
    
    private val _selectedMaterial = MutableStateFlow<MaterialType?>(null)
    val selectedMaterial: StateFlow<MaterialType?> = _selectedMaterial.asStateFlow()
    
    private val _jewelryItems = MutableStateFlow<List<JewelryItem>>(emptyList())
    val jewelryItems: StateFlow<List<JewelryItem>> = _jewelryItems.asStateFlow()
    
    private val _selectedItem = MutableStateFlow<JewelryItem?>(null)
    val selectedItem: StateFlow<JewelryItem?> = _selectedItem.asStateFlow()
    
    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()
    
    private val _makingCharge = MutableStateFlow("")
    val makingCharge: StateFlow<String> = _makingCharge.asStateFlow()
    
    private val _priceCalculation = MutableStateFlow<PriceCalculation?>(null)
    val priceCalculation: StateFlow<PriceCalculation?> = _priceCalculation.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.initializeDefaultItems()
        }
    }
    
    fun selectMaterial(material: MaterialType) {
        _selectedMaterial.value = material
        _selectedItem.value = null
        _weight.value = ""
        _makingCharge.value = ""
        _priceCalculation.value = null
        
        viewModelScope.launch {
            repository.getItemsByMaterial(material.displayName).collect { items ->
                _jewelryItems.value = items
            }
        }
    }
    
    fun selectItem(item: JewelryItem) {
        _selectedItem.value = item
        _priceCalculation.value = null
    }
    
    fun updateWeight(weight: String) {
        _weight.value = weight
        _priceCalculation.value = null
    }
    
    fun updateMakingCharge(charge: String) {
        _makingCharge.value = charge
        _priceCalculation.value = null
    }
    
    fun calculatePrice() {
        val weightValue = _weight.value.toDoubleOrNull()
        val chargeValue = _makingCharge.value.toDoubleOrNull()
        
        if (weightValue != null && chargeValue != null && weightValue > 0 && chargeValue >= 0) {
            val calculation = repository.calculatePrice(weightValue, chargeValue)
            _priceCalculation.value = calculation
        }
    }
    
    fun addNewItem(itemName: String) {
        val material = _selectedMaterial.value
        if (material != null && itemName.isNotBlank()) {
            val newItem = JewelryItem(
                name = itemName.trim(),
                material = material.displayName,
                isDefault = false
            )
            
            viewModelScope.launch {
                repository.insertItem(newItem)
            }
        }
    }
    
    fun resetToMaterialSelection() {
        _selectedMaterial.value = null
        _selectedItem.value = null
        _jewelryItems.value = emptyList()
        _weight.value = ""
        _makingCharge.value = ""
        _priceCalculation.value = null
    }
}

class JewelryViewModelFactory(private val repository: JewelryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JewelryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JewelryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}