package com.terminplaner.ui.calendar

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

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val currentMonth: Long = System.currentTimeMillis(),
    val appointments: List<Appointment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            combine(
                _selectedDate,
                categoryRepository.getAllCategories()
            ) { date, categories ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.timeInMillis

                Pair(Pair(startOfDay, endOfDay), categories)
            }.flatMapLatest { (range, categories) ->
                appointmentRepository.getAppointmentsByDate(range.first, range.second)
                    .map { appointments -> Pair(appointments, categories) }
            }.collect { (appointments, categories) ->
                _uiState.update { it.copy(appointments = appointments, categories = categories) }
            }
        }
    }

    fun selectDate(date: Long) {
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun getCategoryForId(categoryId: Long?): Category? {
        return _uiState.value.categories.find { it.id == categoryId }
    }

    fun getAppointmentsForDate(date: Long): List<Appointment> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return _uiState.value.appointments.filter { appointment ->
            appointment.dateTime >= startOfDay && appointment.dateTime < endOfDay
        }
    }
}