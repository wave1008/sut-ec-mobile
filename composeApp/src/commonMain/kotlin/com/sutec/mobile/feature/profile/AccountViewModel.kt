package com.sutec.mobile.feature.profile

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.User
import com.sutec.mobile.data.repository.AuthRepository
import com.sutec.mobile.i18n.AppLanguage
import com.sutec.mobile.i18n.LocaleController
import kotlinx.coroutines.flow.StateFlow

class AccountViewModel(
    private val authRepository: AuthRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
    val language: StateFlow<AppLanguage> = localeController.language

    fun setLanguage(language: AppLanguage) {
        localeController.set(language)
    }

    fun logout() {
        authRepository.logout()
    }
}
