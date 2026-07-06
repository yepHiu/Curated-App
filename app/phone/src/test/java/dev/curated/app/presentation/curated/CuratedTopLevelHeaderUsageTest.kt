package dev.curated.app.presentation.curated

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedTopLevelHeaderUsageTest {
    @Test
    fun myActorsAndHistoryUseSharedTopHeader() {
        listOf(
                "CuratedMyScreen.kt",
                "CuratedActorsScreen.kt",
                "CuratedHistoryScreen.kt",
            )
            .forEach { fileName ->
                val source =
                    projectFile("src/main/java/dev/curated/app/presentation/curated/$fileName")
                        .readText()

                assertTrue("$fileName should use shared top header", source.contains("CuratedPageHeader("))
            }
    }

    private fun projectFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/phone", relativePath))
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find $relativePath from ${File(".").absoluteFile.normalize().path}")
    }
}
