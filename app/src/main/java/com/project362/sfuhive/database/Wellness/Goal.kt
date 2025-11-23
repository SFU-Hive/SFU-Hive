package com.project362.sfuhive.database.Wellness

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project362.sfuhive.database.Badge.BadgeEntity

@Entity(
    tableName = "goal_table",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["badgeId"],
            childColumns = ["badge_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)

data class Goal (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "name_column")
    var goalName: String ="",

    @ColumnInfo(name = "completion_count_column")
    var completionCount: Int = 0,

    @ColumnInfo(name = "last_completion_date_column")
    var lastCompletionDate: Long, // use epoch millis

    @ColumnInfo(name = "badge_id", index = true)
    val badgeId: Long? = null, // FK

    @ColumnInfo(name = "nfc_tag_id_column")
    val string: String? = null,
)