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
2. Apply elegance-first and minimalism principles (see Universal Design Philosophy)
3. Prioritize readability as highest priority (see Elegance-First Principle)
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
- **Branch Minimal Coverage**: Analyze uncovered branches, write minimal test cases only
- **Test Set Minimization**: Focus on branch coverage and eliminate redundancy

*For detailed testing standards, see CODE_OF_CONDUCT.md reference in Elegant Code Standards section*


## AI Code Understanding Guidelines
*AI-specific capabilities for pattern recognition and style application*

### Pattern Recognition Capabilities
- Identify SPI implementation patterns from existing interface/implementation pairs
- Recognize factory patterns, builder patterns, and strategy patterns from project structure
- Learn field declaration order and naming conventions from existing classes in same package
- Analyze test scenario design and boundary conditions from existing test files

### Style Consistency Application
- Match field access modifiers and declaration order from similar classes
- Apply consistent exception handling and dependency injection patterns from related classes
- Follow established patterns from module architecture (see also ShardingSphere Architecture Patterns)


## ShardingSphere Architecture Patterns
*Architecture decision-making guidance for ShardingSphere-specific contexts*

### Database Abstraction Design Principles
- Create dialect-specific implementations for database-specific features
- Use SPI for extensible components while keeping core logic database-agnostic
- Maintain backward compatibility for metadata structures
- Follow layered architecture: Connection layer, Enhancement layer, Pluggable layer

### Meta-Data Design Patterns
- Use immutable objects for metadata representation (final class + final fields)
- Apply builder pattern and constructor chaining for complex metadata construction
- Include basic validation logic in metadata objects
- Use consistent naming mapping for database concepts

### SPI Implementation Specifications
- Place implementation classes in corresponding spi sub-packages
- Use service discovery mechanism for registration
- Maintain interface backward compatibility
- Provide appropriate default implementations


## Code Consistency Decision Making
*Guidelines for maintaining consistency with existing project code*

### Package Structure and Module Alignment
- Place new classes in appropriate packages based on functional domain
- Maintain same directory structure and hierarchy as similar components
- Respect module boundaries and avoid circular dependencies

### API Design Consistency
- Analyze existing APIs for parameter naming and type patterns
- Apply consistent patterns for exception handling and return values (see Style Consistency Application)


## AI Testing Strategy
*AI-specific testing organization and design capabilities*

### Test Data Construction Strategies
- Build realistic test data that mirrors production scenarios
- Use factory methods to create complex test objects
- Maintain test data independence and repeatability
- Avoid hardcoding; use parameterized tests

*For detailed test organization standards, see CODE_OF_CONDUCT.md reference in Elegant Code Standards section*

### Test Scenario Design Capabilities
- Identify business critical paths for focused testing
- Design integration tests for complex business scenarios
- Construct test cases for boundary conditions and exception situations
- Simulate realistic data scales and concurrent scenarios


## Quality Standards
*Code quality, formatting, and validation requirements*

### Quality Requirements
- **Comprehensive Analysis**: Thoroughly analyze problem context, consider multiple approaches
- **Quality Validation**: Ensure immediate usability, actionable recommendations

### Code Standards
- **Intelligence**: Apply pattern recognition capabilities from AI Code Understanding Guidelines above

### Formatting Standards
*For formatting guidelines, see CODE_OF_CONDUCT.md reference in Elegant Code Standards section*


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
- Apply minimalism principle (see Universal Design Philosophy)


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

### Emergency Procedures
- **Immediate termination** if code deletion exceeds 10 lines without instruction
- **Stop immediately** if tests fail after changes
- **Report deviations** as soon as detected
