# CLAUDE.md - ShardingSphere AI Programming Guide

*Professional Guide for AI Programming Assistants - Best Practices for ShardingSphere Code Development*

## 🏗️ ShardingSphere Architecture Overview

### Project Overview
ShardingSphere is an ecosystem of distributed database solutions with JDBC driver, database proxy, and planned Sidecar modes.

### Core Module Architecture
```yaml
module_hierarchy:
  infrastructure_layer:
    - shardingsphere-infra: Common utilities, SPI definitions
    - shardingsphere-parser: SQL parsing (ANTLR4-based)
  engine_layer:
    - shardingsphere-mode: Configuration management
    - shardingsphere-kernel: Core execution engine
  access_layer:
    - shardingsphere-jdbc: Java JDBC driver
    - shardingsphere-proxy: Database proxy
  feature_layer:
    - shardingsphere-sharding: Data sharding
    - shardingsphere-encryption: Data encryption
    - shardingsphere-readwrite-splitting: Read/write splitting
```

### Technology Stack Decisions
- **ANTLR4**: SQL parsing and abstract syntax tree generation
- **Netty**: High-performance network communication (proxy mode)
- **Apache Calcite**: Query optimization and execution plans
- **SPI**: Plugin architecture for hot-pluggable extensions

### JDBC vs Proxy Patterns
- **JDBC**: Zero invasion, Java-only, highest performance
- **Proxy**: Language-agnostic, centralized management, advanced features

### Key Concepts
- **Sharding**: Horizontal data partitioning
- **DistSQL**: Distributed SQL for dynamic configuration
- **SPI Extension**: Algorithm, protocol, and execution extensions
- **Data Pipeline**: Migration and synchronization functionality

### Code Quality Standards
```yaml
self_documenting_code:
  method_naming: "10-15 characters, verb-noun patterns, no comments needed"
  examples: ["isValidEmailAddress()", "calculateOrderTotal()"]
  anti_examples: ["proc()", "getData()", "handle()"]

complex_logic:
  definition: "3+ nested levels or 20+ lines per method"
  handling: "Extract to meaningful private methods"

mock_boundaries:
  no_mock: "Simple objects, DTOs, stateless utilities"
  must_mock: "Database connections, network services, third-party interfaces"
  judgment: "Mock only with external dependencies or high construction cost"
```

## 🚀 AI Programming Best Practices

### How to Obtain High-Quality Code

#### 1. Source Code Development Request Template
```
Please implement [feature description] for [class name], requirements:
1. Follow ShardingSphere project coding standards and constraints
2. Use self-documenting programming, no comments
3. Extract complex logic into private methods
4. 100% test coverage unit tests
5. Pass spotless code formatting checks
6. Use @RequiredArgsConstructor constructor injection
```

#### 2. Unit Test Request Templates

**Basic Style-Consistent Testing**:
```
Please write unit tests for [class name], requirements:
1. Use ShardingSphere project testing style
2. Test method naming with assert*() prefix
3. Use Hamcrest assertion style assertThat(actual, is(expected))
4. Use Mockito for Mocking, follow project boundary principles
5. Maintain clear Given-When-Then structure
```

**Complex Tests for First-Pass Success**:
```
Please write complete unit tests for [complex class name], requirements:
1. First analyze the dependency relationships and complexity of the class under test
2. Identify all external dependencies that need Mocking
3. Gradually build test fixtures, ensure complete Mock chains
4. Write corresponding test methods for each branch
5. Use @BeforeEach to set up common Mocks
6. Use try-with-resources to manage MockedConstruction
7. Ensure all tests can run independently and pass

If you encounter uncertain dependency relationships, please ask me for confirmation.
```

