---
name: gen-ut
description: >-
  为 Apache ShardingSphere 的一个或多个目标类生成标准单元测试；
  使用统一规则使目标类达到 100% 类/行/分支覆盖率并通过质量门禁。
---

# 生成单元测试

## 输入约定

必需输入：
- 目标类列表：一个或多个类，优先使用全限定类名。

可选输入：
- 模块名（用于限定 Maven 命令作用范围）。
- 测试类列表（用于定向执行测试）。

缺失输入处理：
- 如果缺少目标类，在进行任何编码工作前先请求提供类列表。
- 如果缺少测试类，先按 `TargetClassName + Test` 约定发现已有相关测试类。
- 如果不存在相关测试类，在解析出的模块测试源码集中创建 `<TargetClassName>Test` 并继续。
- 若无法从相关测试文件或目标类源码文件推导 `<ResolvedTestModules>`，标记阻塞并要求用户明确提供模块范围。

输入阻塞状态映射：
- 输入阻塞（待补充输入）：缺少目标类，或无法确定 `<ResolvedTestModules>` 且用户未补充模块范围。
- 输入补齐并进入执行阶段后，再按 `R9`、`R10`、`R3` 判定完成/阻塞。
- 执行阶段状态映射：死代码阻塞按 `R9-B`；范围外失败按 `R9-C`。

测试类占位符约定：
- `<ResolvedTestClass>` 可以是一个全限定测试类，也可以是逗号分隔列表。
- `<ResolvedTestFileSet>` 是具体可编辑文件列表（在 shell 命令中以空格分隔），包括：
  - 从 `<ResolvedTestClass>` 解析出的测试源码文件；
  - 严格针对这些目标类所需的新测试文件和测试资源。
  - 必须在工作流第 3 步解析为具体路径。
- `<ResolvedTestModules>` 是用于作用域验证命令的逗号分隔 Maven 模块列表。
  - 推导顺序：
    1. 如果提供了显式模块输入，则优先使用；
    2. 否则从相关测试文件（`<ResolvedTestFileSet>`）按最近父级 Maven 模块（`pom.xml`）推导；
    3. 否则从目标类源码文件按最近父级 Maven 模块（`pom.xml`）推导。

术语：
- `相关测试类（Related test class）`：在同一模块测试范围内解析到的既有 `TargetClassName + Test` 类。
- `断言差异（Distinct observable assertion）`：针对不同公开结果或副作用的断言。

## 强制约束（规则单一来源）

- 定义源原则：规则定义仅在“强制约束”部分；其他章节只引用规则号。
- 分层索引：
  - `L1（基础约束层）`：`R1`、`R2`、`R3`
  - `L2（测试设计与实现层）`：`R4`、`R5`、`R6`、`R7`、`R8`
  - `L3（状态判定与阻塞处理层）`：`R9`、`R10`
  - `L4（质量硬门禁层）`：`R11`、`R12`
- `R1（L1-基础约束层）`：遵循 `CODE_OF_CONDUCT.md`。
- `R2（L1-基础约束层）`：使用 JUnit `@Test`；禁止 `@RepeatedTest`。
- `R3（L1-基础约束层）`：改动范围严格限制在由输入目标类解析出的测试范围内。
  - 允许编辑文件仅 `<ResolvedTestFileSet>`。
  - 禁止为修复无关构建/检查/门禁失败修改其他测试文件。
  - 仅当用户在当前轮次明确批准时可扩范围。
- `R4（L2-测试设计与实现层）`：分支路径规则：编码前枚举目标公开方法全部分支路径；默认一个分支/路径仅映射一个测试方法；同分支新增测试必要性按 `R11` 判定。
- `R5（L2-测试设计与实现层）`：每个测试覆盖一个场景，且对目标公开方法最多调用一次；同场景可附加断言。
- `R6（L2-测试设计与实现层）`：被测类可通过 SPI 获取时，默认使用 `TypedSPILoader` 和 `OrderedSPILoader`。
  - “可通过 SPI 获取”：类实现 `TypedSPI`/`DatabaseTypedSPI`，或其实现可被 SPI 加载器发现。
  - 不使用 SPI 实例化时，必须在实现前于计划中记录原因。
- `R7（L2-测试设计与实现层）`：目标类已有相关测试类时必须原位更新：先补缺失覆盖，再按 `R11` 删除或合并覆盖等价测试；仅在无相关测试类时新建。
- `R8（L2-测试设计与实现层）`：存在死代码时，报告类名、文件路径、精确行号和不可达原因。
- `R9（L3-状态判定与阻塞处理层）`：完成判定满足以下之一：
  - `R9-A`：目标类覆盖率（类/行/分支）100%，目标测试类 Surefire 成功执行，且必需质量门禁（`R12` 硬门禁、Checkstyle）通过，并附命令与退出码。
  - `R9-B`：若在“不改生产代码”规则下死代码阻塞 100% 分支覆盖率，按 `R8` 报告并标记阻塞。
  - `R9-C`：若失败发生在 `R3` 范围之外，按 `R10` 报告阻塞证据并标记阻塞。
  - 优先级：先判 `R9-B`，再判 `R9-C`；两者都不适用时才判 `R9-A` 完成。
