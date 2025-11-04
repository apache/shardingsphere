# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage
- `Enhance:` Transparent features: sharding, encryption, security, governance
- `Pluggable:` Micro-kernel + 3-layer pluggable architecture

## Quick Reference (Top 8 Rules)

1. Follow CODE_OF_CONDUCT.md for coding standards
2. Generate minimal essential code only
3. Prioritize readability as highest priority
4. 100% test coverage for all new code
5. NEVER auto-commit to Git without explicit instruction
6. ONLY edit explicitly mentioned files
7. Apply formatting to new code ONLY
8. One-shot excellence - provide optimal solutions in single response

## Core Prohibitions

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

## Code Intelligence Principles

### Universal Design Philosophy
- **Boundary Condition Priority**: Handle edge cases naturally in main logic, not in separate methods
- **Logical Mapping**: Code organization should reflect natural business logic flow
- **Cohesion over Granularity**: Keep related functionality together rather than over-splitting
- **Directness Principle**: Solve problems directly without unnecessary abstraction layers
- **Essential Naming**: Names reflect the essence, not implementation details

### Clean Code Standards
- **Single Responsibility**: Each function/class has one clear purpose
- **DRY Principle**: Detect and eliminate duplication automatically
- **Constructor Chaining**: Use this() for multiple constructors, avoid duplicate field assignments
- **Effortless Reading**: Code reads like well-written prose
- **Optimal Abstraction**: Create right level of abstraction for problem domain
- **Self-Documenting Design**: Code explains its purpose through structure

### Evolutionary Design
- **Open-Closed Principle**: Code open for extension, closed for modification
- **Fail-Fast Design**: Detect errors early and exit cleanly
- **Graceful Degradation**: Implement automatic recovery when possible
- **Future-Proofing**: Anticipate likely future requirements

## Testing Philosophy

### Unified Testing Standards
- 100% line and branch coverage for all new code
- Test execution speed: <1 second per test case
- Focus on behavior testing over implementation details
- **Branch Minimal Coverage**: Analyze uncovered branches, write minimal test cases only
- **Test Case Merging**: Integrate boundary conditions into main test methods
- **Test Set Minimization**: Avoid excessive splitting, maintain concise coverage

### Test Code Standards
- **Method Naming**: Start with "assert", use concise names focused on core functionality
- **Assertions**: Use AssertJ style: `assertThat(actual, is(expected))`
- **Variables**: Name test results as "actual" (not "result")
- **Mock Priority**: Prioritize Mockito mock and mockStatic, avoid using spy
- **Simplicity**: Test code must be simpler than business code

### Testing Process
- **Method Order Consistency**: Match source code method declaration order
- **Redundancy Elimination**: Remove duplicate or unnecessary test cases
- **Integration Principle**: Seamlessly integrate with existing test files and conventions

## Quality Excellence

### Code Quality Requirements
- Generate minimal essential code only (<50 lines for simple functions)
- Prioritize readability as highest priority (max 80 chars per line, max 3 nested levels)
- Consider extreme performance optimization (<100ms for common operations)
- Follow CODE_OF_CONDUCT.md (clean code principles, naming, formatting)

### Formatting Standards
- < 200 chars per line, no unnecessary breaks
- Keep empty lines between methods
- Remove empty lines within methods
- **No Code Comments**: Code should be self-documenting

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

## Essential Examples

### Example 1: Boundary Condition Priority
Before (separate handling):
```java
public void processConnection(DatabaseConnection conn) {
    conn.execute(sql);
}

public void validateConnection(DatabaseConnection conn) {
    if (conn == null || !conn.isValid()) {
        throw new IllegalArgumentException();
    }
}
```

After (integrated boundary conditions):
```java
public void processConnection(DatabaseConnection validConnection) {
    if (validConnection != null && validConnection.isValid()) {
        validConnection.execute(sql);
    }
}
```

### Example 2: Cohesion over Granularity
Before (excessive splitting):
```java
@Test
void assertContainsColumn() {
    assertTrue(table.containsColumn("id"));
    assertFalse(table.containsColumn("name"));
}

@Test
void assertContainsColumnWithNull() {
    assertFalse(table.containsColumn(null));
}
```

After (cohesive testing):
```java
@Test
void assertContainsColumn() {
    assertTrue(table.containsColumn("id"));
    assertFalse(table.containsColumn("name"));
    assertFalse(table.containsColumn(null));
}
```

### Example 3: Constructor Chaining
Before (duplicate assignments):
```java
public ShardingSphereColumn(ColumnMetaData meta) {
    name = meta.getName();
    dataType = meta.getDataType();
    // ... 8 manual field assignments
}
```

After (constructor chaining):
```java
public ShardingSphereColumn(ColumnMetaData meta) {
    this(meta.getName(), meta.getDataType(),
        meta.isPrimaryKey(), meta.isGenerated(),
        meta.isCaseSensitive(), meta.isVisible(),
        meta.isUnsigned(), meta.isNullable());
}
```

### Example 4: Test Code Standards
Before:
```java
@Test
void testCalculateTotal() {
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

```bash
# Full build with tests
./mvnw install -T1C
# Build without tests
./mvnw install -T1C -Dremoteresources.skip -DskipTests
# Format code
./mvnw spotless:apply -Pcheck
```

## Project Structure

- `infra/`: SPI implementations & basic components
- `parser/`: SQL parser for dialects & DistSQL
- `kernel/`: Core functionality (metadata, transaction, authority)
- `feature/`: Pluggable features (sharding, encryption, shadow)
- `mode/`: Configuration persistence & coordination
- `proxy/`: Proxy implementation (MySQL/PostgreSQL/Firebird protocols)
- `jdbc/`: JDBC driver implementation
- `test/`: E2E/IT test engine & cases

## Operational Procedures

### Implementation Rules
1. **Direct Code Generation**: Generate final code & call tools directly
2. **Isolate Changes**: Edit smallest possible blocks
3. **Maintain Patterns**: Follow existing architectural conventions
4. **Apply Context-Aware Design**: Use established design principles

### Verification Process
1. **Pre-change**: Verify task matches user request, analyze existing patterns
2. **Post-change**: Run relevant tests, verify no regression
3. **Continuous**: Re-read protocol before tasks, verify compliance

### Emergency Procedures
- **Immediate termination** if code deletion exceeds 10 lines without instruction
- **Stop immediately** if tests fail after changes
- **Report deviations** as soon as detected