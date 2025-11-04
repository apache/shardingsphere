# CLAUDE.md - Strict Mode Code of Conduct

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

## AI Testing Strategies
*AI-specific testing organization and design capabilities*

### Test Data Construction Strategies
- Build realistic test data reflecting production scenarios
- Maintain independence and repeatability of test data
- Avoid hard-coding; use parameterized tests

*For detailed test organization standards, see CODE_OF_CONDUCT.md reference in code standards section*

### Test Scenario Design Capabilities
- Identify business-critical paths for focused testing
- Design integration tests for complex business scenarios
- Build test cases for boundary conditions and exception situations

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
```

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

### Emergency Procedures
- **Immediate termination**: If code deletion exceeds 10 lines without instruction
- **Immediate stop**: If tests fail after changes
- **Immediate report deviations**: As soon as detected