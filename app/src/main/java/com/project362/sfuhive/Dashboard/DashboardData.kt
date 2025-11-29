package com.project362.sfuhive.Dashboard

data class ImportantDate(
    val name: String,
    val date: String,
    val task: String,
    val isComplete: Boolean
)

data class RecentFile(
    val fileName: String,
    val date: String,
    val size: String
)

data class StreakInfo(
    val date: String,
    val isComplete: Boolean
)