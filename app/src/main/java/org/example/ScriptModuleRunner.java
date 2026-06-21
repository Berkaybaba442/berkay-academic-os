package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptModuleRunner {

    public String run(ModuleItem module, Path projectRoot) throws Exception {
        if (module.getTarget() == null || module.getTarget().isBlank()) {
            throw new IllegalArgumentException("Script hedefi tanımlanmamış");
        }

        Path scriptPath = projectRoot.resolve(module.getTarget()).normalize();

        if (!Files.exists(scriptPath)) {
            throw new IllegalArgumentException("Script bulunamadı: " + scriptPath);
        }

        String interpreter = "python3";
        if (module.getScriptOptions() != null
                && module.getScriptOptions().getInterpreter() != null
                && !module.getScriptOptions().getInterpreter().isBlank()) {
            interpreter = module.getScriptOptions().getInterpreter();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(interpreter, scriptPath.toString());
        processBuilder.directory(projectRoot.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Script hata ile bitti:\n" + output);
        }

        return output.toString().isBlank() ? module.getMessage() : output.toString();
    }
}