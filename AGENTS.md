# AGENTS.md

This file contains guidelines for agentic coding agents working on this Spring Boot learning application.

## Project Overview

This is a Spring Boot 3.5.9 web application for programming education (Prog1 LearnApp). It uses:
- Java 17
- Spring Boot with Web, Security, Data JPA, Validation, Thymeleaf
- H2 database (dev) / PostgreSQL (prod)
- Maven build system

## Build and Test Commands

### Maven Commands
```bash
# Build the project
./mvnw clean install

# Run the application (development profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=TestClassName

# Run a single test method
./mvnw test -Dtest=TestClassName#testMethodName

# Skip tests during build
./mvnw clean install -DskipTests

# Generate test coverage report (if Jacoco is configured)
./mvnw jacoco:report
```

### Development Setup
Set VM options in IntelliJ: `-Dspring.profiles.active=dev`

### Database
For production PostgreSQL, use:
```bash
docker run --name lernapp-postgres \
  -e POSTGRES_DB=lernapp \
  -e POSTGRES_USER=lernapp \
  -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 \
  -d postgres:16
```

## Code Style Guidelines

### Package Structure
- Base package: `com.example.prog1learnapp`
- Controllers: `com.example.prog1learnapp.controller`
- Models/Entities: `com.example.prog1learnapp.model`
- Repositories: `com.example.prog1learnapp.repository`
- Configuration: `com.example.prog1learnapp.config`

### Import Order
1. `java.*` and `javax.*` imports
2. Third-party library imports (org.*, com.*)
3. Project-specific imports
4. Empty line between groups

Example:
```java
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.example.prog1learnapp.model.User;
```

### Naming Conventions
- **Classes**: PascalCase (`UserController`, `ExerciseRepository`)
- **Methods**: camelCase (`findUserByPrincipal`, `getNextExerciseId`)
- **Variables**: camelCase (`lessonProgress`, `completedExercises`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_SUCCESS_URL`)
- **Packages**: lowercase, dot-separated (`com.example.prog1learnapp`)

### Entity Annotations
Use JPA annotations consistently:
```java
@Entity
@Table(name = "table_name")
public class EntityName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String uniqueField;
    
    @Column(length = 2000)
    private String longTextField;
    
    @Lob
    private String largeTextField;
}
```

### Spring Annotations
- Controllers: `@Controller` for MVC, `@RestController` for API
- Services: `@Service` (if using service layer)
- Repositories: `@Repository` (automatically applied to interfaces extending JpaRepository)
- Configuration: `@Configuration`, `@Bean`
- Dependencies: `@Autowired` or constructor injection (preferred)

### Logging
- Use SLF4J Logger: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
- Log levels: `log.debug()` for detailed debugging, `log.info()` for important events, `log.warn()` for potential issues, `log.error()` for errors

### Exception Handling
- Use `@ControllerAdvice` with `@ExceptionHandler` for global exception handling
- Return appropriate HTTP status codes
- Log errors with context information

### Security Configuration
- Configure endpoints with appropriate authentication/authorization
- Use BCrypt for password encoding
- Configure CSRF protection for web forms
- Allow static resources access

### Database Configuration
- Development: H2 in-memory database with `ddl-auto: update`
- Production: PostgreSQL with appropriate connection settings
- Use `@Transactional` for service methods that modify data

### Frontend Templates
- Use Thymeleaf templates in `src/main/resources/templates/`
- Include base template with fragments for consistent layout
- Use Thymeleaf Security extras for user-specific content

### Testing
- Place tests in `src/test/java`
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests
- Mock dependencies when testing individual components

### Code Quality
- Avoid System.out.println() - use proper logging
- Use constructor injection over field injection
- Keep methods small and focused on single responsibility
- Use meaningful variable and method names
- Add Javadoc comments for public methods and complex logic

### Error Pages
- Create custom error pages in `templates/error/` (404.html, 500.html)
- Configure in `application.yaml`

### Static Resources
- CSS files in `src/main/resources/static/css/`
- JavaScript files in `src/main/resources/static/js/`
- Images in `src/main/resources/static/images/`

### Security Best Practices
- Never commit secrets or passwords
- Use environment variables for sensitive configuration
- Validate all user input
- Use HTTPS in production
- Implement proper session management