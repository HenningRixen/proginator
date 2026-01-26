package com.example.prog1learnapp.dto;

public class ExecutionResult {
    private String output;
    private String error;
    private String status; // PENDING, RUNNING, SUCCESS, ERROR
    private boolean testPassed;
    private Long executionDuration; // milliseconds
    private Long executionId;
    
    // Getters and Setters
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public boolean isTestPassed() { return testPassed; }
    public void setTestPassed(boolean testPassed) { this.testPassed = testPassed; }
    
    public Long getExecutionDuration() { return executionDuration; }
    public void setExecutionDuration(Long executionDuration) { this.executionDuration = executionDuration; }
    
    public Long getExecutionId() { return executionId; }
    public void setExecutionId(Long executionId) { this.executionId = executionId; }
}