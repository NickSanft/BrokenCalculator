package com.divora.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class UserDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")
        val SUBTRACTION_UNLOCKED = booleanPreferencesKey("subtraction_unlocked")
        val DIVISION_UNLOCKED = booleanPreferencesKey("division_unlocked")
        val MULTIPLICATION_UNLOCKED = booleanPreferencesKey("multiplication_unlocked")
        val ALL_OPERATIONS_UNLOCKED_ALREADY = booleanPreferencesKey("all_operations_unlocked_already")
        val ANSWER_ACHIEVEMENT_UNLOCKED = booleanPreferencesKey("answer_achievement_unlocked")
        val SQRT_UNLOCKED = booleanPreferencesKey("sqrt_unlocked")
        val PERCENT_UNLOCKED = booleanPreferencesKey("percent_unlocked")
        val THEME = stringPreferencesKey("theme")
    }

    open val subtractionUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[SUBTRACTION_UNLOCKED] ?: false
    }

    open val divisionUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[DIVISION_UNLOCKED] ?: false
    }

    open val multiplicationUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[MULTIPLICATION_UNLOCKED] ?: false
    }

    open val allOperationsUnlockedAlreadyFlow: Flow<Boolean> = context.dataStore.data.map {
        it[ALL_OPERATIONS_UNLOCKED_ALREADY] ?: false
    }
    
    open val answerAchievementUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[ANSWER_ACHIEVEMENT_UNLOCKED] ?: false
    }

    open val sqrtUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[SQRT_UNLOCKED] ?: false
    }

    open val percentUnlockedFlow: Flow<Boolean> = context.dataStore.data.map {
        it[PERCENT_UNLOCKED] ?: false
    }

    open val themeFlow: Flow<Theme> = context.dataStore.data.map {
        Theme.valueOf(it[THEME] ?: Theme.System.name)
    }

    open suspend fun setTheme(theme: Theme) {
        context.dataStore.edit {
            it[THEME] = theme.name
        }
    }

    open suspend fun setOperationUnlocked(operation: String, unlocked: Boolean) {
        context.dataStore.edit {
            when (operation) {
                "-" -> it[SUBTRACTION_UNLOCKED] = unlocked
                "/" -> it[DIVISION_UNLOCKED] = unlocked
                "*" -> it[MULTIPLICATION_UNLOCKED] = unlocked
                "√" -> it[SQRT_UNLOCKED] = unlocked
                "%" -> it[PERCENT_UNLOCKED] = unlocked
            }
        }
    }

    open suspend fun setAllOperationsAlreadyUnlocked(unlocked: Boolean) {
        context.dataStore.edit {
            it[ALL_OPERATIONS_UNLOCKED_ALREADY] = unlocked
        }
    }

    open suspend fun setAnswerAchievementUnlocked(unlocked: Boolean) {
        context.dataStore.edit {
            it[ANSWER_ACHIEVEMENT_UNLOCKED] = unlocked
        }
    }

    open suspend fun resetOperations() {
        context.dataStore.edit {
            it[SUBTRACTION_UNLOCKED] = false
            it[DIVISION_UNLOCKED] = false
            it[MULTIPLICATION_UNLOCKED] = false
            it[ALL_OPERATIONS_UNLOCKED_ALREADY] = false
            it[ANSWER_ACHIEVEMENT_UNLOCKED] = false
            it[SQRT_UNLOCKED] = false
            it[PERCENT_UNLOCKED] = false
        }
    }
}
