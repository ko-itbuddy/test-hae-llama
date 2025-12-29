package com.example.llama

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

object LlamaPluginUtils {

    fun getPythonScriptPath(): String? {
        val resourceUrl = this::class.java.classLoader.getResource("python-core/src/main.py")
        return resourceUrl?.path
    }

    private fun getVenvPythonPath(): String {
        val home = System.getProperty("user.home")
        val venvDir = File(home, ".test-hea-llama/venv")
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        return if (isWindows) {
            File(venvDir, "Scripts/python.exe").absolutePath
        } else {
            File(venvDir, "bin/python").absolutePath
        }
    }

    private fun ensureUvEnvironment(project: Project) {
        val pythonPath = getVenvPythonPath()
        if (File(pythonPath).exists()) return

        ProgressManager.getInstance().run(object : Task.Modal(project, "🦙 Building Llama Environment with uv...", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val home = System.getProperty("user.home")
                val venvDir = File(home, ".test-hea-llama/venv")
                venvDir.parentFile.mkdirs()

                try {
                    indicator.text = "Checking uv engine..."
                    val uvCheck = ProcessBuilder("uv", "--version").start().waitFor()
                    if (uvCheck != 0) throw Exception("Please install uv: curl -LsSf https://astral.sh/uv/install.sh | sh")

                    indicator.text = "🏗️ Creating venv with Python 3.13 (via uv)..."
                    ProcessBuilder("uv", "venv", venvDir.absolutePath, "--python", "3.13").start().waitFor()

                    indicator.text = "🚀 Fast-installing dependencies..."
                    val resourceReq = this::class.java.classLoader.getResource("python-core/requirements.txt")
                    if (resourceReq != null) {
                        val reqFile = File(resourceReq.path)
                        ProcessBuilder("uv", "pip", "install", "--python", pythonPath, "-r", reqFile.absolutePath).start().waitFor()
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "🦙 Llama Setup Error: ${e.message}", "Error")
                    }
                }
            }
        })
    }

    fun runPythonCommand(project: Project, command: String, args: List<String>) {
        ensureUvEnvironment(project)
        
        val scriptPath = getPythonScriptPath()
        if (scriptPath == null || !File(scriptPath).exists()) {
            Messages.showErrorDialog(project, "Could not find bundled python-core/src/main.py", "Error")
            return
        }

        val pythonExe = getVenvPythonPath()
        val projectPath = project.basePath ?: return

        val cmd = mutableListOf(pythonExe, scriptPath, command)
        cmd.addAll(args)
        cmd.add("--project-path")
        cmd.add(projectPath)

        val scriptDir = File(scriptPath).parentFile.parent
        val pb = ProcessBuilder(cmd)
        pb.directory(File(projectPath))
        pb.environment()["PYTHONPATH"] = scriptDir

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "🦙 Llama is working...", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val process = pb.start()
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                    
                    val output = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.append(line).append("\n")
                    }
                    
                    val errorOutput = StringBuilder()
                    while (errorReader.readLine().also { line = it } != null) {
                        errorOutput.append(line).append("\n")
                    }
                    
                    process.waitFor() 
                    
                    ApplicationManager.getApplication().invokeLater {
                        if (process.exitValue() == 0) {
                            if (command == "generate") {
                                val code = extractCode(output.toString())
                                val file = ScratchRootType.getInstance().createScratchFile(
                                    project, "GeneratedTest.java", JavaLanguage.INSTANCE, code
                                )
                                if (file != null) {
                                    FileEditorManager.getInstance(project).openFile(file, true)
                                }
                            } else {
                                Messages.showInfoMessage(project, "Llama finished the task!", "Success")
                            }
                        } else {
                            Messages.showErrorDialog(project, "Llama encountered an error:\n$errorOutput", "Error")
                        }
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Failed to run Llama: ${e.message}", "Error")
                    }
                }
            }
        })
    }

    private fun extractCode(output: String): String {
        val regex = """<JAVA CODE>(.*?)<\/JAVA CODE>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(output)
        if (match != null) return match.groupValues[1].trim()
        
        val splitMarker = "Generated Code:"
        if (output.contains(splitMarker)) {
            val parts = output.split(splitMarker)
            return parts.last().replace("={10,}".toRegex(), "").trim()
        }
        return output.trim()
    }
}