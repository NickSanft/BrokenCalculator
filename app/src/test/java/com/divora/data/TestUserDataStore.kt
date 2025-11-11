
package com.divora.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

class TestUserDataStore(context: Context) : UserDataStore(context) {

    val subtractionUnlocked = MutableStateFlow(false)
    val divisionUnlocked = MutableStateFlow(false)
    val multiplicationUnlocked = MutableStateFlow(false)
    val allOperationsUnlockedAlready = MutableStateFlow(false)
    val answerAchievementUnlocked = MutableStateFlow(false)
    val sqrtUnlocked = MutableStateFlow(false)
    val percentUnlocked = MutableStateFlow(false)

    override val subtractionUnlockedFlow = subtractionUnlocked
    override val divisionUnlockedFlow = divisionUnlocked
    override val multiplicationUnlockedFlow = multiplicationUnlocked
    override val allOperationsUnlockedAlreadyFlow = allOperationsUnlockedAlready
    override val answerAchievementUnlockedFlow = answerAchievementUnlocked
    override val sqrtUnlockedFlow = sqrtUnlocked
    override val percentUnlockedFlow = percentUnlocked

    override suspend fun setOperationUnlocked(operation: String, unlocked: Boolean) {
        when (operation) {
            "-" -> subtractionUnlocked.value = unlocked
            "/" -> divisionUnlocked.value = unlocked
            "*" -> multiplicationUnlocked.value = unlocked
            "√" -> sqrtUnlocked.value = unlocked
            "%" -> percentUnlocked.value = unlocked
        }
    }

    override suspend fun setAllOperationsAlreadyUnlocked(unlocked: Boolean) {
        allOperationsUnlockedAlready.value = unlocked
    }

    override suspend fun setAnswerAchievementUnlocked(unlocked: Boolean) {
        answerAchievementUnlocked.value = unlocked
    }

    override suspend fun resetOperations() {
        subtractionUnlocked.value = false
        divisionUnlocked.value = false
        multiplicationUnlocked.value = false
        allOperationsUnlockedAlready.value = false
        answerAchievementUnlocked.value = false
        sqrtUnlocked.value = false
        percentUnlocked.value = false
    }
}