**100% Coverage Testing**:
```
Please implement 100% test coverage for [specific class name] in shardingsphere-[module] module:
1. First generate coverage report to check current status:
   ./mvnw clean test jacoco:report -Djacoco.skip=false -pl [submodule]
   open [submodule]/target/site/jacoco/index.html
2. Identify all branches that need testing (red diamond markers)
3. Write multiple sets of test data for complex conditions
4. Ensure all exception paths have tests
5. Verify final coverage reaches 100%:
   ./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.check.class.pattern=[ClassName] -pl [submodule]

If dead code or uncovered branches are found, please explain in detail.
```

**Special Case Handling Testing**:
```
Please write unit tests for [class name], requirements:
1. Make every effort to achieve 100% coverage
2. If you encounter the following situations, please report to me:
   - Truly unreachable dead code (e.g., never-thrown exceptions)
   - Functions dependent on specific runtime environments (e.g., OS-specific functions)
   - Features requiring special hardware or network conditions
   - Protective programming code for extreme cases

Report format:
- Code location: [class name:line number]
- Uncoverage reason: [detailed explanation]
- Suggested solution: [if any]

Let me confirm before skipping coverage requirements for these codes.
```

**SQL Generation Class Testing**:
```
Please write comprehensive tests for [SQLGeneratorClass] in [module], requirements:
1. Use efficient test development workflow:
   - Analyze existing test Mock patterns first (DatabaseTypedSPILoader + TypedSPILoader)
   - Write most complex scenario test first to verify feasibility
   - Run test to get actual SQL output, then correct expected values
   - Batch copy verified patterns to other simple scenarios

2. SQL syntax verification:
   - For Oracle: verify MERGE INTO, ROWNUM, NVL syntax formats
   - For MySQL: verify LIMIT, IFNULL, REPLACE syntax formats
   - Use database-specific official docs for syntax validation

3. Mock dependency reuse:
   - 100% reuse existing test Mock configuration patterns
   - Avoid redesigning Mock chains for SPI loaders
   - Use DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType) pattern

4. Branch coverage strategy:
   - Merge simple methods into single tests where possible
   - Focus independent tests on truly complex conditional branches
   - Ensure each boolean/enum branch has at least one dedicated test

5. Quality assurance:
   - Run complete test suite in one batch after all tests written
   - Use Jacoco to verify 100% branch coverage
   - Minimize iterative modifications during development
```

#### 3. Key Points for Best Results
- **Provide complete context**: Tell me the specific class path, module information, and related dependencies
- **Clarify complexity**: If the class is particularly complex, specify which part to test first
- **Progressive development**: For complex features, request step-by-step implementation
- **Quality verification**: Ask me to run actual test commands to verify pass rates

#### 4. Critical Success Factors for Testing Tasks
- **Pattern Analysis First**: Always analyze existing test patterns before writing new tests
- **Validation-Driven Development**: Write tests to discover actual behavior before defining expectations
- **Mock Reuse Principle**: Never redesign Mock configurations when existing patterns work
- **Branch Efficiency**: Focus on conditional branches, not method count, for test coverage
- **Batch Validation**: Run complete test suites at once, avoid frequent incremental checks

## 🤖 AI Usage Guidelines

### Core Principles
- **AI-First Principle**: All content is oriented towards AI programming assistants, not human developers
- **Actionability Priority**: Every instruction must be directly executable by AI, avoid theoretical descriptions
- **Unambiguous Expression**: Use clear instructions and parameterized templates, avoid vague expressions
- **Search Efficiency Priority**: Information organization facilitates AI quick positioning, reduces understanding cost

### Content Standards
- **Accuracy Priority**: All information must be correct and verifiable, no exaggeration or beautification
- **Practicality Priority**: Focus on actual effects, avoid exaggerated expressions
- **Problem-Oriented**: Directly address problem essence, provide actionable solutions
- **Concise and Clear**: Use most direct language to express core information

### Response Style Standards
- **In-depth Analysis**: Conduct deep analysis based on code logic and project specifications, avoid surface-level answers
- **Factual and Realistic**: Present facts and data, do not exaggerate achievements, do not hide problems
- **Reasoned Debate**: When facing programmer's misconceptions, engage in well-reasoned debates based on technical standards and best practices
- **Timely Correction**: When facing AI inference errors, immediately admit mistakes and correct them to maintain technical accuracy

