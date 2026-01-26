package com.example.prog1learnapp.dto;

public class ValidationResult extends ExecutionResult {
    private int testsPassed;
    private int totalTests;
    private String testDetails;
    
    // Getters and Setters
    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int testsPassed) { this.testsPassed = testsPassed; }
    
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    
    public String getTestDetails() { return testDetails; }
    public void setTestDetails(String testDetails) { this.testDetails = testDetails; }
}