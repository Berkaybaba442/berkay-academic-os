package org.example;

public class ScriptOptions {
    private String interpreter;
    private String workingDirectory;
    private boolean captureOutput;

    public ScriptOptions() {
    }

    public String getInterpreter() {
        return interpreter;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isCaptureOutput() {
        return captureOutput;
    }

    public void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setCaptureOutput(boolean captureOutput) {
        this.captureOutput = captureOutput;
    }
}