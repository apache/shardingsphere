# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly connect applications and heterogeneous databases.
- `Enhance:` Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).
- `Pluggable:` Leveraging micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibly. Developers can customize their ShardingSphere just like building with LEGO blocks.

## Rule Hierarchy (Priority Order)

1. **Quick Reference** (First check - Daily usage)
2. **Core Prohibitions** (Critical - Absolute restrictions)
3. **AI-Enhanced Standards** (Quality - Excellence requirements)
4. **Detailed Procedures** (Reference - Implementation details)

## Quick Reference (Top 10 Rules)

1. Follow CODE_OF_CONDUCT.md for coding standards
2. Generate minimal essential code only
3. Prioritize readability as highest priority
4. 100% test coverage for all new code
5. ONLY edit explicitly mentioned files
6. Make decisions within task scope ONLY
7. NEVER perform autonomous "best practice" improvements
8. Apply formatting to new code ONLY
9. Provide comprehensive analysis before coding
10. NEVER auto-commit to Git without explicit instruction

## Core Prohibitions (Unified)

### Git Operations
- NEVER auto-commit changes to Git without explicit user command
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

### Testing Standards
- 100% line and branch coverage for all new code
- No redundant test cases - each test validates unique behavior
- Test execution speed: <1 second per test case
- Follow CODE_OF_CONDUCT.md (AIR principle, BCDE design, naming conventions)
- Focus on behavior testing over implementation details

### Excellence Requirements
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches, anticipate issues
- **One-Shot Excellence**: Provide optimal solutions in single response, avoid incremental fixes
- **Quality Validation**: Ensure immediate usability, actionable recommendations, alignment with best practices

## Quality Metrics

### Success Criteria
- Code compiles without warnings
- No functionality regression
- Spotless formatting passes
- 100% coverage for new code

### Code Standards
- **Simplicity**: <50 lines for simple functions, <200 lines for complex classes
- **Performance**: <100ms execution time for common operations
- **Readability**: <80 characters per line, max 3 nested levels
- **Imports**: Remove unused imports, prefer specific imports

### Testing Standards
- **Coverage**: 100% line, 95%+ branch coverage
- **Speed**: <1 second per test case
- **Redundancy**: Zero duplicate test scenarios
- **Isolation**: Each test runs independently

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
   - Make independent decisions within task scope
   - **CRITICAL**: Automated generation permitted, automatic Git commits forbidden

2. **Implementation Rules**:
   - Isolate edits to smallest possible blocks
   - Maintain existing style, even if suboptimal
   - Preserve all comments unless directly contradictory

### Verification Process
1. **Pre-change**: Verify task matches user request, confirm target files/lines referenced
2. **Post-change**: Run relevant tests, verify no regression, confirm scope matches request
3. **Continuous**: Re-read protocol before tasks, verify compliance, report violations

### Emergency Procedures
- **Immediate termination** if code deletion exceeds 10 lines without instruction
- **Stop immediately** if tests fail after changes
- **Report deviations** as soon as detected