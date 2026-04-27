package com.example.splitmate.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.splitmate.model.Check
import com.example.splitmate.model.Reseter
import com.example.splitmate.model.SplitMateUIState
import java.time.LocalDate
import kotlin.Int

class SplitMateViewModel: ViewModel() {
    val checks: MutableList<Check> = mutableListOf()
    var uiState: SplitMateUIState by mutableStateOf(SplitMateUIState())

    val validSum: Boolean
        get() {
            return uiState.curSum.toIntOrNull() != null && uiState.curSum.toInt() > 0
        }

    val validPeople: Boolean
        get() {
            return uiState.curPeople.toIntOrNull() != null && uiState.curPeople.toInt() > 0
        }

    val validTip: Boolean
        get() {
            return uiState.curTip.toIntOrNull() != null && uiState.curTip.toInt() >= 0
        }

    fun tipAmount(sum: Int, tip: Int): Int {
        return sum/100 * tip
    }


    fun totalSum(sum: Int, tip: Int): Int {
        return sum + tipAmount(sum, tip)
    }

    fun perPerson(sum: Int, tip: Int, people: Int): Int {
        return totalSum(sum, tip) / people
    }

    fun changeSum(sum: String) {
        uiState = uiState.copy(
            curSum = sum
        )
    }

    fun changePeople(people: String) {
        uiState = uiState.copy(
            curPeople = people
        )
    }

    fun changeTip(tip: String) {
        uiState = uiState.copy(
            curTip = tip
        )
    }

    fun saveCheck(changeLast: Boolean) {
        val newCheck = Check(
            id = checks.size,
            sum = uiState.curSum.toInt(),
            people = uiState.curPeople.toInt(),
            tip = uiState.curTip.toInt(),
            date = LocalDate.now()
        )

        if (changeLast) checks[checks.size-1] = newCheck
        else checks.add(newCheck)

    }

    fun resetState() {
        uiState = Reseter
    }

}