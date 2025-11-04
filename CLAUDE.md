# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage
- `Enhance:` Transparent features: sharding, encryption, security, governance
- `Pluggable:` Micro-kernel + 3-layer pluggable architecture

## Quick Reference (Top 10 Rules)

1. Follow CODE_OF_CONDUCT.md for coding standards
2. Generate minimal essential code only
3. Prioritize readability as highest priority
4. 100% test coverage for all new code
5. NEVER auto-commit to Git without explicit instruction
6. ONLY edit explicitly mentioned files
7. Make decisions within task scope ONLY
8. NEVER perform autonomous "best practice" improvements
9. Apply formatting to new code ONLY
10. Provide comprehensive analysis before coding

## Core Prohibitions (Unified)

### Git Operations
- NEVER auto-commit changes without explicit user command
- Prepare commit messages when requested, but NEVER execute commits

### Code Changes
- ONLY modify explicitly mentioned files, functions, or lines
- NEVER make changes outside instruction scope
- NEVER perform "helpful" refactoring or improvements

### File Creation
- ONLY create files in explicitly specified directories
- NEVER create unrelated files without instruction

## AI-Enhanced Standards

### Code Quality Requirements
- Generate minimal essential code only (<50 lines for simple functions)
- Prioritize readability as highest priority (max 80 chars per line, max 3 nested levels)
- Consider extreme performance optimization (<100ms for common operations)
- Follow CODE_OF_CONDUCT.md (clean code principles, naming, formatting)

### Formatting
- < 200 chars per line, no unnecessary breaks
- Keep empty lines between methods
- Remove empty lines within methods
- **Comments**: No code comments - "code as documentation"
 
### Testing Standards

#### Testing Strategy Standards
- 100% line and branch coverage for all new code
- No redundant test cases - each test validates unique behavior
- Test execution speed: <1 second per test case
- Follow CODE_OF_CONDUCT.md (AIR principle, BCDE design, naming conventions)
- Focus on behavior testing over implementation details
- **Branch Minimal Coverage**: Analyze all conditional branches first, identify uncovered branches, then write minimal test cases for missing coverage only

#### Test Code Standards
- **Method Naming**: Test methods start with "assert" (not "test")
- **Assertions**: Use AssertJ style: `assertThat(actual, is(expected))`
- **Variables**: Name test results as "actual" (not "result")
- **Mock Priority Principle**: Prioritize using Mockito mock and mockStatic, avoid using spy
- **Minimum Complexity Principle**: Test code must be simpler than business code, choose the simplest but effective testing method

#### Testing Process Standards
- **Simplification Strategy**: Minimize test code lines, cognitive load, and maintenance costs
- **Single Responsibility Testing**: Each test validates only one core functionality point
- **Test Integration Principle**: New test cases should integrate seamlessly with existing test files, maintain consistent naming conventions, and follow established code style patterns

### Intelligent Code Standards

#### Contextual Intelligence
- **Pattern Recognition**: Identify and apply existing architectural patterns
- **Style Adaptation**: Seamlessly match current codebase conventions
- **Architectural Harmony**: Ensure new code fits existing design philosophy
- **Self-Documenting Design**: Code should explain its purpose through structure

#### Clean Code Intelligence
- **Single Responsibility**: Each function/class has one clear purpose
- **DRY Principle**: Detect and eliminate duplication automatically
- **Constructor Chaining Principle**: Use this() for constructor chaining when multiple constructors exist, avoiding duplicate field assignment logic
- **Effortless Reading**: Code reads like well-written prose
- **Optimal Abstraction**: Create right level of abstraction for problem domain
- **Minimum Complexity Principle**: As the highest priority guiding principle for all testing decisions, prioritize simplest implementation approach and minimal intermediate variables

#### Evolutionary Code Design
- **Open-Closed Principle**: Code open for extension, closed for modification
- **Fail-Fast Design**: Detect errors early and exit cleanly
- **Graceful Degradation**: Implement automatic recovery when possible
- **Future-Proofing**: Anticipate likely future requirements

### Excellence Requirements
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches
- **One-Shot Excellence**: Provide optimal solutions in single response
- **Quality Validation**: Ensure immediate usability, actionable recommendations

## Quality Metrics

### Success Criteria
- Code compiles without warnings
- All tests pass in <5 minutes
- No functionality regression
- Spotless formatting passes
- 100% coverage for new code
- Test code follows all formatting and style requirements

### Code Standards
- **Simplicity**: <50 lines for simple functions, <200 lines for complex classes
- **Performance**: <100ms execution time for common operations
- **Readability**: <80 characters per line, max 3 nested levels
- **Intelligence**: Patterns recognized, architecture harmonized, future-proof

## Unified Guidelines

### Scope & Permissions
**Allowed Operations:**
- Edit explicitly mentioned files
- Create files in explicitly specified directories
- Make independent decisions within task scope
- Apply formatting tools to new code only

