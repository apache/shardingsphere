# CLAUDE.md - ShardingSphere AI Development Guide

**ShardingSphere**: Distributed SQL engine for data sharding, distributed transactions, encryption, masking, and more.
Built on Database Plus concept - unified data access layer over existing databases.

## Part 1: Core Principles & Quick Reference

### Project Essentials
- **Core Concepts**: Connect (protocol adaptation), Enhance (feature plugins), Pluggable (micro-kernel architecture)
- **Development Philosophy**: High-quality, self-documenting, 100% tested code
- **Team Culture**: Quality-first, detail-oriented, systematic approach

### The Five Core Principles 🔥

1. **Code Self-Documentation**
   - Zero inline comments - code explains itself through clear naming
   - Extract complex logic to well-named methods
   - Use factory methods for object creation

2. **100% Test Coverage**
   - Every line, branch, and method must be tested
   - Coverage-driven development - use JaCoCo reports to guide testing
   - Focus on branch coverage, eliminate redundant tests

3. **Follow Project Standards**
   - Match existing code patterns and naming conventions
   - Use established architectural patterns (SPI, builder, immutable design)
   - Maintain consistency with similar components

4. **Work Within Scope**
   - Only make changes within explicit task boundaries
   - No "helpful" refactoring or improvements
   - Never create unrelated files

5. **Format Before Completion**
   - Always run `./mvnw spotless:apply -Pcheck` before task completion
   - Ensure all modified code follows project formatting

### Task Classification Standards

#### Task Type Identification

**Source Code Tasks**
- **File Patterns**: `**/src/main/java/**/*.java`, `**/src/test/java/**/*.java`, `**/pom.xml`
- **Impact**: Code compilation, test execution, coverage analysis, functionality
- **Examples**: New feature development, bug fixes, test improvements, configuration changes

**Documentation Tasks**
- **File Patterns**: `**/*.md`, `docs/**/*`, `**/README*`, `.github/**/*.md`
- **Impact**: Content accuracy, user experience, knowledge transfer
- **Examples**: API documentation, user guides, architecture docs, release notes

**Mixed Tasks**
- **Characteristics**: Involves both source code and documentation changes
- **Approach**: Apply combined validation based on changed file types
- **Examples**: Feature additions with documentation, API changes with examples

#### Differentiated Validation Standards

**Source Code Task Requirements**
- ✅ 100% test coverage (JaCoCo validation)
- ✅ Code compilation and build success
- ✅ Code formatting with Spotless
- ✅ All quality checks pass (Checkstyle, etc.)
- ✅ Self-documenting code with zero inline comments

**Documentation Task Requirements**
- ✅ Link validity verification
- ✅ Markdown format consistency
- ✅ Content accuracy review
- ✅ Spelling and grammar check
- ❌ No test coverage or compilation required

**Mixed Task Requirements**
- Apply source code standards to `.java` files
- Apply documentation standards to `.md` files
- Ensure consistency between code and documentation changes

### Quick Checklists

#### Before Starting Any Task
- [ ] Re-read relevant sections of this guide
- [ ] **Identify task type**: Source code, documentation, or mixed task
- [ ] Understand the success criteria and validation requirements for your task type
- [ ] Review specific standards for your task type

#### Before Task Completion

**For Source Code Tasks:**
- [ ] All validation requirements from Differentiated Validation Standards (see above)
- [ ] Additional cleanup: Remove unused mocks, imports, and dead code
- [ ] Optimize mock configurations and branch coverage

**For Documentation Tasks:**
- [ ] All validation requirements from Differentiated Validation Standards (see above)
- [ ] Test code examples and snippets when applicable

**For Mixed Tasks:**
- [ ] Apply source code standards to `.java` files
- [ ] Apply documentation standards to `.md` files
- [ ] Ensure consistency between code and documentation changes

### Common Pitfalls to Avoid
- **Internal blank lines** in methods - extract private methods instead
- **Variable naming confusion** - use `actual` in tests, `result` in production
- **Early test exits** - configure mocks properly to reach target code
- **Surface-level coverage** - verify actual coverage improvement with JaCoCo

#### Code Cleanup Standards
- **Dead Mock Objects**: Remove all mock objects never referenced in tests
- **Unused Imports**: Remove imports that are not used in the file
- **Redundant Mock Setup**: Extract common mock configurations to private methods
- **Unreachable Code**: Remove any code that can never be executed
- **Duplicate Test Logic**: Eliminate tests that don't improve branch coverage

---

## Part 2: Development Process & Quality Standards

### Unified Development Workflow

```mermaid
graph LR
A[Analyze Task] --> B[Identify Task Type]
B --> C[Choose Validation Strategy]
C --> D[Design & Implement]
D --> E[Task-Specific Validation]
E --> F[Format Code]
F --> G[Complete]
```

