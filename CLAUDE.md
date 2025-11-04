# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage
- `Enhance:` Transparent features: sharding, encryption, security, governance
- `Pluggable:` Micro-kernel + 3-layer pluggable architecture

## Document Structure
- **Principles Layer**: Core design principles and prohibitions
- **Standards Layer**: Code, testing, and quality requirements
- **Guidelines Layer**: Operational procedures and examples

## Quick Reference (Top 7 Rules)
*Core rules - see detailed sections for complete requirements*

1. Follow project coding standards (see Elegant Code Standards)
2. Elegance-first, minimalism-second principle (see Minimalism in Universal Design Philosophy)
3. Prioritize readability as highest priority
4. 100% test coverage for all new code (detailed requirements in Testing Philosophy)
5. NEVER auto-commit to Git without explicit instruction
6. ONLY work within explicitly specified scope (see Core Prohibitions)
7. Apply formatting to new code ONLY (see Formatting Standards)

## Core Prohibitions

- NEVER make changes outside instruction scope
- NEVER perform "helpful" refactoring or improvements
- NEVER create unrelated files without instruction

## Code Intelligence Principles
*Core design principles that guide all coding decisions*

### Universal Design Philosophy
- **Elegance-First Principle**: Readability first, maintainability second
- **Minimalism**: Use the most concise expression that maintains elegance and functionality

### Elegant Code Standards
Strictly follow all coding standards in [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

#### Coding Standards Reference
For complete ShardingSphere coding standards and conventions, see: [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

Key areas covered in the coding standards file:
- Naming conventions and code style
- Data structure usage guidelines
- Technical tool specifications
- File formatting requirements
- G4 grammar rules
- Complete unit testing standards

## Testing Philosophy
*Comprehensive testing requirements and standards for all new code*

### Unified Testing Standards
- 100% line and branch coverage for all new code
- Test execution speed: <1 second per test case
- **Branch Minimal Coverage**: Analyze uncovered branches, write minimal test cases only
- **Test Set Minimization**: Focus on branch coverage and eliminate redundancy

*For detailed testing standards, see Coding Standards Reference above*


## Quality Standards
*Code quality, formatting, and validation requirements*

### Quality Requirements
- Follow Universal Design Philosophy for elegant and minimal code expression
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches
- **Quality Validation**: Ensure immediate usability, actionable recommendations

### Code Standards
- **Intelligence**: Patterns recognized, architecture harmonized, future-proof

### Formatting Standards
*For formatting guidelines, see Coding Standards Reference above*


## Unified Guidelines
*Operational scope, permissions, and decision-making framework*

### Scope & Permissions
**Allowed Operations:**
- Make independent decisions within task scope

**Scope Boundaries:**
- Work only within explicitly specified scope
- See Core Prohibitions for complete limitations

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
- Apply minimalism principle: Favor minimal changes over comprehensive solutions


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


### Verification Process
1. **Pre-change**: Verify task matches user request, analyze existing patterns
2. **Post-change**: Run relevant tests, verify no regression
3. **Continuous**: Re-read protocol before tasks, verify compliance

### Emergency Procedures
- **Immediate termination** if code deletion exceeds 10 lines without instruction
- **Stop immediately** if tests fail after changes
- **Report deviations** as soon as detected