package com.boolerules.prl.plugin

import com.booleworks.prl.compiler.PrlCompiler
import com.booleworks.prl.parser.PrlParser
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.antlr.v4.runtime.CharStreams

class PrlCompilerAnnotator : ExternalAnnotator<PrlCompilerAnnotatorInitialInfo, PrlCompilerAnnotatorResult>(), DumbAware {

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): PrlCompilerAnnotatorInitialInfo? {
        // save file or use psi file content
        return if (hasErrors) null else PrlCompilerAnnotatorInitialInfo(file)
    }

    override fun doAnnotate(collectedInfo: PrlCompilerAnnotatorInitialInfo?): PrlCompilerAnnotatorResult? {
        if (collectedInfo == null) {
            return null
        }
        val parsed = PrlParser.prepareParser(CharStreams.fromString(collectedInfo.file.text)).ruleFile("MOCK")
        val compiler = PrlCompiler()
        compiler.compile(parsed)
        val warnings = compiler.warnings().associate { extractLineNumber(it) to extractMessage(it) }
        val errors = compiler.errors().associate { extractLineNumber(it) to extractMessage(it) }
        return PrlCompilerAnnotatorResult(collectedInfo.file, warnings, errors)
    }

    override fun apply(file: PsiFile, annotationResult: PrlCompilerAnnotatorResult?, holder: AnnotationHolder) {
        annotationResult?.apply {
            warnings.forEach {
                holder.newAnnotation(HighlightSeverity.WARNING, it.value).range(elementAtLineNumber(file, it.key)).create()
            }
            errors.forEach {
                holder.newAnnotation(HighlightSeverity.ERROR, it.value).range(elementAtLineNumber(file, it.key)).create()
            }
        }
    }
}

data class PrlCompilerAnnotatorInitialInfo(
    val file: PsiFile,
)

data class PrlCompilerAnnotatorResult(
    val file: PsiFile,
    val warnings: Map<Int, String>,
    val errors: Map<Int, String>
)

private val lineNumberRegex = ".*lineNumber=([0-9]+)].*".toRegex()

fun extractLineNumber(compilerMessage: String): Int = lineNumberRegex.matchAt(compilerMessage, 0)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: run {
    println("Could not extract line number from message: $compilerMessage")
    1
}

fun extractMessage(compilerMessage: String): String = compilerMessage.substringAfter(']')

fun elementAtLineNumber(file: PsiFile, line: Int): TextRange =
    FileDocumentManager.getInstance().getDocument(file.virtualFile)
        ?.let { TextRange(it.getLineStartOffset(line - 1), it.getLineEndOffset(line - 1)) } ?: TextRange(0, 1)