#### Task-Specific Workflows

**Source Code Tasks:**
1. Analyze Task → Identify as Source Code → Coverage Analysis → Design & Implement → Verify Coverage → Format Code → Complete

**Documentation Tasks:**
1. Analyze Task → Identify as Documentation → Content Review → Design & Implement → Content Validation → Format Check → Complete

**Mixed Tasks:**
1. Analyze Task → Identify as Mixed → Combined Strategy → Design & Implement → Multi-Type Validation → Format & Complete → Complete

### Simplified Testing Process

#### Step 1: Analyze & Design
- **Code Flow Analysis**: Understand complete execution paths
- **Coverage Gap Identification**: Use JaCoCo to find uncovered branches
- **Dependency Mapping**: Identify all mocks needed for complete chain
- **Test Scenarios**: Design realistic business cases for uncovered branches

#### Step 2: Implement & Verify
- **Mock Configuration**: Set up complete dependency chains with `RETURNS_DEEP_STUBS`
- **Test Implementation**: Write tests targeting specific uncovered branches
- **Coverage Validation**: Run tests and verify coverage improvement
- **Iteration**: Add tests until 100% coverage achieved

#### Step 3: Coverage Confirmation
```bash
# Verify class coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=your.target.ClassName \
  -pl your-module

# Verify package coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern="your.package.**" \
  -Djacoco.minimum.coverage=1.00 \
  -pl your-module
```

- **BUILD SUCCESS**: Coverage meets requirements
- **BUILD FAILURE**: Review `module/target/site/jacoco/index.html` for uncovered lines

### Code Quality Standards

#### Self-Documentation Patterns
```java
// ❌ WRONG - Inline comments
// Check if user is admin and has permission
if (user.isAdmin() && user.hasPermission()) {
    // ...
}

// ✅ RIGHT - Self-documenting code
if (userIsAdminWithPermission()) {
    // ...
}

// Extract complex logic to private methods
private boolean userIsAdminWithPermission() {
    return user.isAdmin() && user.hasPermission();
}
```

#### Naming Conventions
- **Test methods**: `assert[MethodName]With[Condition]` pattern
- **Test variables**: Use `actual` for assertions, `result` for production code
- **Factory methods**: Describe object creation intent
- **Private methods**: Explain what logic they encapsulate

#### Comprehensive Test Patterns
```java
// Standard test structure (Given-When-Then)
@Test
void assertMethodWithCondition() {
    // Given - setup complete mock chain
    mockDependencyChain();

    // When - call method directly
    MyResult actual = target.methodUnderTest(input);

    // Then - assert with Hamcrest
    assertThat(actual, is(expectedResult));
}

// Branch-focused test patterns for utility classes
@Test
void assertMethodWithConditionTruePathExpectsResult() {
    // Given - configure mocks for true branch
    mockConditionReturnsTrue();

    // When - call method that hits the true branch
    MyResult actual = utilityClass.methodWithCondition(input);

    // Then - verify true branch result
    assertThat(actual, is(expectedTrueResult));
}

// Advanced branch testing naming
@Test
void assert[MethodName]When[BranchCondition]Expects[Result]() {
    // Given - setup for specific branch condition
    // When - execute method
    // Then - verify expected result for this branch
}
```

#### Test Organization Principles
- **Branch-first naming**: `assert[MethodName]When[BranchCondition]Expects[Result]`
- **One test per branch**: Each test should target a specific conditional branch
- **Minimal test count**: Avoid redundant tests that don't improve branch coverage
- **Test isolation**: Each test controls specific constructor and method behaviors
- **Resource management**: Always clean up MockedConstruction with try-with-resources
- **Verification focus**: Each test validates one specific branch outcome

#### Mock Configuration Standards & Strategies

Mock configuration is essential for comprehensive testing. Use these patterns to control dependencies and achieve 100% branch coverage.

##### Core Mock Configuration Patterns
```java
// ✅ Complete dependency chain with deep stubs
when(dependencyA.getDependencyB().process(input)).thenReturn(result);

// ✅ Self-documenting mock setup
private void mockUserPermissionService() {
    when(userPermissionService.hasAdminPermission(any())).thenReturn(true);
}

// ✅ Constructor mocking for utility classes
try (MockedConstruction<DatabaseTypeRegistry> mocked = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
    DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
    when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(true);
    when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
})) {
    // Test code that instantiates DatabaseTypeRegistry
}

// ✅ Complex external dependency mocking
try (MockedConstruction<ExternalClass> mocked = mockConstruction(ExternalClass.class, (mock, context) -> {
    when(mock.getExternalCondition()).thenReturn(true);
    when(mock.getDialectDatabaseMetaData().getConnectionOption().isInstanceConnectionAvailable()).thenReturn(false);
})) {
    boolean actual = UtilityClass.staticMethodWithExternalDependency();
    assertThat(actual, is(expected));
}
```