- `R10（L3-状态判定与阻塞处理层）`：阻塞处理：先在最小测试范围内消除阻塞并重验。
  - 若阻塞位于 `R3` 范围外且当前任务无法安全解决，报告精确阻塞文件/行/命令并请求用户决策。
- `R11（L4-质量硬门禁层）`：测试必要性硬门禁：
  - 不允许无意义测试代码；每行都必须对场景有直接必要性。
  - 某行仅在改变分支选择、协作者行为或可观察断言结果之一时保留。
  - 删除该行后若上述三项均不变，则该行为冗余（含冗余 mock/stub/assertion），必须删除。
  - 除非场景明确需要 stub，否则使用 Mockito 默认返回值。
  - 单次使用局部变量应在调用点内联；仅当该变量用于两个及以上语句的额外 stub/校验或共享断言时保留。
  - 测试方法禁止覆盖等价重复。
  - 每个测试方法必须新增唯一价值：覆盖未覆盖分支/路径，或新增未覆盖断言差异。
  - 覆盖等价重复：同一目标公开方法、同一分支/路径且无断言差异；仅改字面量/mock 名称/夹具值且断言结果不变也属重复。
  - 若删除某测试方法后目标类行/分支覆盖率不变，则必须删除该方法。
  - 工厂/路由回退场景默认每个分支结果仅保留一个代表性方法；仅在有断言差异或用户明确要求额外回归保护时新增。
- `R12（L4-质量硬门禁层）`：布尔断言与硬门禁：
  - 布尔检查必须使用 `assertTrue` / `assertFalse`；禁止以下模式：
  - `assertThat(<boolean expression>, is(true))`
  - `assertThat(<boolean expression>, is(false))`
  - `assertEquals(true, ...)`
  - `assertEquals(false, ...)`
  - 正则单一来源为“验证与命令 / 第 4 项”。
  - 硬门禁扫描必须两次执行：测试实现后（早期快速失败门禁）和最终交付前（最终发布门禁）。
  - 任意命中均视为“未完成”，直到全部修复并复扫干净。

## 执行边界

- 仅处理单元测试任务。
- 不得修改生产代码。
- 仅可修改 `src/test/java` 和 `src/test/resources`。
- 不得编辑生成目录（例如 `target/`）。
- 不得使用破坏性 git 操作（例如 `git reset --hard`、`git checkout --`）。
- 若未提供模块名，推导 `<ResolvedTestModules>` 并保持命令为模块级作用域；除非用户批准，否则禁止仓库级命令。

## 工作流

1. 重新检查 `AGENTS.md` 和 `CODE_OF_CONDUCT.md`。
2. 定位目标类和相关测试类。
3. 解析 `<ResolvedTestClass>`、`<ResolvedTestFileSet>`、`<ResolvedTestModules>`，并记录模块推导证据（`pom.xml` 路径）。
4. 输出分支清单与测试映射（`R4`）；若有额外同分支测试，记录其必要性依据（`R11`）。
5. 按 `R8` 执行死代码分析并记录结果。
6. 按实现规则实现或扩展测试（`R2`、`R4`、`R5`、`R6`、`R7`、`R3`、`R11`、`R12`、执行边界）。
7. 按 `R11` 执行必要性自检，删除冗余 mock/stub/assertion、单次使用局部变量和覆盖等价测试方法。
8. 按 `R12` 执行第一次硬门禁扫描（早期快速失败）并修复所有命中。
9. 按分层顺序运行验证命令（目标测试 -> 目标模块门禁 -> 必要时回退门禁）并迭代。
10. 按 `R12` 执行第二次硬门禁扫描（最终发布门禁）并确保干净。
11. 按 `R9` 判定最终状态，输出规则-证据映射后交付。

## 验证与命令

执行决策树（仅引用规则）：
0. 输入阻塞（待补充输入）优先：按“输入阻塞状态映射”处理，不进入 `R9` 判定。
1. 完成判定只按 `R9` 执行。
2. 失败在 `R3` 范围内：按 `R10` 在 `<ResolvedTestFileSet>` 内修复并重跑。
3. 失败在 `R3` 范围外：按 `R10` 记录证据并按 `R9-C` 标记阻塞。
4. `<FallbackGateModuleFlags>` 仅用于排障，不扩大编辑范围（`R3`）且不改变完成判定（`R9-A`）。