**Scope Boundaries:**
- **Explicit instruction**: What to do
- **Implicit scope**: How to implement within specified files/functions
- **Forbidden**: Anything beyond specified implementation scope

### Decision & Safety
**Ambiguous Situations:**
- **Scope unclear** → Request clarification
- **Impact uncertain** → Propose minimal safe experiment
- **Rules conflict** → Follow most restrictive interpretation
- **Emergency needed** → Stop and report constraints

**Safety Principles:**
- Preserve existing functionality over adding features
- Maintain current behavior over ideal implementation
- Favor minimal changes over comprehensive solutions

## Practical Examples

### Example 1: Intelligent Bug Fix
Before (potential null pointer):
```java
public void processConnection(DatabaseConnection conn) {
    conn.execute(sql);
}
```

After (context-aware, self-documenting):
```java
public void processConnection(DatabaseConnection validConnection) {
    if (validConnection.isValid()) {
        validConnection.execute(sql);
    }
}
```

### Example 2: Pattern-Based New Feature
Following Repository pattern for consistency:

```java
public class MySQLConnectionValidator implements DatabaseValidator {
    
    private static final int TIMEOUT_MS = 5000;
    
    public ValidationResult validate(ConnectionConfig config) {
        return timeoutAwareValidation(config);
    }
}
```

### Example 3: Evolutionary Design
Open-closed principle implementation:
```java
public abstract class AbstractDatabaseConnector {
    
    protected abstract Connection createConnection(Config config);
    
    public final ValidationResult validate(Config config) {
        return preValidation(config);
    // Implementation in child classes
    return createConnection(config);
    }
}
```

### Example 4: Constructor Chaining Principle

Before (duplicate field assignments):
```java
public final class ShardingSphereColumn {
    
    private final String name;
    
    private final int dataType;
    // ... other fields
    
    public ShardingSphereColumn(final ColumnMetaData columnMetaData) {
        name = columnMetaData.getName();
        dataType = columnMetaData.getDataType();
        // ... 8 manual field assignments
    }
}
```

After (constructor chaining):
```java
public final class ShardingSphereColumn {
    
    private final String name;
    
    private final int dataType;
    // ... other fields
    
    public ShardingSphereColumn(final ColumnMetaData columnMetaData) {
        this(columnMetaData.getName(), columnMetaData.getDataType(),
            columnMetaData.isPrimaryKey(), columnMetaData.isGenerated(),
            columnMetaData.isCaseSensitive(), columnMetaData.isVisible(),
            columnMetaData.isUnsigned(), columnMetaData.isNullable());
    }
}
```

### Example 5: Test Code Standards

Before:
```java
@Test
void testCalculateTotal() {
    // Setup test data
    List<Order> orders = Arrays.asList(new Order(100), new Order(200));
    OrderService service = new OrderService();
    double result = service.calculateTotal(orders);
    assertEquals(300.0, result, 0.01);
}
```

After:
```java
@Test
void assertCalculateTotal() {
    List<Order> orders = Arrays.asList(new Order(100), new Order(200));
    OrderService service = new OrderService();
    double actual = service.calculateTotal(orders);
    assertThat(actual, is(300));
}
```

## Build System

Maven build commands:

```bash
# Full build with tests
./mvnw install -T1C
# Build without tests
./mvnw install -T1C -Dremoteresources.skip -DskipTests
# Format code
./mvnw spotless:apply -Pcheck
# Check code style
./mvnw checkstyle:check -Pcheck
```

## Project Structure

- `infra/`: SPI implementations & basic components
- `parser/`: SQL parser for dialects & DistSQL
- `kernel/`: Core functionality (metadata, transaction, authority)
- `feature/`: Pluggable features (sharding, encryption, shadow)
- `mode/`: Configuration persistence & coordination (standalone/cluster)
- `proxy/`: Proxy implementation (MySQL/PostgreSQL/Firebird protocols)
- `jdbc/`: JDBC driver implementation
- `test/`: E2E/IT test engine & cases

## Detailed Procedures

### Operational Procedures
1. **Direct Code Generation**: Generate final code & call tools directly
   - Apply formatting tools for new code only
   - Make intelligent decisions within task scope
   - **CRITICAL**: Automated generation permitted, automatic Git commits forbidden

2. **Implementation Rules**:
   - Isolate edits to smallest possible blocks
   - Maintain existing architectural patterns
   - Preserve existing style and design philosophy
   - Apply context-aware design principles

### Verification Process
1. **Pre-change**: Verify task matches user request, analyze existing patterns
2. **Post-change**: Run relevant tests, verify no regression, confirm architectural harmony
3. **Continuous**: Re-read protocol before tasks, verify compliance, report violations

### Emergency Procedures
- **Immediate termination** if code deletion exceeds 10 lines without instruction
- **Stop immediately** if tests fail after changes
- **Report deviations** as soon as detected
