package com.eltonkola.everything.data.repository

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.SYSTEM

class OkioFileSystemWrapper(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) :  FileSystemInterface {

    override suspend fun listFiles(directoryPath: String): List<String> = withContext(Dispatchers.Default) {
        try {
            val path = directoryPath.toPath()
            if (!fileSystem.exists(path)) return@withContext emptyList()

            fileSystem.list(path).map { it.toString() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun readFile(filePath: String): String = withContext(Dispatchers.Default) {
        val path = filePath.toPath()
        fileSystem.read(path) {
            readUtf8()
        }
    }

    override suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.Default) {
        fileSystem.exists(filePath.toPath())
    }

    override suspend fun getLastModified(filePath: String): Long = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            val metadata = fileSystem.metadataOrNull(path)
            metadata?.lastModifiedAtMillis ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun isDirectory(path: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val pathObj = path.toPath()
            val metadata = fileSystem.metadataOrNull(pathObj)
            metadata?.isDirectory == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isFile(path: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val pathObj = path.toPath()
            val metadata = fileSystem.metadataOrNull(pathObj)
            metadata?.isRegularFile == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun writeFile(filePath: String, content: String): Unit = withContext(Dispatchers.Default) {
        val path = filePath.toPath()
        fileSystem.write(path) {
            writeUtf8(content)
        }
    }

    override suspend fun createDirectory(path: String) = withContext(Dispatchers.Default) {
        val pathObj = path.toPath()
        fileSystem.createDirectories(pathObj)
    }

    override suspend fun delete(filePath: String) = withContext(Dispatchers.Default) {
        val path = filePath.toPath()
        if (fileSystem.exists(path)) {
            fileSystem.delete(path)
        }
    }

    suspend fun createDirectories(path: String) = withContext(Dispatchers.Default) {
        val pathObj = path.toPath()
        fileSystem.createDirectories(pathObj)
    }

}