标志预设：
- 提供模块输入时：
  - `<TestModuleFlags>` = `-pl <module>`
  - `<GateModuleFlags>` = `-pl <module>`
- 未提供模块输入时：
  - `<TestModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<GateModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<FallbackGateModuleFlags>` = `<GateModuleFlags> -am`（仅当门禁因跨模块依赖缺失而失败时使用）。

1. 定向单元测试：
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

2. 覆盖率：
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report jacoco:check@jacoco-check -Pcoverage-check
```
若目标模块的 `pom.xml` 未定义 `jacoco-check@jacoco-check` Maven 执行节点，改用：
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report
```
并按以下模板记录人工覆盖率证据（必须包含生成命令与可访问报告路径）：
- `目标类`: `<ClassA>,<ClassB>,...`
- `覆盖率`: `class=<...>, line=<...>, branch=<...>`
- `报告生成命令`: `<生成报告命令>`
- `报告路径`: `<报告路径>`

3. Checkstyle：
```bash
./mvnw <GateModuleFlags> -Pcheck checkstyle:check -DskipTests
```

4. `R12` 硬门禁扫描（必须干净，在工作流第 8 步和第 10 步执行）：
```bash
bash -lc '
BOOLEAN_ASSERTION_BAN_REGEX="assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*,"
if rg -n "$BOOLEAN_ASSERTION_BAN_REGEX" <ResolvedTestFileSet>; then
  echo "[R12] forbidden boolean assertion found"
  exit 1
fi'
```

命令执行规则：
- 记录每条命令及退出码。
- 仅对可重试错误重试一次（例如插件解析临时失败、仓库镜像超时、瞬时网络抖动）；不可重试错误直接按 `R10` 处理。
- 若门禁命令因跨模块依赖缺失失败，使用 `<FallbackGateModuleFlags>` 补救一次，并记录回退原因。
- 若命令失败，按执行决策树（`R10`、`R3`、`R9-C`）记录失败命令与关键报错行。

阻塞报告模板：
- `失败命令`: `<失败命令>`
- `退出码`: `<code>`
- `关键报错行`: `<关键报错行>`
- `是否在 R3 范围内`: `<是/否>`
- `状态`: `<R9-B 或 R9-C>`

5. 交付前最小机检清单（建议顺序）：
```bash
git diff --name-only
```
- 对照 `<ResolvedTestFileSet>` 逐项确认改动文件未越出 `R3`。
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```
- 执行第 4 项 `R12` 硬门禁扫描命令，并记录结果（最终发布门禁）。

## 输出结构

按以下顺序：

1. 目标与约束（映射到 `R1-R12`）
2. 状态（由 `R9` 判定）+ 一行原因
3. 计划与实现（包含模块推导证据、分支映射结果及任何 `R11` 例外理由）
4. 死代码与覆盖率结果（至少包含：目标类覆盖率数值、死代码定位与原因）
5. 验证与质量门禁证据（至少包含：关键命令、退出码、`R12` 扫描结果）
6. 规则-证据映射（`R#→证据`，至少覆盖 `R4`、`R7`、`R8`、`R9`、`R10`、`R3`、`R11`、`R12`）
7. 风险与后续动作

规则-证据映射最小模板：
- `R4`: 分支清单与分支-测试映射。
- `R7`: 相关测试类原位更新或新建决策证据。
- `R8`: 死代码定位（类、路径、行号、原因）。
- `R9`: 最终状态及其判定理由。
- `R10/R3`: 阻塞范围判定与阻塞报告模板。
- `R11`: 必要性自检结果（删除项、保留理由）。
- `R12`: 布尔断言策略与两次硬门禁扫描结果。

## 质量自检

- 规则定义只能出现在“强制约束”部分；其他部分仅可引用规则 ID。
- 最终状态必须满足 `R9`；命令型规则提供命令与退出码证据，非命令型规则提供映射/代码证据。
- `R12` 命令是必需证据；缺失 `R12` 记录或扫描不干净即视为未完成。
- 阻塞状态证据要求按 `R10` 执行。
- 输出中必须包含“规则-证据映射”，且与 `R9` 最终状态一致。

## 维护规则

- 修改任意 `R` 编号后，运行 `rg -n "R[0-9]+" .codex/skills/gen-ut/SKILL.md` 以确保无悬空引用。
- 修改 skill 规则后，验证 `SKILL.md` 与 `agents/openai.yaml` 的触发语义一致。
- 固定最终复审顺序：
  1. 编号一致性检查
  2. 重复短语扫描
  3. `SKILL.md` 与 `agents/openai.yaml` 的语义一致性检查
