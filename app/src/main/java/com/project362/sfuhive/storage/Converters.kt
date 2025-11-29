package com.project362.sfuhive.storage

import androidx.room.TypeConverter

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