package com.example.prog1learnapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_executions")
public class CodeExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long exerciseId;
    private Long userId;
    
    @Lob
    private String userCode;
    
    private String status; // PENDING, RUNNING, SUCCESS, ERROR
    
    @Lob
    private String output;
    
    @Lob
    private String error;
    
    private boolean testPassed;
    
    private LocalDateTime executionTime;
    
    private Long executionDuration; // milliseconds
    
    // Default constructor
    public CodeExecution() {
        this.executionTime = LocalDateTime.now();
    }
    
    // Constructor for creating new execution
    public CodeExecution(Long exerciseId, Long userId, String userCode) {
        this();
        this.exerciseId = exerciseId;
        this.userId = userId;
        this.userCode = userCode;
        this.status = "PENDING";
        this.testPassed = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public boolean isTestPassed() { return testPassed; }
    public void setTestPassed(boolean testPassed) { this.testPassed = testPassed; }
    
    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
    
    public Long getExecutionDuration() { return executionDuration; }
    public void setExecutionDuration(Long executionDuration) { this.executionDuration = executionDuration; }
}