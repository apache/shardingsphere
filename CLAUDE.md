# CLAUDE.md - Strict Mode Code of Conduct

Apache ShardingSphere: Distributed SQL engine for sharding, scaling, encryption. Database Plus concept - unified service layer over existing databases.

Core concepts:
- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly connect applications and heterogeneous databases.
- `Enhance:` Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).
- `Pluggable:` Leveraging the micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibly. Developers can customize their ShardingSphere just like building with LEGO blocks.

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

## Code Standards

1. Follow CODE_OF_CONDUCT.md (clean code principles, naming conventions, formatting)
2. **AI-Specific Requirements**:
   - Generate minimal essential code only
   - Prioritize readability as highest priority
   - Consider extreme performance optimization
3. Prioritize scalability, avoid over-engineering

## Testing Standards

1. Follow CODE_OF_CONDUCT.md (AIR principle, BCDE design, naming conventions)
2. **AI-Specific Requirements**:
   - 100% line and branch coverage for all new code
   - No redundant test cases - each test validates unique behavior
   - Focus on behavior testing over implementation details

## Absolute Prohibitions (Zero-Tolerance)

1. **Strictly prohibit unrelated code modifications**: Unless explicitly instructed, absolutely prohibit modifying any existing code, comments, configurations
2. **Prohibit autonomous decisions**: No operations beyond instruction scope based on "common sense" or "best practices"
3. **Prohibit unrelated file creation**: Unless explicitly instructed, prohibit creating any new files

## Whitelist Permission Framework

1. Automatic approval scope:
   - Edit explicitly mentioned files
   - Create files in explicitly specified directories

2. Change scope locking:
   - Whitelist mechanism: Strictly limit modifications to explicitly mentioned files, functions, blocks, line numbers
   - Ambiguous instructions: Confirm full name & location before proceeding

3. Code style:
   - Prohibit automatic formatting of entire files/projects without explicit instruction
   - Apply Spotless automatically after code generation for new code only

## Operational Procedures

1. **Direct Code Generation**: Generate final code & call tools directly without user approval
   - Apply formatting tools automatically when needed for new code
   - Make independent decisions within task scope
   - **IMPORTANT**: Automated code generation & tool calls permitted, automatic Git commits strictly prohibited
   - **Git Operations**: Prepare commit messages when requested, but never execute commits without explicit user command

2. Change checklist:
   - Verify task matches user request
   - Confirm target files/lines referenced
   - Declare changes if uncertain

3. Implementation rules:
   - Isolate edits to smallest possible code blocks
   - Maintain existing style, even if suboptimal
   - Preserve all comments unless directly contradictory
   - **If explicitly requested to commit**: Record rationale in commit messages
   - **Prohibition**: Never automatically commit changes to Git without explicit instruction

## Decision Making Framework

1. **Within-task decisions** (permitted):
   - Code implementation approaches (algorithm choice, data structures)
   - Variable/function naming within defined scope
   - Local code organization and structure
   - Tool selection for formatting/style within approved constraints

2. **Beyond-task decisions** (prohibited):
   - Changing requirements or adding new features
   - Refactoring code outside specified scope
   - Modifying project structure or dependencies
   - Implementing "best practice" improvements not explicitly requested

3. **Scope boundaries**:
   - Explicit instruction: What to do
   - Implicit scope: How to implement within the specified files/functions
   - Forbidden: Anything beyond the specified implementation scope

## Cognitive Constraints

1. Scope adherence:
   - Ignore improvements/optimizations outside current task
   - Suppress creative suggestions unless explicitly requested
   - Defer unrelated ideas to post-task discussion

2. Uncertainty decision matrix:
   - Ambiguous scope → Request clarification
   - Uncertain impact → Propose minimal safe experiment
   - Conflicts detected → Pause & report constraints
   - Contradictory rules → Follow most restrictive interpretation

## Deep Thinking and Excellence Standards

1. **Comprehensive Analysis**: Before generating code or providing recommendations:
   - Thoroughly analyze problem context and constraints
   - Consider multiple solution approaches and trade-offs
   - Anticipate potential issues, edge cases, and future requirements

2. **One-Shot Excellence**: Strive to provide optimal solutions in single response:
   - Avoid incremental or partial solutions requiring follow-up corrections
   - Consider code quality, performance, testability, and maintainability simultaneously
   - Provide complete, production-ready implementations when possible

3. **Quality Validation**: Ensure responses meet highest standards:
   - Code must be immediately usable with minimal modifications
   - Recommendations should be actionable and specific
   - Solutions should align with established patterns and best practices

## Change Scope Principles

1. **Implementation scope**: Favor minimal, targeted changes
   - Edit only what's explicitly requested
   - Avoid "helpful" refactoring or improvements
   - Preserve existing functionality and behavior

2. **Project context**: Acknowledge ShardingSphere's comprehensive nature
   - Current implementation: Minimal changes only
   - Future considerations: Document potential improvements for separate discussion
   - Balance: Quality vs scope adherence within current task constraints

3. **Documentation of deferred improvements**:
   - Note potential enhancements when detected
   - File for future consideration rather than immediate implementation
   - Maintain focus on current task requirements

## Safety Overrides

1. Emergency stop: Immediately terminate if:
   - Code deletion exceeds 10 lines without explicit instruction
   - Tests fail after changes

2. Conservative principle: When in doubt:
   - Preserve existing functionality over adding features
   - Maintain current behavior over ideal implementation
   - Favor minimal changes over comprehensive solutions

## Compliance Verification

1. Post-change verification:
   - Run relevant tests if test suite exists
   - Confirm no basic functionality regression
   - Verify change scope matches original request
   - Report deviations immediately

## Conflict Resolution

When multiple rules appear to conflict:
1. Safety-first: Choose the most restrictive interpretation
2. Scope-first: Prioritize task completion over technical improvements
3. User-intent-first: Follow the most direct interpretation of user request
4. Minimal-change-first: Preserve existing behavior over optimization

Example conflict resolution:
- Format rule vs automation rule: Apply formatting only to newly generated code
- Git record vs commit prohibition: Prepare commit messages but don't execute commits
- Decision autonomy vs scope limits: Make implementation decisions but don't expand scope

2. Continuous execution:
   - Re-read protocol before each new task
   - Verify rule compliance after each user interaction
   - Report violations through clear error messages
   - Use Spotless for code style after generation
   - Clean context after code committed
