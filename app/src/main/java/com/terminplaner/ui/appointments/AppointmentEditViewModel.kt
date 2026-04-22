package com.terminplaner.ui.appointments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AppointmentEditUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val categoryId: Long? = null,
    val color: Int? = null,
    val categories: List<Category> = emptyList(),
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: Boolean = false
)

@HiltViewModel
class AppointmentEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appointmentId: Long = savedStateHandle.get<Long>("appointmentId") ?: 0
    private val selectedDate: Long = savedStateHandle.get<Long>("selectedDate") ?: System.currentTimeMillis()

    private val _uiState = MutableStateFlow(AppointmentEditUiState(isEditMode = appointmentId > 0))
    val uiState: StateFlow<AppointmentEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            if (appointmentId > 0) {
                appointmentRepository.getAppointmentById(appointmentId)?.let { appointment ->
                    _uiState.update {
                        it.copy(
                            id = appointment.id,
                            title = appointment.title,
                            description = appointment.description ?: "",
                            dateTime = appointment.dateTime,
                            categoryId = appointment.categoryId,
                            color = appointment.color
                        )
                    }
                }
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                _uiState.update { it.copy(dateTime = calendar.timeInMillis) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = false) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateDateTime(dateTime: Long) {
        _uiState.update { it.copy(dateTime = dateTime) }
    }

    fun updateCategory(categoryId: Long?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun updateColor(color: Int?) {
        _uiState.update { it.copy(color = color) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            val appointment = Appointment(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                dateTime = state.dateTime,
                categoryId = state.categoryId,
                color = state.color
            )

            if (state.isEditMode) {
                appointmentRepository.updateAppointment(appointment)
            } else {
                appointmentRepository.insertAppointment(appointment)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}