### Quick Search Mapping
- "Create rule change processor" → Code Templates.Rule Change Processor Template
- "Write test methods" → Code Templates.Test Method Template
- "Mock external dependencies" → Code Templates.Mock Configuration Template
- "Coverage check" → Quick Commands Reference.Validation Commands
- "Format code" → Quick Commands Reference.Validation Commands
- "Test style requirements" → AI Programming Best Practices.Unit Test Request Templates

### AI Decision Rules
```yaml
task_type_detection:
  if contains(["src/main/java"], ["*.java"]): "source_code_task"
  if contains(["src/test/java"], ["*Test.java"]): "test_task"
  if contains(["*.md"], ["docs/"]): "documentation_task"

validation_rules:
  source_code_task: ["100% test coverage", "code formatting", "self-documenting"]
  test_task: ["branch coverage", "mock configuration", "assertion correctness"]
  doc_task: ["link validity", "format consistency"]

decision_logic:
  if task_type == "source_code": apply source code task workflow
  if task_type == "test": apply test task workflow
  if involves_external_dependencies: use MockedConstruction template
```

## ⚡ Quick Commands Reference

### Build Commands
```bash
./mvnw install -T1C                           # Full build with parallel execution
./mvnw install -T1C -DskipTests              # Build without tests
./mvnw clean compile                          # Compile only
```

### Code Quality Commands
```bash
./mvnw spotless:apply -Pcheck                 # Format code
./mvnw checkstyle:check                      # Code style checking
./mvnw pmd:check                             # Static code analysis
./mvnw spotbugs:check                        # Bug detection
./mvnw dependency-check                      # Security vulnerability scan
./mvnw archunit:test                         # Architecture rule validation
```

### Testing Commands
```bash
./mvnw test                                  # Run all tests
./mvnw test -Dtest=${TestClassName}          # Run specific test class
./mvnw test -pl ${submodule}                 # Run tests for specific module
./mvnw test jacoco:report -Djacoco.skip=false -pl ${submodule}  # Generate coverage report
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=${ClassName} -pl ${submodule}  # Coverage check

# Performance Testing
./mvnw jmh:benchmark                         # Run performance benchmarks
```

# Parameters: ${ClassName}, ${TestClassName}, ${submodule}

## 📝 Code Templates

### Rule Change Processor Template
```java
package org.apache.shardingsphere.${module}.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.processor.AlgorithmChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.${module}.api.config.${RuleType}RuleConfiguration;
import org.apache.shardingsphere.${module}.rule.${RuleType}Rule;

import java.util.Map;

/**
 * ${AlgorithmType} algorithm changed processor.
 */
public final class ${AlgorithmType}AlgorithmChangedProcessor extends AlgorithmChangedProcessor<${RuleType}RuleConfiguration> {

    public ${AlgorithmType}AlgorithmChangedProcessor() {
        super(${RuleType}Rule.class);
    }

    @Override
    protected ${RuleType}RuleConfiguration createEmptyRuleConfiguration() {
        return new ${RuleType}RuleConfiguration();
    }

    @Override
    protected Map<String, AlgorithmConfiguration> getAlgorithmConfigurations(final ${RuleType}RuleConfiguration currentRuleConfig) {
        return currentRuleConfig.get${AlgorithmType}Algorithms();
    }

    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("${ruleType}", "${algorithmType}_algorithms");
    }
}
```

### Test Method Template
```java
@Test
void assert${MethodName}With${Condition}Expects${Result}() {
    // Given
    ${MockSetup}

    // When
    ${ActualCall}

    // Then
    assertThat(${actual}, is(${expected}));
}
```

### Mock Configuration Template

**Mock Usage Boundaries:**
- **No Mock**: Simple objects, DTOs, stateless utilities, configuration objects
- **Must Mock**: Database connections, network services, third-party interfaces, SPI services
- **Judgment**: Mock only with external dependencies or high construction cost

