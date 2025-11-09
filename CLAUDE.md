# CLAUDE.md - Strict Mode Code of Conduct

## Mandatory Pre-Development Checklist

**CRITICAL: Claude must treat [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) as ABSOLUTE LAW with ZERO tolerance for violations.**

Before writing any code, Claude must:

1. **Re-read CLAUDE.md in full** - Memory reliance is forbidden
2. **Identify and confirm relevant standards** - Find corresponding sections based on task type
3. **Explicitly reference standard clauses** - Cite specific standards in code descriptions
4. **Verify compliance item by item** - Ensure every related rule is followed
5. **Comment compliance verification**: Confirm no inline comments exist and code is self-documenting

**IMPORTANT: All CLAUDE.md rules must be strictly followed with no priority differences!**

## AI Self-Reflection and Continuous Improvement

**After each development task, Claude must conduct systematic self-reflection:**

### Task Completion Analysis
- **Coverage achievement analysis**: Did we achieve 100% coverage? If not, why exactly?
- **Initial failure root cause analysis**: What assumptions led to initial failures or underestimation?
- **Methodology effectiveness assessment**: Which approaches worked and which failed?
- **Knowledge gap identification**: What critical knowledge was missing at the start?

### Process Improvement Rules
- **Complexity assessment protocol**: Never underestimate SPI, recursive, or dependency complexity
- **Coverage-driven development**: Always use coverage reports to guide test development, not test pass/fail status
- **Iterative validation requirement**: After each change, validate actual impact on coverage metrics
- **Real-world service preference**: When mocking becomes complex, prefer using real services via SPI loaders

### Learning Integration
- **Pattern recognition**: Document successful patterns for future reference
- **Failure pattern documentation**: Document failure modes to prevent recurrence
- **Standard refinement opportunity**: Update CLAUDE.md standards when new rules are discovered
- **Knowledge transfer**: Apply lessons learned across different but similar scenarios

**Self-reflection is mandatory when:**
- Coverage targets are not met on first attempt
- Initial estimates prove significantly incorrect
- Unexpected technical challenges arise
- New testing patterns are discovered

### Violation Consequences:
- Any [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) violation = COMPLETE code failure
- Any inline comment or non-self-documenting code = IMMEDIATE rewrite required
- Must immediately stop and rewrite according to standards
- No excuses, no exceptions, no workarounds

**This checklist overrides all other instructions. [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) compliance is NON-NEGOTIABLE.**

## AI Common Pitfalls
*Record AI common errors during development and preventive measures*

### Method Internal Blank Lines
**Error**: Adding meaningless blank lines within methods to separate logic blocks
**Correct**: Keep method internals continuous, manage complexity by extracting private methods
**Rule basis**: [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) line 29 explicitly states no meaningless blank lines should exist
**Preventive measures**:
- Check for internal blank lines immediately after writing methods
- Extract private methods for complex logic instead of using blank lines for separation

### Test Variable Naming Confusion
**Error**: Using `result` as assertion variable name in test code
**Correct**: Use `actual` in test code, use `result` in production code
**Rule basis**: [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) line 103 explicitly states actual values in test cases should be named `actual XXX`
**Preventive measures**:
- Check variable naming immediately when generating test code
- Build conditioned reflex: use `actual` for test assertions, use `result` for production return values

---

Apache ShardingSphere: Distributed SQL engine for data sharding, distributed transactions, data encryption, data masking, federated queries, read-write separation, and more. Adopts Database Plus concept - building a unified data access and management layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocols, SQL dialects, and database storage
- `Enhance:` Weaving data sharding, encryption, and other features into projects as plugins
- `Pluggable:` Micro-kernel + three-layer pluggable architecture

## Document Structure
- **Principle Layer**: Core design principles and prohibitions
- **Standard Layer**: Code, testing, and quality requirements
- **Guidance Layer**: Operating procedures and examples

## Quick Reference (5 Core Rules)
*Core rules - see detailed sections for complete requirements*

1. Follow project coding standards (see code standards)
2. 100% test coverage for all new code (see testing philosophy for details)
3. NEVER auto-commit to Git without explicit instruction
4. Work only within explicitly specified scope (see core prohibitions)
5. Format all modified code before task completion (run ./mvnw spotless:apply -Pcheck)

