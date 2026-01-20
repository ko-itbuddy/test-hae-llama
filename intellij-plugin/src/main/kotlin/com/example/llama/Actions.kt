package com.example.llama

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class IngestAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        LlamaPluginUtils.runPythonCommand(project, "ingest", emptyList())
    }
}

class GenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        
        if (virtualFile.extension != "java") {
            // Show error
            return
        }
        
        LlamaPluginUtils.runPythonCommand(project, "generate", listOf("--target-file", virtualFile.path))
    }
}
