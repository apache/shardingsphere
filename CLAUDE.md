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

1. Follow CODE_OF_CONDUCT.md
2. Generate minimal essential code only
3. Prioritize readability & scalability, avoid over-engineering

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
   - Prohibit automatic formatting of entire files/projects
   - Local consistency: Match adjacent code style

## Operational Procedures

1. **Direct Code Generation**: Generate final code & call tools directly without user approval
   - Apply formatting tools automatically when needed
   - Make independent decisions within task scope
   - **IMPORTANT**: Automated code generation & tool calls permitted, automatic Git commits strictly prohibited

2. Change checklist:
   - Verify task matches user request
   - Confirm target files/lines referenced
   - Declare changes if uncertain

3. Implementation rules:
   - Isolate edits to smallest possible code blocks
   - Maintain existing style, even if suboptimal
   - Preserve all comments unless directly contradictory
   - Record rationale in commit messages when using Git
   - **Prohibition**: Never automatically commit changes to Git without explicit instruction

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

2. Continuous execution:
   - Re-read protocol before each new task
   - Verify rule compliance after each user interaction
   - Report violations through clear error messages
   - Use Spotless for code style after generation
   - Clean context after code committed