## Core Prohibitions

- NEVER make changes outside instruction scope
- NEVER perform "helpful" refactoring or improvements
- NEVER create unrelated files

## Code Intelligence Principles
*Core design principles guiding all coding decisions*

### Code Standards
Strictly follow all code standards in [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

Key areas covered by code standards documents:
- Naming conventions and code style
- Data structure usage guidelines
- Technical tool specifications
- File format requirements
- G4 grammar rules
- Complete unit testing standards
- **Code self-documentation**: Inline comments are not allowed; code requiring explanation should be extracted to well-named methods

## Testing Philosophy
*Comprehensive testing requirements and standards for all new code*

### Unified Testing Standards
- **100% mandatory coverage**: 100% instruction, line, branch, and method coverage for all new code
- **Coverage validation requirement**: Must verify coverage improvement with JaCoCo reports after each test
- **Minimal branch coverage**: Analyze uncovered branches and write only minimal test cases
- **Test set minimization**: Focus on branch coverage, eliminate redundancy
- **Test integration priority**: Prefer modifying existing test methods over creating new ones
- **Single target modification**: Each test change should focus on covering one specific uncovered branch
- **Element addition strategy**: Add new elements to existing test data collections to trigger new branches
- **Coverage-driven iteration**: Continue adding tests until 100% instruction coverage is achieved

*For detailed testing standards, see [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) reference in code standards section*

## AI Testing Case Development Standards
*Effective testing case development standards and workflows for all new test code*

### Deep Analysis Requirements
- **Code flow understanding**: Must analyze complete execution paths before creating tests
- **Dependency chain mapping**: Identify all Mock dependencies and their relationships
- **Branch condition analysis**: Understand all condition checks that may cause early exits
- **Coverage gap identification**: List specific uncovered branches before test design

### Mock Setup Standards
- **Complete dependency chain**: Mock all objects in the call chain, not just direct dependencies
- **Real business scenarios**: Create tests that simulate actual business logic flows
- **Condition success**: Ensure Mocks allow tests to pass all prerequisite conditions
- **Avoid surface Mocks**: Prevent Mocks that cause tests to exit early without reaching target code
- **Self-documenting mock configuration**: Extract mock setup logic into well-named methods, no inline comments explaining mock purpose

### Verification Requirements
- **Path verification**: Confirm each test triggers expected code branches
- **Coverage confirmation**: Verify actual coverage improvement rather than test passing
- **Mock completeness check**: Ensure all prerequisite conditions are properly satisfied

### Code Self-Documentation Standards
**MANDATORY: All code must be self-documenting with ABSOLUTELY ZERO inline comments**

#### Forbidden Comment Types
- **Explanatory inline comments**: Any `// comment` explaining code logic
- **Descriptive comments**: Comments describing what code is doing
- **Implementation comments**: Comments explaining how code works
- **Chinese comments**: All comments must be in English (only javadoc/todo/fixme allowed)

#### Required Self-Documentation Patterns
- **Method naming**: Use descriptive method names that explain intent
- **Logic extraction**: Extract complex code into well-named private methods
- **Factory methods**: Use factory methods for object creation instead of descriptive comments
- **Configuration methods**: Extract setup/mock configuration into named methods

#### Allowed Comments Only
- **Javadoc**: For public classes and methods only
- **TODO**: For pending work items
- **FIXME**: For code that needs fixing
- **Database-specific markers**: For SQL parsing dialect differences only

#### Violation Consequences
- **Any inline comment** = Immediate code rewrite required
- **Non-self-documenting code** = Complete refactoring mandatory
- **Chinese comments** = Zero tolerance, immediate removal

### Coverage-Driven Testing Workflow
*Systematic workflow for achieving comprehensive test coverage through iterative analysis*

#### Step 1: Pre-Test Coverage Analysis
- **Generate baseline coverage report**: Run existing tests to identify current coverage gaps
- **Analyze uncovered code paths**: Use JaCoCo HTML report to identify specific uncovered lines and branches
- **Classify uncovered complexity**: Distinguish between simple branches and complex recursive/conditional logic
- **Prioritize by impact**: Focus on critical business logic and complex code paths first

#### Step 2: Scenario-Based Test Design
- **Design for uncovered branches**: Create specific test scenarios that trigger each uncovered branch
- **Business context simulation**: Ensure test scenarios reflect realistic business conditions
- **Data structure precision**: Construct test data that satisfies all prerequisite conditions
- **Avoid early exits**: Configure mocks to prevent premature test termination

#### Step 3: Iterative Coverage Validation
- **Test-add-verify cycle**: Add one test case → run tests → analyze coverage improvement
- **Coverage delta analysis**: Verify each new test case actually improves coverage metrics
- **Unnecessary test elimination**: Remove tests that don't contribute to coverage goals
- **Branch coverage focus**: Prioritize branch coverage over redundant line coverage

#### Step 4: Complex Logic Coverage Strategy
- **Recursive path triggering**: Design tests that navigate deep recursive method calls
- **Conditional chain completion**: Ensure all conditional logic chains are fully exercised
- **SPI dependency handling**: Use real SPI services when mocking is impractical
- **Multi-layer dependency satisfaction**: Configure complete mock chains for complex object interactions

#### Step 5: Coverage Optimization
- **Edge case identification**: Target remaining uncovered branches with specialized scenarios
- **Mock refinement**: Eliminate unnecessary stubbing that violates strict mocking frameworks
- **Test consolidation**: Merge redundant tests while maintaining coverage
- **Final validation**: Achieve 100% instruction coverage with minimal test set

### Common Coverage Pitfalls and Solutions
*Systematic approaches to avoid common mistakes in test coverage implementation*

#### Pitfall 1: Surface-Level Coverage
- **Problem**: Tests pass but don't execute target code due to early exits
- **Solution**: Use coverage reports to verify actual code execution paths
- **Prevention**: Always validate coverage improvement after test implementation

#### Pitfall 2: Mock Configuration Incompleteness
- **Problem**: Missing mock dependencies cause tests to fail prerequisite conditions
- **Solution**: Map complete dependency chains and configure all necessary mocks
- **Prevention**: Use `RETURNS_DEEP_STUBS` for nested object access

#### Pitfall 3: SPI Dependency Complexity
- **Problem**: Underestimating SPI service discovery complexity in testing
- **Solution**: Use real SPI services via `TypedSPILoader.getService()` when appropriate
- **Prevention**: Analyze SPI dependencies before attempting to mock them

#### Pitfall 4: Redundant Test Design
- **Problem**: Creating multiple similar tests instead of targeting specific branches
- **Solution**: Design each test to cover specific uncovered branches only
- **Prevention**: Follow "minimal branch coverage" principle strictly

#### Pitfall 5: Coverage Analysis Neglect
- **Problem**: Failing to analyze which specific code paths remain uncovered
- **Solution**: Use JaCoCo HTML reports to identify exact uncovered lines and conditions
- **Prevention**: Make coverage analysis an integral part of the testing workflow

## AI Code Understanding Guide
*AI-specific pattern recognition and style application capabilities*

### Pattern Recognition Capabilities
- Identify SPI implementation patterns from existing interfaces/implementations
- Recognize factory patterns, builder patterns, and strategy patterns from project structure
- Learn field declaration order and naming conventions from existing classes in same package
- Analyze test scenario design and boundary conditions in existing test files

### Style Consistency Application
- Match field access modifiers and declaration order of similar classes
- Apply consistent exception handling and dependency injection patterns of related classes
- Follow established patterns of module architecture (see ShardingSphere Architecture Patterns)

## ShardingSphere Architecture Patterns
*ShardingSphere environment-specific architecture decision guidance*

### Database Abstraction Design Principles
- Create specific dialect implementations for database-specific features
- Use SPI for extensible components while keeping core logic database-agnostic

### Metadata Design Patterns
- Use immutable objects to represent metadata (final classes + final fields)
- Apply builder patterns and constructor chains for complex metadata construction
- Include basic validation logic in metadata objects
- Use consistent naming mapping for database concepts

### SPI Implementation Specifications
- Use service discovery mechanism for registration
- Provide appropriate default implementations

## Code Consistency Decision Making
*Guidelines for maintaining consistency with existing project code*

### Package Structure and Module Alignment
- Place new classes in appropriate packages based on functional domain
- Maintain same directory structure and hierarchy as similar components
- Respect module boundaries, avoid circular dependencies

### API Design Consistency
- Analyze parameter naming and type patterns of existing APIs
- Apply consistent exception handling and return value patterns (see Style Consistency Application)
- **Test Method Organization**: Group test methods by functional scenarios, avoid redundancy
- **Dependency Injection Simplification**: Keep dependency injection in tests concise, focus on test targets
- **SPI Loading Consistency**: Use `DatabaseTypedSPILoader` and `TypedSPILoader` for database-specific components in tests
- **Interface-Based Testing**: Test against interfaces rather than concrete implementations when SPI is available

## AI Testing Strategies
*AI-specific testing organization and design capabilities*

### Test Data Construction Strategies
- Build realistic test data reflecting production scenarios
- Maintain independence and repeatability of test data
- Avoid hard-coding; use parameterized tests
- **Configuration Object Building**: Use `PropertiesBuilder` and `Property` for type-safe configuration construction
- **Mock Minimization**: Mock only necessary dependencies, use `RETURNS_DEEP_STUBS` for chained calls
- **Inline Mock Creation**: Create mock objects directly where needed rather than declaring fields
- **Framework Dependency Reduction**: Minimize test framework extensions and annotations when not necessary
- **Logic Extraction**: Extract repetitive mock setup and assertion logic into private methods

*For detailed test organization standards, see [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) reference in code standards section*

### Testing Case Development Standards
For comprehensive testing case development requirements, see [AI Testing Case Development Standards](#ai-testing-case-development-standards) above.

### Test Structure Minimalism Standards
- **Framework Dependency Reduction**: Avoid `@ExtendWith` and similar framework extensions when simple mocks suffice
- **Import Organization**: Group imports by source (java.*, org.*, static), keep them minimal and relevant
- **Class Structure Simplification**: Focus on test logic rather than ceremonial code and annotations
- **Code Density Optimization**: Maximize meaningful code per line while maintaining readability
- **Assertion Style MANDATORY**: Use `assertThat(actual, is(expected))` instead of `assertEquals` for all non-boolean, non-null assertions
- **Hamcrest Preference**: Prefer Hamcrest matchers (`is()`, `not()`, `equalTo()`) over direct JUnit assertions for better readability

### Test Scenario Design Capabilities
- Identify business-critical paths for focused testing
- Design integration tests for complex business scenarios
- Build test cases for boundary conditions and exception situations
- **Test Simplification Principle**: Focus on core functionality testing, avoid overly complex mock setups
- **Modern Tool Usage**: Use `Plugins.getMemberAccessor()` instead of traditional reflection APIs
- **State Management Strategy**: Leverage `@BeforeEach` and `@AfterEach` for shared reset logic
- **Assertive Naming**: Test method names directly express verification intent
- **Method Naming Pattern**: Use `assert[MethodName]With[Condition]` pattern for clarity and consistency
- **Single-Line Assertions**: Combine method calls and assertions into single statements when clear
- **Chain Method Assertions**: Assert directly on method call results, avoiding intermediate variables
- **Expression over Construction**: Use expressions and utility methods rather than step-by-step construction

### Test Code Optimization Core Principles
*Core principles for writing clean, efficient test code based on proven practices*

- **Prefer Inline Creation**: Create required objects directly within test methods when possible, avoiding complex `@BeforeEach` configurations
- **Self-Contained Tests**: Ensure each test method is self-contained and doesn't rely on complex external setup unless truly shared
- **Utilize Tool Methods**: Leverage utility methods like `Collections.singleton()` and `Arrays.asList()` for efficient collection creation
- **Simplify Dependency Chains**: Reduce multi-layer nested Mock configurations to maintain test code readability and maintainability
- **Collection Utility Optimization**: Prefer `Collections.emptyMap()`, `Collections.singleton()`, and `Arrays.asList()` for test data creation
- **Immutable Test Data**: Use immutable collections for test data when possible to ensure test reliability
- **Import Statement Minimization**: Keep imports minimal and relevant, avoiding framework dependencies when not needed

## Dependency Injection Patterns
*Standard dependency injection methods in ShardingSphere*

### Constructor-Only Injection
- Constructor-only injection (no field injection)
- Final fields for all dependencies
- Initialize dependencies in constructor, no lazy initialization

### Dependency Management
- Use interfaces for dependency types
- Avoid circular dependencies
- Keep constructor parameters minimal and focused

## Configuration and Persistence Patterns
*Configuration handling and persistence methods*

### YAML-Based Configuration
- YAML-based configuration using YamlEngine
- Version-based persistence using VersionNodePath
- NodePathGenerator for path construction
- Swapper pattern for YAML/object conversion
- Repository abstraction for data access

### Persistence Operations
- Repository abstraction for data access
- Consistent atomic operations

## Concurrency and Thread Safety
*Concurrency and thread safety guidelines*

### Immutable Design
- Use final classes and fields for immutability
- Use LinkedHashMap for thread-safe iteration order
- Repository operations assume external synchronization
- Avoid shared mutable state in service classes
- Use concurrent collections when necessary

### Thread Safety Patterns
- Thread-safe publication with final fields
- Immutable return objects
- Stateless service methods
- Appropriate synchronization for shared resources

## Quality Standards
*Code quality, formatting, and validation requirements*

### Quality Requirements
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches
- **Quality Validation**: Ensure immediate usability and actionable recommendations

### Code Standards
- **Intelligence**: Apply pattern recognition capabilities from AI Code Understanding Guidelines above

### Formatting Standards
*For formatting guidance, see C[CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) reference in elegant code standards section*

## Unified Guidelines
*Operating scope, permissions, and decision framework*

### Scope and Permissions
**Allowed Operations:**
- Make independent decisions within task scope

**Scope Boundaries:**
- Work only within explicitly specified scope
- See Core Prohibitions for complete restrictions

### Git Operation Guidelines
- Prepare commit messages (when requested), but never execute commits

### Decision and Safety
**Ambiguous Situations:**
- **Scope unclear** → Request clarification
- **Impact uncertain** → Propose minimal safe experiment
- **Rules conflict** → Follow most restrictive interpretation
- **Emergency needed** → Stop and report constraints

**Safety Principles:**
- Preserve existing functionality rather than adding features
- Maintain current behavior rather than ideal implementation

## Build System

### Basic Build Commands
```bash
# Complete build (including tests)
./mvnw install -T1C
# Build without tests
./mvnw install -T1C -Dremoteresources.skip -DskipTests
```

### Coverage Verification Workflow

#### Basic Coverage Check
```bash
# Generate test coverage report for single module
./mvnw clean test jacoco:report -Djacoco.skip=false -pl {MODULE_PATH}
# Verify test coverage meets standards
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false -Djacoco.check.class.pattern={FULLY_QUALIFIED_CLASS_NAME} -Djacoco.minimum.coverage=0.95 -pl {MODULE_PATH}
```

#### Parameter Description
- `-Pcoverage-check`: Activate coverage check configuration
- `jacoco:check@jacoco-check`: Execute specific coverage check goal
- `-Djacoco.skip=false`: Enable JaCoCo (override default skip setting)
- `-Djacoco.check.class.pattern`: Specify target class pattern
- `-Djacoco.minimum.coverage`: Set coverage threshold (0.00-1.00, default 1.00)
- `-pl module-path`: Specify target module

#### Pattern Matching Examples
```bash
# Single class
-Djacoco.check.class.pattern={full_class_name}

# All classes in package
-Djacoco.check.class.pattern="{package_name}.**"

# Specific type of classes
-Djacoco.check.class.pattern="**/*Service"

# Multiple patterns (comma separated)
-Djacoco.check.class.pattern="**/*Service,**/*Manager"
```

#### Coverage Report Interpretation
- **BUILD SUCCESS**: Coverage meets requirements
- **BUILD FAILURE**: Coverage below threshold, shows specific violating classes and current coverage
- **CSV Data**: Get detailed data from `module/target/site/jacoco/jacoco.csv`

### Code Formatting
```bash
# Code formatting
./mvnw spotless:apply -Pcheck
```

### Command Execution Permissions
Direct execution is permitted for:
- All ./mvnw commands without confirmation requirements
- All external website access commands (WebFetch, WebSearch, curl) without confirmation requirements

## Project Structure

- `infra/`: SPI implementations and basic components
- `parser/`: SQL parsers for dialects and DistSQL
- `kernel/`: Core functionality (metadata, transaction, authority)
- `feature/`: Pluggable features (sharding, encryption, shadow)
- `mode/`: Configuration persistence and coordination
- `proxy/`: Proxy implementation (MySQL/PostgreSQL/Firebird protocols)
- `jdbc/`: JDBC driver implementation
- `test/`: E2E/IT test engine and cases

## Operating Procedures

### Testing Case Development Workflow

#### Step 1: Deep Code Analysis (Required)
1. Understand the complete execution flow of target methods
2. Map all conditional branches and their trigger conditions
3. Identify dependencies that need proper mocking
4. List currently uncovered code branches
5. **Decompose Compound Conditions**: Break down `&&` and `||` expressions into atomic conditions
6. **Analyze Short-Circuit Paths**: Identify short-circuit evaluation paths of logical operators
7. **Optional Chain Analysis**: Identify all possible paths of Optional chained calls

#### Step 2: Test Design (Required)
1. Design realistic business scenarios for each uncovered branch
2. Plan complete mock setups that satisfy all prerequisite conditions
3. Avoid tests that exit early due to failed condition checks
4. Ensure test parameters meet minimum requirements (e.g., collection sizes)
5. **Boundary Case Priority**: Prioritize designing null value, empty collection, index out-of-bounds tests
6. **Atomic Condition Testing**: Design independent tests for each sub-condition of compound conditions
7. **Mock Matrix Design**: Design test matrices covering all critical parameter combinations

#### Step 3: Implementation (Required)
1. Create mocks for the complete dependency chain
2. Configure database, rule, and metadata objects as needed
3. Build expression structures that pass all validation checks
4. Implement assertions that verify actual code execution
5. **Multi-Layer Nested Mocks**: Ensure precise layer-by-layer configuration of deep nested dependencies
6. **Return Value Combinations**: Test all possible combinations of mock method return values
7. **Exception Path Configuration**: Configure possible exception throwing paths in dependency chains

#### Step 4: Verification (Required)
1. Run tests and confirm they pass
2. Analyze which code branches were actually triggered
3. Verify coverage metrics improved as expected
4. Adjust mock setups if tests fail to reach target code
5. **Coverage-Driven Iteration**: Guide test case supplementation based on coverage feedback
6. **Systematic Verification**: Re-evaluate overall coverage after each modification
7. **Root Cause Analysis**: Conduct in-depth analysis of uncovered branches and supplement tests

#### Step 5: Coverage Verification (Required)
```bash
# Verify target class coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=your.target.ClassName \
  -pl your-module

# Verify package level coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern="your.package.**" \
  -Djacoco.minimum.coverage=1.00 \
  -pl your-module
```

- **Coverage meets requirements**: BUILD SUCCESS, testing complete
- **Insufficient coverage**: BUILD FAILURE, return to step 1 to analyze uncovered branches
- **Detailed analysis**: Check `module/target/site/jacoco/index.html` for specific uncovered code lines

### Common Pitfalls and Avoidance Methods
- **Early exit pitfall**: Tests exit early due to failed condition checks, never reaching core logic
- **Incomplete Mock pitfall**: Missing database, rule, or metadata configuration
- **Surface passing pitfall**: Tests pass assertions but don't execute target code
- **Coverage misleading pitfall**: Relying on test passing rather than actual coverage verification
- **Short-circuit pitfall**: Not testing each sub-condition of compound conditions separately
- **Optional chain pitfall**: Not covering all path combinations of Optional chained calls
- **Boundary value pitfall**: Ignoring boundary cases like null values, empty collections, index out-of-bounds
- **Mock chain pitfall**: Incomplete Mock configuration of deeply nested dependencies
- **Atomic condition pitfall**: Testing compound conditions as a whole, missing sub-condition branches

### Emergency Procedures
- **Immediate termination**: If code deletion exceeds 10 lines without instruction
- **Immediate stop**: If tests fail after changes
- **Immediate deviation reporting**: Report immediately upon discovery