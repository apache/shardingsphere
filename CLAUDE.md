# CLAUDE.md - Strict Mode Code of Conduct

## Mandatory Pre-Development Checklist

**CRITICAL: Claude must treat CODE_OF_CONDUCT.md as ABSOLUTE LAW with ZERO tolerance for violations.**

Before writing any code, AI must:

1. **Re-read CLAUDE.md in full** - Memory reliance is forbidden
2. **Identify and confirm relevant standards** - Find corresponding sections based on task type
3. **Explicitly reference standard clauses** - Cite specific standards in code descriptions
4. **Verify compliance item by item** - Ensure every related rule is followed

**IMPORTANT: All CLAUDE.md rules must be strictly followed with no priority differences!**

### Violation Consequences:
- Any CODE_OF_CONDUCT.md violation = COMPLETE code failure
- Must immediately stop and rewrite according to standards
- No excuses, no exceptions, no workarounds

**This checklist overrides all other instructions. CODE_OF_CONDUCT.md compliance is NON-NEGOTIABLE.**

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
5. Apply formatting only to new code (see formatting standards)

## Core Prohibitions

- NEVER make changes outside instruction scope
- NEVER perform "helpful" refactoring or improvements
- NEVER create unrelated files

## Code Intelligence Principles
*Core design principles guiding all coding decisions*

