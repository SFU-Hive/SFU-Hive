package com.project362.sfuhive.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

// adapted from RoomDatabase demo
@Entity(tableName = "assignment_table")
data class Assignment (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    // different from the database primary id, this is id from Canvas for this assignment
    @ColumnInfo(name = "assignment_id_column")
    var assignmentId: Long = 0L,

    @ColumnInfo(name = "course_name_column")
    var courseName: String = "",

    @ColumnInfo(name = "assignment_name_column")
    var assignmentName: String = "",

    @ColumnInfo(name = "due_at_column")
    var dueAt: String = "",

    @ColumnInfo(name = "points_possible_column")
    var pointsPossible: Double = 0.0,

    // need to get from students submissions which is a whole other thing
//    @ColumnInfo(name = "points_awarded_column")
//    var pointsAwarded: Double = 0.0,
)
