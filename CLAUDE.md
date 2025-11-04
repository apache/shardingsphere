# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage
- `Enhance:` Transparent features: sharding, encryption, security, governance
- `Pluggable:` Micro-kernel + 3-layer pluggable architecture

## Quick Reference (Top 7 Rules)

1. Follow project coding standards
2. Elegance-first, minimalism-second principle
3. Prioritize readability as highest priority
4. 100% test coverage for all new code
5. NEVER auto-commit to Git without explicit instruction
6. ONLY modify explicitly mentioned files and directories
7. Apply formatting to new code ONLY

## Core Prohibitions

- NEVER make changes outside instruction scope
- NEVER perform "helpful" refactoring or improvements
- NEVER create unrelated files without instruction

## Code Intelligence Principles

### Universal Design Philosophy
- **Elegance-First Principle**: Readability first, maintainability second
- **Logical Mapping**: Code organization follows natural business logic flow - methods grouped by business functionality, classes reflect domain concepts, packages follow feature boundaries
- **Cohesion over Granularity**: Keep related functionality together rather than over-splitting
- **Directness Principle**: Solve problems directly without unnecessary abstraction layers
- **Essential Naming**: Names reflect the essence, not implementation details
- **Minimalism**: Use the most concise expression that maintains elegance and functionality

### Elegant Code Standards
Strictly follow all coding standards in [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

## Testing Philosophy

### Unified Testing Standards
- 100% line and branch coverage for all new code
- Test execution speed: <1 second per test case
- Focus on behavior testing over implementation details
- **Branch Minimal Coverage**: Analyze uncovered branches, write minimal test cases only
- **Test Set Minimization**: No redundant tests, focus on branch coverage

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
- Focus on elegant and minimal code expression

### Formatting Standards
- < 200 chars per line, max 3 nested levels, no unnecessary breaks
- Keep empty lines between methods
- Remove empty lines within methods
- **Javadoc Only**: Only Javadoc comments allowed, no inline comments

### Excellence Requirements
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches
- **Quality Validation**: Ensure immediate usability, actionable recommendations

## Quality Metrics

### Success Criteria
- Code compiles without warnings
- No functionality regression
- Spotless formatting passes

### Code Standards
- **Simplicity**: <50 lines for simple functions, <200 lines for complex classes
- **Intelligence**: Patterns recognized, architecture harmonized, future-proof

## Unified Guidelines

### Scope & Permissions
**Allowed Operations:**
- Make independent decisions within task scope

**Scope Boundaries:**
- **Explicit instruction**: What to do
- **Implicit scope**: How to implement within specified files/functions
- **Forbidden**: Anything beyond specified implementation scope

### Git Operations Guidelines
- Prepare commit messages when requested, but NEVER execute commits

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

### Example 1: Cohesion over Granularity
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

### Example 2: Constructor Chaining
Before (duplicate assignments):
```java
public ShardingSphereColumn(ColumnMetaData meta) {
    name = meta.getName();
    dataType = meta.getDataType();
    isPrimaryKey = meta.isPrimaryKey();
    isGenerated = meta.isGenerated();
    isCaseSensitive = meta.isCaseSensitive();
    isVisible = meta.isVisible();
    isUnsigned = meta.isUnsigned();
    isNullable = meta.isNullable();
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

### Example 3: Test Code Standards
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