##### Mock Configuration Strategies

**MockedStatic vs MockedConstruction**
- **MockedStatic**: For static method calls (`UtilityClass.staticMethod()`)
- **MockedConstruction**: For object creation (`new UtilityClass()`)
- **Placement**: MockedConstruction must wrap the code that creates the objects

**Key Configuration Principles**
- **Deep stubs**: Use `RETURNS_DEEP_STUBS` for complex nested dependencies
- **Resource management**: Always use try-with-resources for MockedConstruction
- **Behavior isolation**: Each test controls specific constructor behaviors
- **Constructor identification**: Look for `new SomeClass()` in target methods
- **Constructor interception**: Control how dependency objects are created
- **Complete call chain**: Configure full dependency paths with `RETURNS_DEEP_STUBS`
- **Utility class testing**: Perfect for static utility methods with external dependencies

#### Assertion Style (Mandatory)
```java
// ✅ Use Hamcrest matchers for readability
assertThat(actual, is(expected));
assertThat(actualList, hasSize(expectedSize));

// ❌ Avoid JUnit assertions (except boolean/null)
// assertEquals(actual, expected); // Not preferred
```

### Coverage Tools Reference

#### Essential Commands
```bash
# Generate coverage report
./mvnw clean test jacoco:report -Djacoco.skip=false -pl module-path

# Check specific class coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=com.example.ClassName

# Verify package coverage with 100% requirement
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern="com.example.**" \
  -Djacoco.minimum.coverage=1.00

# Format code
./mvnw spotless:apply -Pcheck

# Full build with tests
./mvnw install -T1C
```

#### Coverage Patterns
```bash
# Package level coverage
-Djacoco.check.class.pattern="com.example.**"

# By type coverage
-Djacoco.check.class.pattern="**/*Service"

# Multiple patterns
-Djacoco.check.class.pattern="**/*Service,**/*Manager"
```

#### Coverage Interpretation
- **Target**: 100% instruction, line, branch, and method coverage
- **Validation**: Must verify improvement with JaCoCo HTML reports
- **Focus**: Branch coverage over redundant line coverage
- **Strategy**: Minimal tests targeting specific uncovered branches
- **BUILD SUCCESS**: Coverage meets requirements
- **BUILD FAILURE**: Review `module/target/site/jacoco/index.html` for uncovered lines

#### Advanced Coverage Strategies
```bash
# Branch gap analysis - identify uncovered branches
./mvnw clean test jacoco:report -Djacoco.skip=false
open target/site/jacoco/index.html  # Check red diamonds for missing branches
```

#### Constructor Path Testing
- **Identify `new` operators**: Look for `new SomeClass()` in target code
- **Use MockedConstruction**: Intercept constructor calls to control behavior
- **Configure complete mock chains**: Ensure all dependency paths are properly mocked
- **Test each branch**: Create focused tests for each conditional branch

#### Utility Class Coverage Optimization
- **Static method analysis**: Map each conditional branch to specific test scenarios
- **Dependency isolation**: Use MockedConstruction to control external dependencies
- **Complete path coverage**: Ensure both true and false paths are tested
- **Verification strategy**: Use JaCoCo HTML reports to confirm 100% branch coverage

---

## Part 3: Project Context & Patterns

### ShardingSphere Architecture

#### Module Structure
```
infra/     - SPI implementations and basic components
parser/    - SQL parsers for dialects and DistSQL
kernel/    - Core functionality (metadata, transaction, authority)
feature/   - Pluggable features (sharding, encryption, shadow)
mode/      - Configuration persistence and coordination
proxy/     - Proxy implementation (MySQL/PostgreSQL/Firebird)
jdbc/      - JDBC driver implementation
test/      - E2E/IT test engine and cases
```

#### Core Design Patterns

**Database Abstraction**
- Database-specific dialect implementations
- SPI for extensible components
- Database-agnostic core logic

**Metadata Design**
- Immutable objects (final classes + final fields)
- Builder patterns for complex construction
- Consistent database concept naming

**SPI Implementation**
- Service discovery mechanism for registration
- Appropriate default implementations
- Use `TypedSPILoader.getService()` in tests when mocking is complex

### Development Patterns

#### Dependency Injection
```java
// Constructor-only injection with final fields
public final class ExampleService {
    private final DependencyService dependencyService;

    public ExampleService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }
}
```

#### Configuration Patterns
```java
// YAML-based configuration
Properties props = PropertiesBuilder.build(new Property("key", "value"));

// Repository abstraction for data access
Repository repository = new YamlRepository(path);
```

