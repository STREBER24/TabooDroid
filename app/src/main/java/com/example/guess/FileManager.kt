package com.example.guess

import android.content.res.AssetManager

private const val TAG = "FileManager"

class FileInfo(val filename: String, val title: String, val language: String)
class GuessTask(guessWord: String, blockedWords: List<String>) {
    val guessWord = guessWord.uppercase()
    val blockedWords = blockedWords.map { a -> a.uppercase() }

    fun toList(): List<String> {
        return listOf(guessWord) + blockedWords
    }
}

class FileManager(private val assets: AssetManager) {
    private val folder = "taskFiles"
    fun getAllFilesInfo(): List<FileInfo> {
        val allFilenames = assets.list(folder)
        if (allFilenames.isNullOrEmpty()) return emptyList()
        return allFilenames.map {
            parseHeader(it, assets.open("${folder}/${it}").bufferedReader().readLine())
        }
    }

    fun getTaskFile(filename: String): Pair<List<GuessTask>, FileInfo> {
        Log().i(TAG, "loading file '${filename}' ...")
        val reader = assets.open("${folder}/${filename}").bufferedReader()
        val header = parseHeader(filename, reader.readLine())
        return reader.lineSequence().filter { it.isNotBlank() }.map { line ->
            val words = line.split(',', ignoreCase = false).filter { it != "" }
            GuessTask(words[0], words.subList(1, words.size))
        }.toList() to header
    }

    private fun parseHeader(filename: String, header: String): FileInfo {
        val formattedHeader = header.split(":", ignoreCase = false)
        if (formattedHeader.size < 2) {
            Log().w(TAG, "failed to parse header '${header}'")
            return FileInfo(filename, "", "")
        }
        return FileInfo(filename, formattedHeader[0], formattedHeader[1])
    }
}