**Basic Mock Patterns:**
```java
// Interface method Mock
when(dependency.method(any())).thenReturn(result);

// Constructor Mock with MockedConstruction
try (MockedConstruction<ClassName> mocked = mockConstruction(ClassName.class)) {
    // Test code involving new ClassName()
}
```

**Advanced Mock Patterns:**
```java
// Static method Mocking (avoid UnfinishedStubbingException)
@SneakyThrows(SQLException.class)
private static Array createMockArray(final Object data) {
    Array result = mock(Array.class);
    doReturn(data).when(result).getArray();
    return result;
}

// Deep stubs for complex dependencies
@Mock(answer = Answers.RETURNS_DEEP_STUBS)
private ComplexService complexService;

// MockedStatic for static method calls
try (MockedStatic<UtilityClass> mocked = mockStatic(UtilityClass.class)) {
    when(UtilityClass.staticMethod(any())).thenReturn(value);
    // Test code
}
```

**Example Comparison:**
```java
// ❌ Over-mocking simple objects
String result = mock(String.class);  // Unnecessary

// ✅ Direct creation for simple objects
String result = "testValue";

// ✅ Mock external dependencies
when(dataSource.getConnection()).thenReturn(mockConnection);
```

### SPI Implementation Template
```java
package org.apache.shardingsphere.${module}.spi;

@TypedSPI
public final class ${SPIName}Impl implements ${SPIName}SPI {
    @Override
    public ${ResultType} execute(${ContextType} context) {
        // Implementation logic
        return ${result};
    }

    @Override
    public String getType() {
        return "${type}";
    }
}
```

## 🧪 ShardingSphere Testing Style Guide

### Project Testing Style Summary

#### 1. Naming Conventions
- **Test Classes**: `*Test.java` suffix
- **Test Methods**: `assert*()` prefix with descriptive naming
  - Examples: `assertConnectWithInvalidURL()`, `assertDriverWorks()`, `assertLoadEmptyConfiguration()`
- **Integration Tests**: `*IT.java` suffix

#### 2. Mock Usage Patterns
- **Framework**: Mockito + Mockito Extension
- **Annotations**: `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`
- **Deep Stubs**: `@Mock(answer = Answers.RETURNS_DEEP_STUBS)`
- **Constructor Mocks**: `MockedConstruction` for complex objects
- **Boundary Principle**: Direct creation for simple objects, Mock only complex dependencies

#### 3. Assertion Styles
- **Primary**: Hamcrest matchers
- **Pattern**: `assertThat(actual, is(expected))`
- **Custom**: `ShardingSphereAssertionMatchers.deepEqual()` for deep equality comparisons

#### 4. Test Structure
- **Single Responsibility**: Each test method focuses on one scenario
- **Given-When-Then**: Clear three-part structure
- **Independence**: Complete isolation between tests
- **Resource Management**: `try-with-resources` for Mock resource management

#### 5. Coverage Requirements
- **Target**: 100% branch coverage
- **Focus**: Algorithm execution paths, boundary conditions, exception handling
- **Method**: Independent testing of each conditional branch

### Module-Specific Testing Patterns

#### JDBC Module Testing
- **Driver Testing**: JDBC driver registration and functionality
- **Connection Testing**: Connection pooling and state management
- **Adapter Testing**: JDBC adapter implementations

#### Proxy Module Testing
- **Configuration Testing**: Proxy configuration loading
- **Protocol Testing**: Database protocol implementations
- **Handler Testing**: Request/response handlers

#### Kernel Module Testing
- **Algorithm Testing**: Core algorithms (sharding, encryption, etc.)
- **Rule Testing**: Business rule implementations
- **Pipeline Testing**: Data pipeline operations

### Advanced Testing Patterns

#### Concurrency Testing
- **Multi-threaded Tests**: Thread safety validation
- **Async Testing**: Asynchronous operation testing with Awaitility
- **Race Condition Testing**: Concurrent access scenarios

