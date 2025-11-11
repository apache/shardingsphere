# CLAUDE.md - ShardingSphere AI Programming Guide

*Professional Guide for AI Programming Assistants - Best Practices for ShardingSphere Code Development*

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

#### 3. Key Points for Best Results
- **Provide complete context**: Tell me the specific class path, module information, and related dependencies
- **Clarify complexity**: If the class is particularly complex, specify which part to test first
- **Progressive development**: For complex features, request step-by-step implementation
- **Quality verification**: Ask me to run actual test commands to verify pass rates

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
./mvnw install -T1C                           # Full build
./mvnw install -T1C -DskipTests              # Build without tests
```

### Validation Commands
```bash
./mvnw spotless:apply -Pcheck                 # Format code
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=${ClassName} -pl ${submodule}  # Coverage check

# Parameter instructions:
# ${ClassName} - Specific class name for coverage check (e.g., ShardingRuleService)
# ${submodule} - Specific submodule name (e.g., shardingsphere-jdbc, shardingsphere-proxy)
```

### Troubleshooting Commands
```bash
./mvnw clean test jacoco:report -Djacoco.skip=false -pl ${submodule}  # Generate coverage report
open ${submodule}/target/site/jacoco/index.html                       # View coverage details
```

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

**Mock Usage Boundary Principles:**
- ✅ **No Mock Needed**: Simple objects (String, basic types, DTOs, POJOs), stateless utility classes, configuration objects
- ✅ **Mock Required**: Complex external dependencies (database connections, network services, file systems), third-party interfaces, SPI services, stateful objects
- ✅ **Judgment Criteria**: If object construction cost is high or has external dependencies, Mock is needed

```java
// Basic Mock configuration - for interface method Mock
when(dependency.${method}(any())).thenReturn(${result});

// Complex dependency Mock - for constructor objects that need Mock
try (MockedConstruction<${ClassName}> mocked = mockConstruction(${ClassName}.class, (mock, context) -> {
    ${DependencyChainSetup}
    when(mock.${getMethod}()).thenReturn(${mockResult});
})) {
    // Test code - code paths involving new ${ClassName}()
}

// Mock configuration example comparison:
// ❌ No Mock needed - simple objects
String result = "testValue";  // Direct creation
Map<String, Object> config = new HashMap<>();  // Direct creation

// ✅ Mock needed - complex dependencies
when(dataSource.getConnection()).thenReturn(mockConnection);  // External dependency
try (MockedConstruction<DatabaseMetaData> mocked = mockConstruction(DatabaseMetaData.class)) {
    // Constructor needs Mock case
}
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
1. **Analyze Test Scenarios** → Identify branches that need testing
2. **Mock Configuration** → Use Mock Configuration Template
3. **Write Tests** → Apply Test Method Template
4. **Verify Coverage** → Ensure complete branch coverage
5. **Assertion Validation** → Use correct assertion patterns

### Documentation Task Steps
1. **Content Review** → Check accuracy and formatting
2. **Link Validation** → Ensure all links are valid
3. **Format Check** → Unify markdown format
4. **Complete Validation** → Ensure documentation quality standards

## 📋 Project Constraint Rules

### YAML Format Constraint Configuration
```yaml
# Package naming conventions
package_naming:
  service: "org.apache.shardingsphere.{module}.service"
  spi: "org.apache.shardingsphere.{module}.spi"
  config: "org.apache.shardingsphere.{module}.config"
  util: "org.apache.shardingsphere.{module}.util"

# Class design rules
class_design:
  services:
    - final_class_with_final_fields
    - constructor_injection_only
    - use_requiredArgsConstructor
    - self_documenting_code_only

  naming_conventions:
    test_methods: "assert{MethodName}With{Condition}Expects{Result}"
    production_variables: "result"
    test_variables: "actual"
    private_methods: "descriptive_verb_noun"

  test_organization:
    - one_test_per_branch
    - branch_first_naming
    - minimal_test_count
    - test_isolation
    - try_with_resources_for_mocks

# Quality requirements
quality_requirements:
  test_coverage: "100%"
  code_formatting: "spotless_applied"
  documentation: "self_documenting_only"
  mock_strategy: "use_mockedconstruction_for_external_deps"

# Assertion standards
assertion_standards:
  preferred_style: "hamcrest_matchers"
  usage_pattern: "assertThat(actual, is(expected))"
  variable_naming: "use_actual_for_assertions"
```

