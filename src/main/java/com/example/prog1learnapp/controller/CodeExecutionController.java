package com.example.prog1learnapp.controller;

import com.example.prog1learnapp.dto.CodeRequest;
import com.example.prog1learnapp.dto.ExecutionResult;
import com.example.prog1learnapp.dto.ValidationResult;
import com.example.prog1learnapp.model.CodeExecution;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.User;
import com.example.prog1learnapp.repository.CodeExecutionRepository;
import com.example.prog1learnapp.repository.ExerciseRepository;
import com.example.prog1learnapp.repository.UserRepository;
import com.example.prog1learnapp.service.DockerExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/execution")
public class CodeExecutionController {
    private static final Logger log = LoggerFactory.getLogger(CodeExecutionController.class);
    
    private final CodeExecutionRepository codeExecutionRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final DockerExecutionService dockerExecutionService;
    
    public CodeExecutionController(CodeExecutionRepository codeExecutionRepository,
                                  UserRepository userRepository,
                                  ExerciseRepository exerciseRepository,
                                  DockerExecutionService dockerExecutionService) {
        this.codeExecutionRepository = codeExecutionRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.dockerExecutionService = dockerExecutionService;
    }
    
    @PostMapping("/run")
    public ResponseEntity<ExecutionResult> runCode(@RequestBody CodeRequest request, Principal principal) {
        User user = findUserByPrincipal(principal);
        Long userId = user != null ? user.getId() : null;
        
        // Create execution record (userId may be null for anonymous execution)
        CodeExecution execution = new CodeExecution(request.getExerciseId(), userId, request.getCode());
        execution.setStatus("PENDING");
        execution = codeExecutionRepository.save(execution);
        
        try {
            // Get exercise to retrieve test code
            Optional<Exercise> exerciseOpt = exerciseRepository.findById(request.getExerciseId());
            if (exerciseOpt.isEmpty()) {
                execution.setStatus("ERROR");
                execution.setError("Exercise not found");
                codeExecutionRepository.save(execution);
                return ResponseEntity.badRequest().body(createErrorResult("Exercise not found"));
            }
            
            Exercise exercise = exerciseOpt.get();
            String testCode = exercise.getTestCode() != null ? exercise.getTestCode() : "";
            
            // Execute code
            ExecutionResult result = dockerExecutionService.executeJavaCode(
                request.getCode(),
                testCode,
                 30000, // 30 second timeout
                512   // 512 MB memory limit
            );
            
            // Update execution record
            execution.setStatus(result.getStatus());
            execution.setOutput(result.getOutput());
            execution.setError(result.getError());
            execution.setTestPassed(result.isTestPassed());
            execution.setExecutionDuration(result.getExecutionDuration());
            execution.setExecutionTime(LocalDateTime.now());
            codeExecutionRepository.save(execution);
            
            // Set execution ID in result
            result.setExecutionId(execution.getId());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error executing code", e);
            execution.setStatus("ERROR");
            execution.setError("Internal server error: " + e.getMessage());
            codeExecutionRepository.save(execution);
            
            return ResponseEntity.internalServerError()
                    .body(createErrorResult("Internal server error"));
        }
    }
    
    @GetMapping("/status/{executionId}")
    public ResponseEntity<ExecutionResult> getStatus(@PathVariable Long executionId, Principal principal) {
        User user = findUserByPrincipal(principal);
        Long userId = user != null ? user.getId() : null;
        
        Optional<CodeExecution> executionOpt = codeExecutionRepository.findById(executionId);
        if (executionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CodeExecution execution = executionOpt.get();
        
        // Check authorization (user can only see their own executions)
        // Allow access if userId is null (anonymous) and execution has null userId, or if user matches
        if (execution.getUserId() == null) {
            // Anonymous execution - allow anyone to view (e.g., for testing)
        } else if (userId == null || !execution.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        ExecutionResult result = new ExecutionResult();
        result.setExecutionId(execution.getId());
        result.setStatus(execution.getStatus());
        result.setOutput(execution.getOutput());
        result.setError(execution.getError());
        result.setTestPassed(execution.isTestPassed());
        result.setExecutionDuration(execution.getExecutionDuration());
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/validate/{exerciseId}")
    public ResponseEntity<ValidationResult> validateSolution(@PathVariable Long exerciseId,
                                                           @RequestBody CodeRequest request,
                                                           Principal principal) {
        User user = findUserByPrincipal(principal);
        // Allow anonymous validation for testing
        
        Optional<Exercise> exerciseOpt = exerciseRepository.findById(exerciseId);
        if (exerciseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Exercise exercise = exerciseOpt.get();
        String testCode = exercise.getTestCode() != null ? exercise.getTestCode() : "";
        String validationCode = exercise.getValidationCode() != null ? exercise.getValidationCode() : "";
        
        // Combine test code and validation code
        String fullTestCode = testCode + "\n" + validationCode;
        
        // Execute validation
        ExecutionResult executionResult = dockerExecutionService.executeJavaCode(
            request.getCode(),
            fullTestCode,
             30000, // 30 second timeout
            512
        );
        
        ValidationResult validationResult = new ValidationResult();
        validationResult.setStatus(executionResult.getStatus());
        validationResult.setOutput(executionResult.getOutput());
        validationResult.setError(executionResult.getError());
        validationResult.setTestPassed(executionResult.isTestPassed());
        validationResult.setExecutionDuration(executionResult.getExecutionDuration());
        
        // Parse test results for details (simplistic)
        if (executionResult.getOutput() != null) {
            String output = executionResult.getOutput();
            if (output.contains("ALL_TESTS_PASSED")) {
                validationResult.setTestsPassed(1);
                validationResult.setTotalTests(1);
                validationResult.setTestDetails("All tests passed!");
            } else if (output.contains("TEST_FAILED")) {
                validationResult.setTestsPassed(0);
                validationResult.setTotalTests(1);
                validationResult.setTestDetails("Test failed: " + output);
            }
        }
        
        return ResponseEntity.ok(validationResult);
    }
    
    private User findUserByPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }
    
    private ExecutionResult createErrorResult(String error) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus("ERROR");
        result.setError(error);
        return result;
    }
}