#### Integration Testing Patterns
- **YAML Integration**: Configuration serialization/deserialization
- **SPI Integration**: Service provider interface testing
- **Database Integration**: Mocked database interactions for metadata testing

## 🎯 Task Execution Workflow

### Source Code Task Steps
1. **Analyze Task** → Identify as source code task
2. **Coverage Analysis** → Use JaCoCo to find uncovered branches
3. **Design Implementation** → Apply templates from Code Templates
4. **Verify Coverage** → Run tests to ensure 100% coverage
5. **Format Code** → Apply spotless formatting
6. **Complete Validation** → Ensure all quality checks pass

### Test Task Steps

#### Standard Testing Workflow
1. **Analyze Test Scenarios** → Identify branches that need testing
2. **Mock Configuration** → Use Mock Configuration Template
3. **Write Tests** → Apply Test Method Template
4. **Verify Coverage** → Ensure complete branch coverage
5. **Assertion Validation** → Use correct assertion patterns

#### SQL Generation Class Workflow (High-Efficiency)
**Phase 1: Comprehensive Analysis (One-time)**
```
Task agent analysis should include:
- Complete class structure and method listing
- Complexity and branch count for each method
- All existing test patterns and configurations
- Dependency relationships and Mock requirements
- Identification of any non-coverable code
```

**Phase 2: Validation-First Development**
1. **Select most complex scenario** and write one test first
2. **Verify Mock configuration and syntax expectations** by running the test
3. **Batch copy verified patterns** to other simple scenarios
4. **Write dedicated tests only for truly complex conditional branches**

**Phase 3: Quality Assurance**
- Run complete test suite and coverage checks in one batch
- Avoid frequent iterative modifications
- Use Jacoco to verify 100% branch coverage
```

### Documentation Task Steps
1. **Content Review** → Check accuracy and formatting
2. **Link Validation** → Ensure all links are valid
3. **Format Check** → Unify markdown format
4. **Complete Validation** → Ensure documentation quality standards

## 📋 Project Constraint Rules

### Core Design Principles
```yaml
class_design:
  - final classes with final fields
  - constructor injection only
  - @RequiredArgsConstructor for dependencies
  - self-documenting code (no comments)

package_structure:
  service: "org.apache.shardingsphere.{module}.service"
  spi: "org.apache.shardingsphere.{module}.spi"
  config: "org.apache.shardingsphere.{module}.config"
  util: "org.apache.shardingsphere.{module}.util"
```

### Code Patterns
```java
// Self-documenting pattern
if (isValidUserWithPermission()) {
    processPayment();
}

private boolean isValidUserWithPermission() {
    return user.isValid() && user.hasPermission();
}

// Test structure
@Test
void assertMethodWithConditionExpectsResult() {
    // Given
    mockDependencies();

    // When
    Result actual = target.method(input);

    // Then
    assertThat(actual, is(expected));
}
```

### Quality Requirements
- **Test Coverage**: 100% branch coverage
- **Code Formatting**: Spotless applied
- **Mock Strategy**: Mock only external dependencies
- **Naming**: Test methods use assert*() prefix

## 🔍 Quick Search Index

### AI Search Mapping Table
```yaml
quick_search_index:
  "Create rule change processor":
    target: "Code Templates.Rule Change Processor Template"
    description: "Create rule change processor class"

  "Write test methods":
    target: "Code Templates.Test Method Template"
    description: "Write unit test methods"

  "Mock external dependencies":
    target: "Code Templates.Mock Configuration Template"
    description: "Configure external dependency Mock"

  "Coverage check":
    target: "Quick Commands Reference.Validation Commands"
    description: "Run test coverage check"

  "Format code":
    target: "Quick Commands Reference.Validation Commands"
    description: "Apply code formatting"

  "Test style requirements":
    target: "AI Programming Best Practices.Unit Test Request Templates"
    description: "View testing style requirements"

  "Naming rules":
    target: "Project Constraint Rules.class_design.naming_conventions"
    description: "View naming conventions"

  "Package structure":
    target: "Project Constraint Rules.package_naming"
    description: "View package naming rules"

  "Quality issues":
    target: "Troubleshooting Guide"
    description: "Solve common quality issues"

  "ShardingSphere test style":
    target: "ShardingSphere Testing Style Guide"
    description: "Complete project testing style guide"

