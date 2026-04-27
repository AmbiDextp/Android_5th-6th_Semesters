package com.example.splitmate.model

data class SplitMateUIState(
    val curSum: String = "",
    val curPeople: String = "",
    val curTip: String = "10"
)

val Reseter = SplitMateUIState(
    curSum = "",
    curPeople = "",
    curTip = "10"
)