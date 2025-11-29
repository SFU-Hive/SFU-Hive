package com.project362.sfuhive.storage

sealed class FileData{
    abstract val id: String
    abstract val name: String
    abstract val type: String
    abstract val size: Long
    abstract val lastAccessed: Long
}

//User uploaded files
data class FileMetaData(
    override val id: String,
    override val name: String,
    override val type: String,
    override val size: Long,
    override val lastAccessed: Long,
    val uploadDate: Long

): FileData()

//Files accessed from the internet
data class FileMetaDataNetwork(
    override val id: String,
    override val name: String,
    override val type: String,
    override val size: Long,
    override val lastAccessed: Long,
    val url: String
): FileData()

//Firebase
data class FileMetaDataFirebase(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val size: Long = 0,
    val lastAccessed: Long = 0,
    val uploadDate: Long? = null,
    val url: String? = null,
    val source: FileSource = FileSource.USER_UPLOAD
)
