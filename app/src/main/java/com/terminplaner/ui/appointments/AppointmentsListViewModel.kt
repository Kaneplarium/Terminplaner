package com.terminplaner.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppointmentsListUiState(
    val appointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AppointmentsListViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentsListUiState())
    val uiState: StateFlow<AppointmentsListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appointmentRepository.getAllAppointments(),
                categoryRepository.getAllCategories()
            ) { appointments, categories ->
                AppointmentsListUiState(
                    appointments = appointments,
                    categories = categories,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            appointmentRepository.softDeleteAppointment(id)
        }
    }
}