### Code Pattern Examples
```java
// Self-documenting code pattern (must follow)
if (userIsAdminWithPermission()) {
    // Complex logic extracted to private method
}

private boolean userIsAdminWithPermission() {
    return user.isAdmin() && user.hasPermission();
}

// Standard test structure (must follow)
@Test
void assertMethodNameWithConditionExpectsResult() {
    // Given
    mockDependencyChain();

    // When
    MyResult actual = target.methodUnderTest(input);

    // Then
    assertThat(actual, is(expectedResult));
}
```

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

## 🛠️ Troubleshooting Guide

### Common Problem Diagnosis

#### Coverage Issues
```yaml
problem: "Coverage not met"
cause_check:
  - Mock configuration incomplete, some branches not executed
  - Tests exit early, not covering target code
  - Complex conditional statement branch testing missing

solution:
  1. Check Mock configuration, use MockedConstruction to control external dependencies
  2. View JaCoCo HTML report, locate red diamond marked uncovered branches
  3. Create dedicated test methods for each conditional branch

reference_commands:
  ./mvnw clean test jacoco:report -Djacoco.skip=false -pl ${submodule}
  open ${submodule}/target/site/jacoco/index.html
```

#### Compilation Errors
```yaml
problem: "Compilation failure"
cause_check:
  - Dependency version conflicts
  - Syntax errors
  - Package import errors

solution:
  1. Check dependency versions and compatibility
  2. Verify syntax correctness
  3. Confirm package paths and import statements

reference_commands:
  ./mvnw clean compile
  ./mvnw dependency:tree
```

#### Test Failures
```yaml
problem: "Test execution failure"
cause_check:
  - Mock configuration incorrect
  - Assertion logic errors
  - Test data construction problems

solution:
  1. Check Mock configuration, ensure complete dependency chain
  2. Verify assertion logic, use Hamcrest matchers
  3. Confirm test data validity

reference_template: "Code Templates.Test Method Template"
```

#### Mock Configuration Issues
```yaml
problem: "Mock configuration complex and difficult to manage"
cause_check:
  - Nested dependencies too deep
  - Constructor Mock missing
  - Static method call Mock inappropriate
  - Mock object selection inappropriate (over-Mocking simple objects)

solution:
  1. Identify Mock boundaries: direct creation for simple objects, Mock only for complex objects
  2. Use RETURNS_DEEP_STUBS to handle complex dependencies
  3. Use MockedConstruction for constructor calls
  4. Use MockedStatic for static method calls
  5. Reduce unnecessary Mock, improve test readability

mock_boundary_judgment:
  - No Mock needed: String, basic types, DTOs, POJOs, stateless utility classes
  - Must Mock: database connections, network services, file systems, third-party interfaces

reference_template: "Code Templates.Mock Configuration Template"
```

### Debugging Techniques

#### Coverage Debugging
1. **Generate detailed report**: `./mvnw clean test jacoco:report -Djacoco.skip=false -pl ${submodule}`
2. **View HTML report**: `open ${submodule}/target/site/jacoco/index.html`
3. **Locate uncovered lines**: Look for red-marked code lines
4. **Analyze branch conditions**: Identify unexecuted conditional statement branches

#### Mock Debugging
1. **Verify Mock calls**: `verify(mock).method(params)`
2. **Check Mock state**: Confirm Mock configuration is correct
3. **Debug dependency chain**: Verify Mock configuration layer by layer

---

## 📋 Quick Checklist

### Pre-Task Check
- [ ] Clarify task type (source code/test/documentation)
- [ ] Understand quality requirements (100% coverage/formatting/self-documenting)
- [ ] Find relevant templates and constraint rules

### Pre-Completion Check
- [ ] Source code task: 100% test coverage
- [ ] Source code task: Code formatting
- [ ] Source code task: Self-documenting code
- [ ] Test task: Complete branch coverage
- [ ] Documentation task: Link validity
- [ ] All tasks: Conform to project constraint rules

### Final Verification
- [ ] Run full build: `./mvnw install -T1C`
- [ ] Verify coverage: `./mvnw test jacoco:check@jacoco-check -Pcoverage-check`
- [ ] Check formatting: `./mvnw spotless:apply -Pcheck`