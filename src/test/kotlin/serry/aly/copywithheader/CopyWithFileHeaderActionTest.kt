package serry.aly.copywithheader

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.datatransfer.DataFlavor

class CopyWithFileHeaderActionTest : BasePlatformTestCase() {

    fun `test wrapped content is copied to clipboard`() {
        val psiFile = myFixture.configureByText("Sample.kt", "fun main() {}\n")
        val editor = myFixture.editor
        val project = project

        val action: AnAction = CopyWithFileHeaderAction()

        val dataContext = DataContext { key ->
            when (key) {
                CommonDataKeys.PROJECT.name -> project
                CommonDataKeys.EDITOR.name -> editor
                CommonDataKeys.VIRTUAL_FILE.name -> psiFile.virtualFile
                else -> null
            }
        }

        val event = TestActionEvent(dataContext, action)

        action.actionPerformed(event)

        val clipboardText = CopyPasteManager.getInstance()
            .contents
            ?.getTransferData(DataFlavor.stringFlavor) as? String ?: ""

        assertTrue(clipboardText.contains("<file path="))
        assertTrue(clipboardText.contains("```kt"))
        assertTrue(clipboardText.contains("fun main() {}"))
        assertTrue(clipboardText.trim().endsWith("</file>"))
    }
}
