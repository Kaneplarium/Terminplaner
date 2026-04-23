package com.terminplaner.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminplaner.data.preferences.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    fun setThemeColor(color: Long) {
        viewModelScope.launch {
            themePreferences.setThemeColor(color)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            themePreferences.setFirstRunCompleted()
        }
    }
}