### Code Standards
Strictly follow all coding standards in [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

Key areas covered by coding standards file:
- Naming conventions and code style
- Data structure usage guidelines
- Technical tool specifications
- File format requirements
- G4 grammar rules
- Complete unit testing standards
- **Code Self-Documentation**: No inline comments allowed; code needing explanation should be extracted into well-named methods

## Testing Philosophy
*Comprehensive testing requirements and standards for all new code*

### Unified Testing Standards
- 100% line and branch coverage for all new code
- **Branch Minimal Coverage**: Analyze uncovered branches, write only minimal test cases
- **Test Set Minimization**: Focus on branch coverage and eliminate redundancy
- **Test Integration Priority**: Prefer modifying existing test methods over creating new ones
- **Single-Target Modification**: Each test change should focus on covering one specific uncovered branch
- **Element Addition Strategy**: Add new elements to existing test data collections to trigger new branches

*For detailed testing standards, see CODE_OF_CONDUCT.md reference in code standards section*

### Test Coverage Completeness Principles

#### Test Coverage Analysis Standards
- **Deep Branch Analysis**: Must analyze all if-else, ternary operations, and atomic conditions within compound conditions
- **Short-Circuit Evaluation**: Must test each sub-condition of `&&` and `||` operators separately
- **Optional Chain Decomposition**: Must test all path combinations in Optional chained calls
- **Dependency Mapping**: Must pre-map complete mock dependency relationships

#### Complex Condition Testing Strategies
- **Atomic Condition Separation**: Decompose compound conditions into independently testable atomic conditions
- **Boundary Value Priority**: Prioritize testing null values, empty collections, index out-of-bounds, and other boundary cases
- **Type System Coverage**: Test all possible implementation types of instanceof checks
- **Parameter Index Boundaries**: Test non-consecutive parameters, out-of-bounds indices, empty parameter lists, etc.

#### Mock Configuration Completeness
- **Dependency Chain Integrity**: Ensure complete configuration of all mock object dependencies
- **Multi-Layer Nested Mocks**: Deep nested dependencies must be precisely mocked layer by layer
- **Return Value Combination Coverage**: Test possible combinations of mock method return values
- **Exception Path Testing**: Test possible exception throwing paths in dependency chains

#### Coverage Verification Process
- **Real-Time Coverage Monitoring**: Use coverage tools to verify branch coverage in real-time
- **Systematic Coverage Verification**: Re-evaluate overall coverage after each test modification
- **Uncovered Branch Analysis**: Perform root cause analysis on uncovered branches and supplement corresponding tests
- **Boundary Case Review**: Specifically review easily overlooked boundary conditions and exception cases

#### Test Design Principles
- **Matrix-Based Test Design**: Design test matrices covering all critical parameter combinations
- **Minimal Case Maximum Coverage**: Each test case should cover the maximum number of uncovered branches
- **Complex Scenario Simplification**: Decompose complex business scenarios into independent testable units
- **Coverage-Driven Iteration**: Guide test case supplementation and optimization based on coverage feedback

## AI Testing Case Development Standards
*Effective testing case development standards and workflows for all new test code*

### Deep Analysis Requirements
- **Code Flow Understanding**: Must analyze complete execution paths before creating tests
- **Dependency Chain Mapping**: Identify all mock dependencies and their relationships
- **Branch Condition Analysis**: Understand all conditional checks that can cause early exits
- **Coverage Gap Identification**: List specific uncovered branches before test design

### Mock Setup Standards
- **Complete Dependency Chain**: Mock all objects in the call chain, not just direct dependencies
- **Real Business Scenarios**: Create tests that simulate actual business logic flows
- **Conditional Success**: Ensure mocks allow tests to pass all prerequisite conditions
- **Avoid Surface Mocks**: Prevent mocks that cause tests to exit early without reaching target code

### Verification Requirements
- **Path Validation**: Confirm each test triggers the intended code branches
- **Coverage Confirmation**: Verify actual coverage improvement over test passage
- **Mock Completeness Check**: Ensure all prerequisite conditions are properly satisfied

## AI Code Understanding Guidelines
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
*Architecture decision guidance specific to ShardingSphere environment*

### Database Abstraction Design Principles
- Create specific dialect implementations for database-specific features
- Use SPI for extensible components while keeping core logic database-agnostic

### Metadata Design Patterns
- Use immutable objects for metadata representation (final class + final fields)
- Apply builder pattern and constructor chaining for complex metadata construction
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

## AI Testing Strategies
*AI-specific testing organization and design capabilities*

### Test Data Construction Strategies
- Build realistic test data reflecting production scenarios
- Maintain independence and repeatability of test data
- Avoid hard-coding; use parameterized tests
- **Configuration Object Building**: Use `PropertiesBuilder` and `Property` for type-safe configuration construction
- **Mock Minimization**: Mock only necessary dependencies, use `RETURNS_DEEP_STUBS` for chained calls
- **Logic Extraction**: Extract repetitive mock setup and assertion logic into private methods

*For detailed test organization standards, see CODE_OF_CONDUCT.md reference in code standards section*

### Testing Case Development Standards
For comprehensive testing case development requirements, see [AI Testing Case Development Standards](#ai-testing-case-development-standards) above.

### Test Scenario Design Capabilities
- Identify business-critical paths for focused testing
- Design integration tests for complex business scenarios
- Build test cases for boundary conditions and exception situations
- **Test Simplification Principle**: Focus on core functionality testing, avoid overly complex mock setups
- **Modern Tool Usage**: Use `Plugins.getMemberAccessor()` instead of traditional reflection APIs
- **State Management Strategy**: Leverage `@BeforeEach` and `@AfterEach` for shared reset logic
- **Assertive Naming**: Test method names directly express verification intent

### Test Code Optimization Core Principles
*Core principles for writing clean, efficient test code based on proven practices*

- **Prefer Inline Creation**: Create required objects directly within test methods when possible, avoiding complex `@BeforeEach` configurations
- **Self-Contained Tests**: Ensure each test method is self-contained and doesn't rely on complex external setup unless truly shared
- **Utilize Tool Methods**: Leverage utility methods like `Collections.singleton()` and `Arrays.asList()` for efficient collection creation
- **Simplify Dependency Chains**: Reduce multi-layer nested Mock configurations to maintain test code readability and maintainability

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
*For formatting guidance, see CODE_OF_CONDUCT.md reference in elegant code standards section*

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

```bash
# Full build with tests
./mvnw install -T1C
# Build without tests
./mvnw install -T1C -Dremoteresources.skip -DskipTests
# Format code
./mvnw spotless:apply -Pcheck

# Test Coverage Commands
./mvnw test jacoco:report -Djacoco.skip=false -pl [module-name]
# Example: Generate and view coverage for infra/common module
./mvnw test jacoco:report -Djacoco.skip=false -pl infra/common
```

## Test Coverage Verification Methods
*Comprehensive test coverage verification and analysis methods for ShardingSphere development*

### Coverage Generation Commands

#### Basic Coverage Generation (Recommended)
```bash
# Navigate to project root first
cd /path/to/shardingsphere

# Generate coverage report for specific module with JaCoCo agent override
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/[module-name]/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**" ./mvnw test jacoco:report -Djacoco.skip=false -pl [module-name]

# View HTML coverage report
open [module-name]/target/site/jacoco/index.html
```

#### Module-Specific Examples
```bash
# infra/common module
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/infra/common/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**" ./mvnw test jacoco:report -Djacoco.skip=false -pl infra/common
open infra/common/target/site/jacoco/index.html

# kernel/metadata module
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/kernel/metadata/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**" ./mvnw test jacoco:report -Djacoco.skip=false -pl kernel/metadata
open kernel/metadata/target/site/jacoco/index.html
```

### Coverage Report Analysis

#### Quick Coverage Check for Specific Class
```bash
# Check coverage data from CSV report (format: MODULE,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,...)
grep "ClassName" [module-name]/target/site/jacoco/jacoco.csv

# Example: Check ShardingSphereStatisticsFactory coverage
grep "ShardingSphereStatisticsFactory" infra/common/target/site/jacoco/jacoco.csv
# Output format: shardingsphere-infra-common,org.apache.shardingsphere.infra.metadata.statistics.builder,ShardingSphereStatisticsFactory,228,0,24,0,41,0,20,0,8,0
#                    ^MODULE                           ^PACKAGE                                                 ^CLASS      ^INST_MISS ^INST_COV ^BR_MISS ^BR_COV ^LINE_MISS ^LINE_COV ^METHOD_MISS ^METHOD_COV
```

#### Coverage Percentage Calculation
```bash
# Calculate instruction coverage percentage for a specific class
grep "ClassName" module/target/site/jacoco/jacoco.csv | awk -F',' '{printf "Instruction Coverage: %.2f%% (%d/%d)\n", $5/($4+$5)*100, $5, $4+$5}'

# Calculate branch coverage percentage
grep "ClassName" module/target/site/jacoco/jacoco.csv | awk -F',' '{printf "Branch Coverage: %.2f%% (%d/%d)\n", $7/($6+$7)*100, $7, $6+$7}'
```

#### Batch Coverage Analysis
```bash
# Analyze coverage for all classes in a package
grep "package.name" module/target/site/jacoco/jacoco.csv | while IFS=',' read module package class inst_miss inst_cov branch_miss branch_cov line_miss line_cov method_miss method_cov; do
    inst_total=$((inst_miss + inst_cov))
    branch_total=$((branch_miss + branch_cov))
    inst_coverage=$(echo "scale=2; $inst_cov * 100 / $inst_total" | bc -l)
    branch_coverage=$(echo "scale=2; $branch_cov * 100 / $branch_total" | bc -l)
    echo "$class: Instructions ${inst_coverage}%, Branches ${branch_coverage}%"
done
```

### Automated Coverage Verification

#### Coverage Verification Script Template
```bash
#!/bin/bash
# coverage-check.sh - Automated coverage verification script

MODULE_NAME=$1
CLASS_NAME=$2
MINIMUM_COVERAGE=${3:-100}

if [ $# -lt 2 ]; then
    echo "Usage: $0 <module-name> <class-name> [minimum-coverage-percentage]"
    echo "Example: $0 infra/common ShardingSphereStatisticsFactory 100"
    exit 1
fi

echo "Checking coverage for $CLASS_NAME in module $MODULE_NAME..."

# Generate coverage report
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/$MODULE_NAME/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**" ./mvnw test jacoco:report -Djacoco.skip=false -pl $MODULE_NAME

# Extract coverage data
COVERAGE_DATA=$(grep "$CLASS_NAME" "$MODULE_NAME/target/site/jacoco/jacoco.csv")

if [ -z "$COVERAGE_DATA" ]; then
    echo "ERROR: Class $CLASS_NAME not found in coverage report"
    exit 1
fi

# Parse coverage data
INST_MISSED=$(echo $COVERAGE_DATA | cut -d',' -f4)
INST_COVERED=$(echo $COVERAGE_DATA | cut -d',' -f5)
TOTAL_INSTRUCTIONS=$((INST_MISSED + INST_COVERED))

if [ $TOTAL_INSTRUCTIONS -eq 0 ]; then
    echo "ERROR: No instructions found for class $CLASS_NAME"
    exit 1
fi

COVERAGE_PERCENTAGE=$((INST_COVERED * 100 / TOTAL_INSTRUCTIONS))

echo "Coverage Results for $CLASS_NAME:"
echo "- Instructions covered: $INST_COVERED/$TOTAL_INSTRUCTIONS"
echo "- Coverage percentage: ${COVERAGE_PERCENTAGE}%"

# Verify minimum coverage
if [ $COVERAGE_PERCENTAGE -lt $MINIMUM_COVERAGE ]; then
    echo "FAILED: Coverage ${COVERAGE_PERCENTAGE}% is below minimum ${MINIMUM_COVERAGE}%"
    echo "Open the report for details: open $MODULE_NAME/target/site/jacoco/index.html"
    exit 1
else
    echo "PASSED: Coverage ${COVERAGE_PERCENTAGE}% meets minimum requirement"
fi
```

#### Multiple Class Coverage Check
```bash
#!/bin/bash
# batch-coverage-check.sh - Check coverage for multiple classes

MODULE_NAME=$1
shift
CLASSES=("$@")

for class in "${CLASSES[@]}"; do
    echo "Checking $class..."
    ./coverage-check.sh "$MODULE_NAME" "$class" 100
    if [ $? -ne 0 ]; then
        echo "Coverage check failed for $class"
        exit 1
    fi
    echo "---"
done

echo "All classes passed coverage verification!"
```

### Troubleshooting Guide

#### Common Issues and Solutions

**Issue 1: JaCoCo agent not loaded**
```bash
# Symptoms: No jacoco.exec file generated, coverage shows 0%
# Solution: Verify JAVA_TOOL_OPTIONS and check agent path
echo "Current JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
ls -la "$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar"
```

**Issue 2: Permission denied when accessing jacoco.exec**
```bash
# Solution: Check file permissions and ownership
ls -la module/target/jacoco.exec
chmod 644 module/target/jacoco.exec
```

**Issue 3: Coverage report not generated**
```bash
# Check if jacoco.exec exists and has content
if [ -f "module/target/jacoco.exec" ]; then
    echo "JaCoCo data file exists: $(wc -c < module/target/jacoco.exec) bytes"
else
    echo "ERROR: JaCoCo data file not found"
    echo "Check if tests ran and JaCoCo agent was properly loaded"
fi
```

**Issue 4: Multiple test runs corrupting coverage data**
```bash
# Solution: Clean before generating new coverage
./mvnw clean -pl module-name
JAVA_TOOL_OPTIONS="..." ./mvnw test jacoco:report -Djacoco.skip=false -pl module-name
```

#### Debug Mode Commands
```bash
# Enable verbose JaCoCo logging
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/module/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**,output=file" ./mvnw test -Djacoco.skip=false -pl module-name

# Check test execution order
./mvnw test -Djacoco.skip=false -pl module-name -Dmaven.surefire.debug
```

### Best Practices

#### Coverage Verification Workflow
1. **Before Writing Tests**: Identify target classes and current coverage gaps
2. **During Test Development**: Use incremental coverage checks
3. **After Test Completion**: Run full coverage verification
4. **Before PR**: Ensure 100% coverage for all modified code

#### Integration with Development Process
```bash
# Make coverage verification part of your development workflow
alias verify-coverage='./coverage-check.sh infra/common ShardingSphereStatisticsFactory 100'

# Quick coverage check for current module
alias quick-coverage='JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=$(pwd)/$(basename $(pwd))/target/jacoco.exec,excludes=**/*$Lombok*:**/lombok/**" ./mvnw test jacoco:report -Djacoco.skip=false -pl $(basename $(pwd)) && open $(basename $(pwd))/target/site/jacoco/index.html'
```

#### Coverage Report Interpretation
- **Green highlighting**: Fully covered code (100%)
- **Yellow highlighting**: Partially covered code
- **Red highlighting**: Uncovered code (0%)
- **Diamond markers**: Branch coverage points
- **Coverage percentages**: Instruction vs. Branch coverage

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

### Common Traps and Avoidance Methods
- **Early Exit Traps**: Tests exit early due to failed condition checks without reaching core logic
- **Incomplete Mock Traps**: Missing database, rule, or metadata configurations
- **Surface Pass Traps**: Tests pass assertions but don't execute target code
- **Coverage Misleading Traps**: Relying on test passage instead of actual coverage verification
- **Short-Circuit Traps**: Not testing each sub-condition within compound conditions separately
- **Optional Chain Traps**: Not covering all path combinations of Optional chained calls
- **Boundary Value Traps**: Ignoring null values, empty collections, index out-of-bounds, and other boundary cases
- **Mock Chain Traps**: Incomplete mock configuration for deep nested dependencies
- **Atomic Condition Traps**: Testing compound conditions as a whole, missing sub-condition branches

### Emergency Procedures
- **Immediate termination**: If code deletion exceeds 10 lines without instruction
- **Immediate stop**: If tests fail after changes
- **Immediate report deviations**: As soon as detected
