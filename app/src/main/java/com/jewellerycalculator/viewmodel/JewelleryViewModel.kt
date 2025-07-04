package com.jewellerycalculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jewellerycalculator.data.JewelleryDatabase
import com.jewellerycalculator.data.JewelleryItem
import com.jewellerycalculator.data.JewelleryRepository
import com.jewellerycalculator.data.Material
import com.jewellerycalculator.data.PriceCalculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JewelleryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: JewelleryRepository

    init {
        val database = JewelleryDatabase.getDatabase(application, viewModelScope)
        repository = JewelleryRepository(database.jewelleryItemDao())
    }

    private val _selectedMaterial = MutableStateFlow<Material?>(null)
    val selectedMaterial: StateFlow<Material?> = _selectedMaterial.asStateFlow()

    private val _currentRate = MutableStateFlow("")
    val currentRate: StateFlow<String> = _currentRate.asStateFlow()

    private val _selectedItem = MutableStateFlow<JewelleryItem?>(null)
    val selectedItem: StateFlow<JewelleryItem?> = _selectedItem.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _makingCharge = MutableStateFlow("")
    val makingCharge: StateFlow<String> = _makingCharge.asStateFlow()

    private val _calculatedPrice = MutableStateFlow<PriceCalculation?>(null)
    val calculatedPrice: StateFlow<PriceCalculation?> = _calculatedPrice.asStateFlow()

    private val _showNewItemDialog = MutableStateFlow(false)
    val showNewItemDialog: StateFlow<Boolean> = _showNewItemDialog.asStateFlow()

    fun selectMaterial(material: Material) {
        _selectedMaterial.value = material
        _selectedItem.value = null
        _calculatedPrice.value = null
    }

    fun updateCurrentRate(rate: String) {
        _currentRate.value = rate
    }

    fun selectItem(item: JewelleryItem) {
        _selectedItem.value = item
    }

    fun updateWeight(weight: String) {
        _weight.value = weight
    }

    fun updateMakingCharge(charge: String) {
        _makingCharge.value = charge
    }

    fun calculatePrice() {
        val rate = _currentRate.value.toDoubleOrNull() ?: return
        val weight = _weight.value.toDoubleOrNull() ?: return
        val makingCharge = _makingCharge.value.toDoubleOrNull() ?: return

        val calculation = PriceCalculation(
            weight = weight,
            rate = rate,
            makingCharge = makingCharge
        )
        _calculatedPrice.value = calculation
    }

    fun getItemsByMaterial(material: String) = repository.getItemsByMaterial(material)

    fun showNewItemDialog() {
        _showNewItemDialog.value = true
    }

    fun hideNewItemDialog() {
        _showNewItemDialog.value = false
    }

    fun addNewItem(name: String) {
        val material = _selectedMaterial.value ?: return
        val item = JewelleryItem(
            name = name,
            material = material.displayName,
            isDefault = false
        )
        viewModelScope.launch {
            repository.insertItem(item)
            _selectedItem.value = item
        }
    }

    fun resetCalculation() {
        _currentRate.value = ""
        _weight.value = ""
        _makingCharge.value = ""
        _calculatedPrice.value = null
        _selectedItem.value = null
    }
}