error_recovery_index:
  "Coverage not met":
    solution: "Check Mock configuration, add branch tests"
    reference: "Code Templates.Mock Configuration Template"

  "Compilation errors":
    solution: "Check dependencies and syntax"
    reference: "Quick Commands Reference.Build Commands"

  "Format errors":
    solution: "Run spotless formatting"
    reference: "Quick Commands Reference.Validation Commands"

  "Test failures":
    solution: "Check Mock configuration and assertion logic"
    reference: "Code Templates.Test Method Template"

  "Complex mock setup":
    solution: "Use Mock boundary judgment and complex dependency handling"
    reference: "ShardingSphere Testing Style Guide.Mock Usage Patterns"
```

## 🛠️ Common Issues & Solutions

### SQL Generation Class Issues

#### Expected SQL Syntax Errors
- **Issue**: Expected SQL assertion doesn't match actual generated SQL
- **Solution**: Run test first to get actual output, then correct expected values
- **Prevention**: Use database official documentation to verify syntax formats
- **Oracle Common Issues**: MERGE INTO ON clause format, ROWNUM positioning, NVL parameter order

#### Mock Configuration Complexity
- **Issue**: Over-engineering Mock configurations for SPI loaders
- **Solution**: 100% reuse existing test Mock patterns instead of redesigning
- **Pattern**: Use `DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType)` + `TypedSPILoader.getService(DatabaseType.class, "Oracle")` combination
- **Prevention**: Analyze existing tests completely before writing new ones

#### Branch Coverage Strategy Inefficiency
- **Issue**: Writing too many granular tests for simple methods
- **Solution**: Merge simple method tests, focus independent tests on complex branches only
- **Guideline**: One test per conditional branch, not one test per method
- **Example**: Test all simple SQL formatting methods in one focused test

### Coverage Problems
- **Issue**: Mock configuration incomplete, branches not executed
- **Solution**: Use MockedConstruction, create dedicated test methods for each branch
- **Command**: `./mvnw clean test jacoco:report -pl ${submodule}`

### Mock Configuration Errors
- **Issue**: UnfinishedStubbingException in static methods
- **Solution**: Use `doReturn().when()` instead of `when().thenReturn()`
- **Pattern**: `@SneakyThrows(SQLException.class) private static Array createMockArray()`

### Test Failures
- **Issue**: Mock dependency chain broken
- **Solution**: Verify complete dependency chain, use RETURNS_DEEP_STUBS
- **Check**: Mock calls with `verify(mock).method(params)`

### Compilation Errors
- **Issue**: Dependency conflicts, syntax errors
- **Solution**: Check versions, verify imports, run `./mvnw dependency:tree`

### Quick Reference
```bash
# Generate coverage report
./mvnw clean test jacoco:report -Djacoco.skip=false -pl ${submodule}

# View coverage details
open ${submodule}/target/site/jacoco/index.html

# Check dependencies
./mvnw dependency:tree
```

---

## 📋 Quality Checklist

### Before Starting
- [ ] Task type identified (source/test/docs)
- [ ] Quality requirements understood
- [ ] Relevant templates found

### Before Completing
- [ ] Source: 100% coverage + formatting + self-documenting
- [ ] Test: Complete branch coverage
- [ ] Docs: Valid links + consistent format
- [ ] All: Project constraints satisfied

### Final Verification
- [ ] Build: `./mvnw install -T1C`
- [ ] Coverage: `./mvnw test jacoco:check@jacoco-check -Pcoverage-check`
- [ ] Format: `./mvnw spotless:apply -Pcheck`