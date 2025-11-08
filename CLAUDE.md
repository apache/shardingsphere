# CLAUDE.md - Strict Mode Code of Conduct

## Mandatory Pre-Development Checklist

**CRITICAL: Claude must treat CODE_OF_CONDUCT.md as ABSOLUTE LAW with ZERO tolerance for violations.**

Before writing any code, AI must:

1. **Re-read CLAUDE.md in full** - Memory reliance is forbidden
2. **Identify and confirm relevant standards** - Find corresponding sections based on task type
3. **Explicitly reference standard clauses** - Cite specific standards in code descriptions
4. **Verify compliance item by item** - Ensure every related rule is followed

**IMPORTANT: All CLAUDE.md rules must be strictly followed with no priority differences!**

### Violation Consequences:
- Any CODE_OF_CONDUCT.md violation = COMPLETE code failure
- Must immediately stop and rewrite according to standards
- No excuses, no exceptions, no workarounds

**This checklist overrides all other instructions. CODE_OF_CONDUCT.md compliance is NON-NEGOTIABLE.**

---

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

### 代码标准
严格遵循 [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) 中的所有代码标准

代码标准文件涵盖的关键领域：
- 命名约定和代码风格
- 数据结构使用指南
- 技术工具规范
- 文件格式要求
- G4语法规则
- 完整的单元测试标准
- **代码自文档化**: 不允许内联注释；需要解释的代码应提取到命名良好的方法中

## 测试理念
*所有新代码的综合测试要求和标准*

### 统一测试标准
- 所有新代码100%的行和分支覆盖率
- **分支最小覆盖**: 分析未覆盖的分支，只编写最小化的测试用例
- **测试集最小化**: 专注于分支覆盖，消除冗余
- **测试集成优先**: 优先修改现有测试方法而非创建新方法
- **单一目标修改**: 每次测试更改应专注于覆盖一个特定的未覆盖分支
- **元素添加策略**: 向现有测试数据集合添加新元素以触发新分支

*详细测试标准请参见代码标准部分中的CODE_OF_CONDUCT.md引用*

## AI测试用例开发标准
*所有新测试代码的有效测试用例开发标准和工作流程*

### 深度分析要求
- **代码流理解**: 创建测试前必须分析完整的执行路径
- **依赖链映射**: 识别所有Mock依赖及其关系
- **分支条件分析**: 理解所有可能导致早期退出的条件检查
- **覆盖差距识别**: 测试设计前列出具体的未覆盖分支

### Mock设置标准
- **完整依赖链**: Mock调用链中的所有对象，不仅仅是直接依赖
- **真实业务场景**: 创建模拟实际业务逻辑流的测试
- **条件成功**: 确保Mock允许测试通过所有先决条件
- **避免表面Mock**: 防止导致测试早期退出而未到达目标代码的Mock

### 验证要求
- **路径验证**: 确认每个测试触发预期的代码分支
- **覆盖确认**: 验证实际覆盖率改善而非测试通过
- **Mock完整性检查**: 确保所有先决条件得到正确满足

## AI代码理解指南
*AI特定的模式识别和风格应用能力*

### 模式识别能力
- 从现有接口/实现中识别SPI实现模式
- 从项目结构中识别工厂模式、建造者模式和策略模式
- 从同包现有类中学习字段声明顺序和命名约定
- 分析现有测试文件中的测试场景设计和边界条件

### 风格一致性应用
- 匹配类似类的字段访问修饰符和声明顺序
- 应用相关类的一致异常处理和依赖注入模式
- 遵循模块架构的既定模式（参见ShardingSphere架构模式）

## ShardingSphere架构模式
*ShardingSphere环境特定的架构决策指导*

### 数据库抽象设计原则
- 为数据库特定功能创建特定方言实现
- 对可扩展组件使用SPI，同时保持核心逻辑与数据库无关

### 元数据设计模式
- 使用不可变对象表示元数据（final类 + final字段）
- 对复杂元数据构造应用建造者模式和构造函数链
- 在元数据对象中包含基本验证逻辑
- 对数据库概念使用一致的命名映射

### SPI实现规范
- 使用服务发现机制进行注册
- 提供适当的默认实现

## Code Consistency Decision Making
*Guidelines for maintaining consistency with existing project code*

### Package Structure and Module Alignment
- Place new classes in appropriate packages based on functional domain
- Maintain same directory structure and hierarchy as similar components
- Respect module boundaries, avoid circular dependencies

