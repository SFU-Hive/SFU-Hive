package com.project362.sfuhive.Assignments

import com.project362.sfuhive.database.Assignment

class Course {
    private val id = 0L
    private val name = ""
    private val assignments = mutableListOf<Assignment>()

    fun getId(): Long {
        return id
    }

    fun getName(): String {
        return name
    }

    fun getAssignments(): List<Assignment> {
        return assignments
    }
}