// For testing patterns and organization, see Part 2: Comprehensive Test Patterns & Mock Configuration Standards

#### Priority Guidelines
1. **Quality > Speed**: Never compromise on 100% coverage or code standards
2. **Consistency > Preference**: Match existing patterns over personal preferences
3. **Safety > Features**: Preserve existing functionality over adding new features
4. **Clarity > Brevity**: Self-documenting code over clever but unclear code

#### Handling Ambiguity
- **Scope unclear** → Request clarification before proceeding
- **Impact uncertain** → Propose minimal safe experiment
- **Rules conflict** → Follow most restrictive interpretation
- **Emergency needed** → Stop and report constraints immediately

#### Performance Considerations
- Identify performance-sensitive code paths
- Use appropriate data structures (LinkedHashMap for thread-safe iteration)
- Consider concurrency implications in multi-threaded contexts
- Avoid shared mutable state in service classes

#### Security Practices
- Validate input parameters at API boundaries
- Use immutable collections for test data
- Apply appropriate synchronization for shared resources
- Follow secure coding patterns for database operations

---

## Appendix: Tools & Troubleshooting

### Quick Reference Commands

**Source Code Tasks**
```bash
# === Development ===
./mvnw install -T1C                           # Full build
./mvnw install -T1C -DskipTests              # Build without tests
./mvnw spotless:apply -Pcheck                 # Format code

# === Coverage (Quick Reference) ===
./mvnw clean test jacoco:report -Djacoco.skip=false -pl module/path
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=ClassName

# See Part 2: Coverage Tools Reference for detailed commands and strategies
```

**Documentation Tasks**
```bash
# === Documentation Validation ===
# Quick link check (if available)
find . -name "*.md" -exec grep -l "http" {} \;  # Find docs with links

# Markdown format check
markdownlint docs/**/*.md                       # If markdownlint is available

# Spell check (if available)
cspell docs/**/*.md                             # If cspell is configured

# === Documentation Quick Fix ===
./mvnw spotless:apply -Pcheck                 # Format any code examples
```

**Mixed Tasks**
```bash
# === Combined Validation ===
# Apply source code commands to .java files only
# Apply documentation commands to .md files only
# Use file-specific validation based on changed file patterns

# Example: Check changed files
git diff --name-only HEAD~1 | grep "\.java$"   # Changed Java files
git diff --name-only HEAD~1 | grep "\.md$"     # Changed Markdown files
```

### Common Issues & Solutions

#### Coverage Problems
- **Issue**: Tests pass but coverage doesn't improve
- **Cause**: Tests exit early before reaching target code
- **Solution**: Check mock configuration, use coverage reports to verify execution paths

#### Mock Configuration Issues
- **Issue**: Complex nested mocks become unmanageable
- **Solution**: Use `RETURNS_DEEP_STUBS`, extract mock setup to private methods

// For constructor mocking techniques, see Mock Configuration Standards (Part 2)

#### Branch Coverage Challenges
- **Issue**: Tests pass but coverage doesn't reach 100%
- **Solution**: Use MockedConstruction to control all external dependencies in conditional branches
- **Issue**: Complex conditional logic with multiple nested dependencies
- **Solution**: Break down complex conditions into individual branch tests
- **Issue**: Cannot determine which branch is being executed
  - **Solution**: Use JaCoCo HTML reports to identify red diamond (uncovered branches)

#### SPI Testing Complexity
- **Issue**: SPI services are difficult to mock
- **Solution**: Use real services via `TypedSPILoader.getService()` when appropriate

#### Code Formatting Issues
- **Issue**: Spotless formatting fails
- **Solution**: Check for syntax errors, run `./mvnw validate` first

### Extended Resources

#### Documentation Links
- [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) - Detailed coding standards
- Project Wiki - Architecture and design decisions
- JavaDocs - API documentation and examples

#### Best Practice Examples
- Test case patterns in existing test files
- Implementation patterns in similar classes
- SPI implementation examples in infra modules

#### Troubleshooting Checklist
- [ ] Check test dependencies and mock configuration
- [ ] Verify JaCoCo report for actual coverage gaps
- [ ] Ensure code follows existing patterns
- [ ] Confirm all changes are within task scope
- [ ] Run final formatting and validation

---

**Key Success Factors:**
1. **Analyze before coding** - Understand requirements and existing patterns
2. **Test with purpose** - Each test should improve coverage
3. **Code self-documentation** - Zero comments through clear naming
4. **Iterate systematically** - Use coverage reports to guide development
5. **Finish completely** - Format, validate, and verify 100% coverage

*This guide emphasizes practical implementation over theoretical concepts. Focus on writing clean, tested code that follows established patterns.*