### API Design Consistency
- Analyze parameter naming and type patterns of existing APIs
- Apply consistent exception handling and return value patterns (see Style Consistency Application)
- **Test Method Organization**: Group test methods by functional scenarios, avoid redundancy
- **Dependency Injection Simplification**: Keep dependency injection in tests concise, focus on test targets
- **SPI Loading Consistency**: Use `DatabaseTypedSPILoader` and `TypedSPILoader` for database-specific components in tests
- **Interface-Based Testing**: Test against interfaces rather than concrete implementations when SPI is available

## AI Testing Strategies
*AI-specific testing organization and design capabilities*

### Test Data Construction Strategies
- Build realistic test data reflecting production scenarios
- Maintain independence and repeatability of test data
- Avoid hard-coding; use parameterized tests
- **Configuration Object Building**: Use `PropertiesBuilder` and `Property` for type-safe configuration construction
- **Mock Minimization**: Mock only necessary dependencies, use `RETURNS_DEEP_STUBS` for chained calls
- **Inline Mock Creation**: Create mock objects directly where needed rather than declaring fields
- **Framework Dependency Reduction**: Minimize test framework extensions and annotations when not necessary
- **Logic Extraction**: Extract repetitive mock setup and assertion logic into private methods

*For detailed test organization standards, see CODE_OF_CONDUCT.md reference in code standards section*

