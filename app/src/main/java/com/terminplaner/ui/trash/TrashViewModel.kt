package com.terminplaner.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val deletedAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appointmentRepository.getDeletedAppointments().collect { appointments ->
                _uiState.update { it.copy(deletedAppointments = appointments, isLoading = false) }
            }
        }
    }

    fun restoreAppointment(id: Long) {
        viewModelScope.launch {
            appointmentRepository.restoreAppointment(id)
        }
    }

    fun permanentlyDelete(id: Long) {
        viewModelScope.launch {
            appointmentRepository.permanentlyDeleteAppointment(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            appointmentRepository.emptyTrash()
        }
    }
}