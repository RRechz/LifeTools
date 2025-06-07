// File: ui/truthordare/SpinnerViewModel.kt
package com.babelsoftware.lifetools.ui.truthordare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import java.util.Collections

data class SpinTarget(
    val winningItemIndex: Int,
    val targetRotationDegrees: Float // Çarkın toplam döneceği derece
)

data class SpinnerUiState(
    val items: List<String> = emptyList(),
    val currentInput: String = "",
    val selectedItem: String? = null,
    val isSpinning: Boolean = false,
    val spinTarget: SpinTarget? = null,
    val errorMessage: String? = null
)

class SpinnerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SpinnerUiState())
    val uiState: StateFlow<SpinnerUiState> = _uiState.asStateFlow()

    fun onInputChanged(newInput: String) {
        _uiState.update { it.copy(currentInput = newInput, errorMessage = null) }
    }

    fun addItem() {
        val newItem = _uiState.value.currentInput.trim()
        if (newItem.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lütfen bir metin girin.") }
            return
        }
        if (_uiState.value.items.contains(newItem)) {
            _uiState.update { it.copy(errorMessage = "'$newItem' zaten listede mevcut.") }
            return
        }
        _uiState.update { currentState ->
            currentState.copy(
                items = currentState.items + newItem,
                currentInput = "", // Giriş alanını temizle
                errorMessage = null
            )
        }
    }

    fun removeItem(itemToRemove: String) {
        _uiState.update { currentState ->
            currentState.copy(items = currentState.items.filterNot { it == itemToRemove })
        }
    }

    // YENİ: Öğeleri yeniden sıralamak için fonksiyon
    fun reorderItems(from: Int, to: Int) {
        _uiState.update { currentState ->
            val reorderedList = currentState.items.toMutableList().apply {
                // Öğeyi eski yerinden yeni yerine taşı
                add(to, removeAt(from))
            }
            currentState.copy(items = reorderedList)
        }
    }

    fun spinWheel() {
        val currentItems = _uiState.value.items
        if (currentItems.size < 2) {
            _uiState.update { it.copy(errorMessage = "Çarkı çevirmek için en az 2 öğe eklemelisiniz.") }
            return
        }

        if (_uiState.value.isSpinning) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSpinning = true, selectedItem = null, spinTarget = null, errorMessage = null) }

            val winningItemIndex = Random.nextInt(currentItems.size)
            val degreesPerSegment = 360f / currentItems.size

            // Kazanan dilimin orta açısını hesapla (0. dilimin ortası 0 derece olacak şekilde)
            // Bizim çizimimiz -90'dan başladığı için, bunu hesaba katmamız lazım.
            // Dilim 'i'nin çizimdeki orta açısı: (i * degreesPerSegment - 90f) + degreesPerSegment / 2f
            // Bu açının okun olduğu -90f (veya +270f) pozisyonuna gelmesi için gereken dönüş:
            val rotationNeededForAlignment =
                -(winningItemIndex * degreesPerSegment + degreesPerSegment / 2f)

            // Rastgele ofset (dilimin tam ortasında durmaması için, isteğe bağlı)
            // Bu ofset, dilimin sınırları içinde kalmalı.
            val randomOffsetInSegment =
                if (currentItems.size > 1) Random.nextDouble(-degreesPerSegment / 2.5, degreesPerSegment / 2.5).toFloat()
                else 0f

            var targetAngle = rotationNeededForAlignment + randomOffsetInSegment

            // Sonucun [0, 360) aralığında olmasını sağlamak için (opsiyonel, sadece son pozisyon için)
            // targetAngle = (targetAngle % 360f + 360f) % 360f // Bu satır, totalRotation'a ekleneceği için gerekmeyebilir

            val fullSpins = 3 // En az 3 tam tur
            // totalRotation, mevcut açıdan başlayarak bu kadar daha dönecek.
            // Eğer çark her zaman 0'dan başlıyormuş gibi hesaplıyorsak:
            val totalRotation = (fullSpins * 360f) + targetAngle
            // Eğer çark bir önceki pozisyonundan devam ediyorsa, o zaman:
            // val currentAngle = _uiState.value.spinTarget?.targetRotationDegrees % 360f ?: 0f
            // totalRotation = currentAngle + (fullSpins * 360f) + targetAngle

            _uiState.update {
                it.copy(
                    spinTarget = SpinTarget(winningItemIndex, totalRotation),
                )
            }
        }
    }

    fun setSpinningCompleted(winningItem: String) {
        _uiState.update { it.copy(isSpinning = false, selectedItem = winningItem, spinTarget = null) }
    }

    fun clearSelectedItem() {
        _uiState.update { it.copy(selectedItem = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}