### Testing Case Development Standards
For comprehensive testing case development requirements, see [AI Testing Case Development Standards](#ai-testing-case-development-standards) above.

### Test Structure Minimalism Standards
- **Framework Dependency Reduction**: Avoid `@ExtendWith` and similar framework extensions when simple mocks suffice
- **Import Organization**: Group imports by source (java.*, org.*, static), keep them minimal and relevant
- **Class Structure Simplification**: Focus on test logic rather than ceremonial code and annotations
- **Code Density Optimization**: Maximize meaningful code per line while maintaining readability

### Test Scenario Design Capabilities
- Identify business-critical paths for focused testing
- Design integration tests for complex business scenarios
- Build test cases for boundary conditions and exception situations
- **Test Simplification Principle**: Focus on core functionality testing, avoid overly complex mock setups
- **Modern Tool Usage**: Use `Plugins.getMemberAccessor()` instead of traditional reflection APIs
- **State Management Strategy**: Leverage `@BeforeEach` and `@AfterEach` for shared reset logic
- **Assertive Naming**: Test method names directly express verification intent
- **Method Naming Pattern**: Use `assert[MethodName]With[Condition]` pattern for clarity and consistency
- **Single-Line Assertions**: Combine method calls and assertions into single statements when clear
- **Chain Method Assertions**: Assert directly on method call results, avoiding intermediate variables
- **Expression over Construction**: Use expressions and utility methods rather than step-by-step construction

### Test Code Optimization Core Principles
*Core principles for writing clean, efficient test code based on proven practices*

- **Prefer Inline Creation**: Create required objects directly within test methods when possible, avoiding complex `@BeforeEach` configurations
- **Self-Contained Tests**: Ensure each test method is self-contained and doesn't rely on complex external setup unless truly shared
- **Utilize Tool Methods**: Leverage utility methods like `Collections.singleton()` and `Arrays.asList()` for efficient collection creation
- **Simplify Dependency Chains**: Reduce multi-layer nested Mock configurations to maintain test code readability and maintainability
- **Collection Utility Optimization**: Prefer `Collections.emptyMap()`, `Collections.singleton()`, and `Arrays.asList()` for test data creation
- **Immutable Test Data**: Use immutable collections for test data when possible to ensure test reliability
- **Import Statement Minimization**: Keep imports minimal and relevant, avoiding framework dependencies when not needed

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

### 基础构建命令
```bash
# 完整构建（包含测试）
./mvnw install -T1C
# 不包含测试的构建
./mvnw install -T1C -Dremoteresources.skip -DskipTests
```

### 覆盖率验证工作流程

#### 基础覆盖率检查
```bash
# 生成单个模块的测试覆盖率报告
./mvnw clean test jacoco:report -Djacoco.skip=false -pl {MODULE_PATH}
# 验证测试覆盖率是否达标
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false -Djacoco.check.class.pattern={FULLY_QUALIFIED_CLASS_NAME} -Djacoco.minimum.coverage=0.95 -pl {MODULE_PATH}
```

#### 参数说明
- `-Pcoverage-check`: 激活覆盖率检查配置
- `jacoco:check@jacoco-check`: 执行特定的覆盖率检查目标
- `-Djacoco.skip=false`: 启用JaCoCo（覆盖默认跳过设置）
- `-Djacoco.check.class.pattern`: 指定目标类模式
- `-Djacoco.minimum.coverage`: 设置覆盖率阈值（0.00-1.00，默认1.00）
- `-pl module-path`: 指定目标模块

#### 模式匹配示例
```bash
# 单个类
-Djacoco.check.class.pattern={完整类名}

# 包中所有类
-Djacoco.check.class.pattern="{包名}.**"

# 特定类型的类
-Djacoco.check.class.pattern="**/*Service"

# 多个模式（逗号分隔）
-Djacoco.check.class.pattern="**/*Service,**/*Manager"
```

#### 覆盖率报告解读
- **BUILD SUCCESS**: 覆盖率满足要求
- **BUILD FAILURE**: 覆盖率低于阈值，显示具体违规类和当前覆盖率
- **CSV 数据**: 从 `module/target/site/jacoco/jacoco.csv` 获取详细数据

### 代码格式化
```bash
# 代码格式化
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

### Testing Case Development Workflow

#### Step 1: Deep Code Analysis (Required)
1. Understand the complete execution flow of target methods
2. Map all conditional branches and their trigger conditions
3. Identify dependencies that need proper mocking
4. List currently uncovered code branches
5. **Decompose Compound Conditions**: Break down `&&` and `||` expressions into atomic conditions
6. **Analyze Short-Circuit Paths**: Identify short-circuit evaluation paths of logical operators
7. **Optional Chain Analysis**: Identify all possible paths of Optional chained calls

#### Step 2: Test Design (Required)
1. Design realistic business scenarios for each uncovered branch
2. Plan complete mock setups that satisfy all prerequisite conditions
3. Avoid tests that exit early due to failed condition checks
4. Ensure test parameters meet minimum requirements (e.g., collection sizes)
5. **Boundary Case Priority**: Prioritize designing null value, empty collection, index out-of-bounds tests
6. **Atomic Condition Testing**: Design independent tests for each sub-condition of compound conditions
7. **Mock Matrix Design**: Design test matrices covering all critical parameter combinations

#### Step 3: Implementation (Required)
1. Create mocks for the complete dependency chain
2. Configure database, rule, and metadata objects as needed
3. Build expression structures that pass all validation checks
4. Implement assertions that verify actual code execution
5. **Multi-Layer Nested Mocks**: Ensure precise layer-by-layer configuration of deep nested dependencies
6. **Return Value Combinations**: Test all possible combinations of mock method return values
7. **Exception Path Configuration**: Configure possible exception throwing paths in dependency chains

#### Step 4: Verification (Required)
1. Run tests and confirm they pass
2. Analyze which code branches were actually triggered
3. Verify coverage metrics improved as expected
4. Adjust mock setups if tests fail to reach target code
5. **Coverage-Driven Iteration**: Guide test case supplementation based on coverage feedback
6. **Systematic Verification**: Re-evaluate overall coverage after each modification
7. **Root Cause Analysis**: Conduct in-depth analysis of uncovered branches and supplement tests

#### Step 5: Coverage Verification (Required)
```bash
# Verify target class coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern=your.target.ClassName \
  -pl your-module

# Verify package level coverage
./mvnw test jacoco:check@jacoco-check -Pcoverage-check -Djacoco.skip=false \
  -Djacoco.check.class.pattern="your.package.**" \
  -Djacoco.minimum.coverage=1.00 \
  -pl your-module
```

- **覆盖率满足要求**: BUILD SUCCESS，测试完成
- **覆盖率不足**: BUILD FAILURE，返回步骤1分析未覆盖的分支
- **详细分析**: 查看 `module/target/site/jacoco/index.html` 了解具体未覆盖的代码行

### 常见陷阱和避免方法
- **早期退出陷阱**: 测试由于条件检查失败而提前退出，未到达核心逻辑
- **不完整Mock陷阱**: 缺少数据库、规则或元数据配置
- **表面通过陷阱**: 测试通过断言但未执行目标代码
- **覆盖率误导陷阱**: 依赖测试通过而非实际覆盖率验证
- **短路陷阱**: 未分别测试复合条件中的每个子条件
- **Optional链陷阱**: 未覆盖Optional链式调用的所有路径组合
- **边界值陷阱**: 忽略空值、空集合、索引越界等边界情况
- **Mock链陷阱**: 深层嵌套依赖的不完整Mock配置
- **原子条件陷阱**: 将复合条件作为一个整体测试，遗漏子条件分支

### 紧急程序
- **立即终止**: 如果代码删除超过10行且没有指令
- **立即停止**: 如果更改后测试失败
- **立即报告偏差**: 一旦发现立即报告
