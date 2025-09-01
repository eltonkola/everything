package com.eltonkola.everything.data.repository

interface FileSystemInterface {
    suspend fun listFiles(directoryPath: String): List<String>
    suspend fun readFile(filePath: String): String
    suspend fun fileExists(filePath: String): Boolean
    suspend fun getLastModified(filePath: String): Long
    suspend fun isDirectory(path: String): Boolean

    suspend fun writeFile(filePath: String, content: String)
    suspend fun createDirectory(path: String)
    suspend fun delete(filePath: String)
}