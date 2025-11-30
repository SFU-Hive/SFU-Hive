package com.project362.sfuhive.database.storage

import androidx.room.TypeConverter
import com.project362.sfuhive.storage.FileSource

class Converters {
    @TypeConverter
    fun fromSource(source: FileSource): String {
        return source.name
    }

    @TypeConverter
    fun toFileSource(name: String): FileSource {
        return FileSource.valueOf(name)
    }
}