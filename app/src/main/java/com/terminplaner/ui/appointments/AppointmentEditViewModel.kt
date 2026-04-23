package com.terminplaner.ui.appointments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Category
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.CategoryRepository
import com.terminplaner.util.AlarmScheduler
import com.terminplaner.util.DataExportManager
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
    val endDateTime: Long = System.currentTimeMillis() + 3600000, // + 1 hour
    val categoryId: Long? = null,
    val color: Int? = null,
    val categories: List<Category> = emptyList(),
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: Boolean = false,
    val hasOverlap: Boolean = false,
    val showOverlapDialog: Boolean = false,
    val isPastDateError: Boolean = false
)

@HiltViewModel
class AppointmentEditViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val categoryRepository: CategoryRepository,
    private val alarmScheduler: AlarmScheduler,
    private val dataExportManager: DataExportManager,
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
                            endDateTime = appointment.endDateTime,
                            categoryId = appointment.categoryId,
                            color = appointment.color
                        )
                    }
                    checkForOverlap()
                }
            } else {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                val endCalendar = Calendar.getInstance().apply {
                    timeInMillis = calendar.timeInMillis
                    add(Calendar.HOUR_OF_DAY, 1)
                }
                _uiState.update { 
                    it.copy(
                        dateTime = calendar.timeInMillis,
                        endDateTime = endCalendar.timeInMillis
                    ) 
                }
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
        val duration = _uiState.value.endDateTime - _uiState.value.dateTime
        _uiState.update { 
            it.copy(
                dateTime = dateTime, 
                endDateTime = dateTime + duration,
                isPastDateError = isPast(dateTime)
            ) 
        }
        checkForOverlap()
    }

    private fun isPast(millis: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return millis < today
    }

    fun updateEndDateTime(endDateTime: Long) {
        _uiState.update { it.copy(endDateTime = endDateTime) }
        checkForOverlap()
    }

    private fun checkForOverlap() {
        val state = _uiState.value
        viewModelScope.launch {
            val overlapping = appointmentRepository.getOverlappingAppointments(
                state.dateTime,
                state.endDateTime,
                state.id
            )
            _uiState.update { it.copy(hasOverlap = overlapping.isNotEmpty()) }
        }
    }

    fun updateCategory(categoryId: Long?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun updateColor(color: Int?) {
        _uiState.update { it.copy(color = color) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        if (!state.isEditMode && isPast(state.dateTime)) {
            _uiState.update { it.copy(isPastDateError = true) }
            return
        }

        if (state.hasOverlap) {
            _uiState.update { it.copy(showOverlapDialog = true) }
        } else {
            performSave()
        }
    }

    fun confirmOverlapSave() {
        _uiState.update { it.copy(showOverlapDialog = false) }
        performSave()
    }

    fun dismissOverlapDialog() {
        _uiState.update { it.copy(showOverlapDialog = false) }
    }

    private fun performSave() {
        val state = _uiState.value
        viewModelScope.launch {
            val appointment = Appointment(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                dateTime = state.dateTime,
                endDateTime = state.endDateTime,
                categoryId = state.categoryId,
                color = state.color
            )

            val id = if (state.isEditMode) {
                appointmentRepository.updateAppointment(appointment)
                state.id
            } else {
                appointmentRepository.insertAppointment(appointment)
            }
            
            val finalAppointment = appointment.copy(id = id)
            alarmScheduler.schedule(finalAppointment)

            dataExportManager.autoExport()

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    // Deprecated, use onSaveClick
    fun save() = onSaveClick()
}
