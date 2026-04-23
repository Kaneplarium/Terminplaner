package com.terminplaner.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.domain.model.Appointment
import com.terminplaner.domain.model.Task
import com.terminplaner.domain.repository.AppointmentRepository
import com.terminplaner.domain.repository.TaskRepository
import com.terminplaner.util.DataExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskEditUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val appointmentId: Long? = null,
    val appointments: List<Appointment> = emptyList(),
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val titleError: Boolean = false
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val appointmentRepository: AppointmentRepository,
    private val dataExportManager: DataExportManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0
    private val initialAppointmentId: Long = savedStateHandle.get<Long>("appointmentId") ?: 0

    private val _uiState = MutableStateFlow(TaskEditUiState(
        isEditMode = taskId > 0,
        appointmentId = if (initialAppointmentId > 0) initialAppointmentId else null
    ))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appointmentRepository.getAllAppointments().collect { appointments ->
                _uiState.update { it.copy(appointments = appointments) }
            }
        }

        viewModelScope.launch {
            if (taskId > 0) {
                taskRepository.getTaskById(taskId)?.let { task ->
                    _uiState.update {
                        it.copy(
                            id = task.id,
                            title = task.title,
                            description = task.description ?: "",
                            isCompleted = task.isCompleted,
                            appointmentId = task.appointmentId
                        )
                    }
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

    fun updateAppointment(appointmentId: Long?) {
        _uiState.update { it.copy(appointmentId = appointmentId) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }

        viewModelScope.launch {
            val task = Task(
                id = state.id,
                title = state.title,
                description = state.description.ifBlank { null },
                isCompleted = state.isCompleted,
                appointmentId = state.appointmentId
            )

            if (state.isEditMode) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.insertTask(task)
            }
            dataExportManager.autoExport()
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
