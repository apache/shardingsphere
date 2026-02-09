---
name: gen-ut
description: >-
  为 Apache ShardingSphere 的一个或多个目标类生成标准单元测试；
  默认以 100% 类/行/分支覆盖率为目标并通过质量门禁；
  对参数化测试执行显式合并分析、适配性筛选与重构优化。
---

# 生成单元测试

## 输入约定

必需输入：
- 目标类列表（建议使用全限定类名）。

可选输入：
- 模块名（限定 Maven 命令作用域）。
- 测试类列表（仅用于定向执行，不限制相关测试类原位更新）。

缺失处理：
- 说明：本节仅描述入口处理，最终判定以 `R7` 与 `R10` 为准。
- 缺少目标类：进入 `R10-INPUT_BLOCKED`。
- 缺少测试类：按 `TargetClassName + Test` 约定自动发现。
- 无相关测试类：在推导出的模块测试源码集中创建 `<TargetClassName>Test`。
- 无法推导 `<ResolvedTestModules>`：进入 `R10-INPUT_BLOCKED` 并请求补充模块范围。

## 术语

- `<ResolvedTestClass>`：一个全限定测试类或逗号分隔测试类列表。
- `<ResolvedTestFileSet>`：可编辑文件集合（shell 命令中空格分隔），仅含相关测试文件与必要测试资源。
- `<ResolvedTestModules>`：用于作用域验证命令的逗号分隔 Maven 模块列表。
- `相关测试类`：同模块测试范围内可解析到的既有 `TargetClassName + Test` 类。
- `断言差异`：对外可观察结果或副作用存在可区分断言。
- `必要性理由标签`：保留项理由的固定格式标签，使用 `KEEP:<id>:<reason>`，记录在交付报告的“实现与优化”部分。

模块推导顺序：
1. 用户显式提供模块时优先使用。
2. 否则从 `<ResolvedTestFileSet>` 对应路径向上查找最近父级 `pom.xml` 推导。
3. 否则从目标类源码路径向上查找最近父级 `pom.xml` 推导。

## 强制约束

- 规范等级：`MUST`（必须）、`SHOULD`（优先）、`MAY`（可选）。
- 定义源原则：所有强制约束仅定义在本节 `R1-R14`；其他章节只允许引用规则号与执行顺序。

- `R1`：`MUST` 遵循 `AGENTS.md` 与 `CODE_OF_CONDUCT.md`；规则解释优先引用 `CODE_OF_CONDUCT.md` 的对应条款与行号证据。

- `R2`：测试类型与命名
  - 非参数化场景 `MUST` 使用 JUnit `@Test`。
  - 数据驱动场景 `MUST` 使用 JUnit `@ParameterizedTest`，展示名 `MUST` 使用 `@ParameterizedTest(name = "{0}")`。
  - `MUST NOT` 使用 `@RepeatedTest`。

- `R3`：改动与执行范围
  - 编辑范围 `MUST` 仅限 `<ResolvedTestFileSet>`。
  - 路径范围 `MUST` 仅限 `src/test/java` 与 `src/test/resources`。
  - `MUST NOT` 修改生产代码与生成目录（如 `target/`）。
  - `MUST NOT` 为修复范围外失败而改动其他测试文件；若需扩范围，`MUST` 由用户当前轮次显式批准。
  - `MUST NOT` 使用破坏性 git 操作（例如 `git reset --hard`、`git checkout --`）。

- `R4`：分支清单与映射
  - 编码前 `MUST` 枚举目标公开方法分支/路径并建立分支-测试映射。
  - 默认一条分支/路径映射一个测试方法。
  - 同分支新增测试是否保留由 `R13` 判定。

- `R5`：测试粒度
  - 每个测试方法 `MUST` 只覆盖一个场景。
  - 每个测试方法对目标公开方法 `MUST` 最多调用一次；同场景允许补充断言。
  - 公共生产方法 `MUST` 采用专用测试方法覆盖。

- `R6`：SPI、Mock 与反射
  - 被测类可通过 SPI 获取时，`MUST` 默认使用 `TypedSPILoader`/`OrderedSPILoader`（或数据库对应加载器）实例化。
  - 不经 SPI 实例化时，`MUST` 在实现前记录原因。
  - 测试依赖 `SHOULD` 默认使用 Mockito mock。
  - 反射访问 `MUST` 使用 `Plugins.getMemberAccessor()`，且仅限字段访问。

- `R7`：相关测试类策略
  - 已存在相关测试类时，`MUST` 原位更新并先补缺失覆盖。
  - 无相关测试类时，`MUST` 新建 `<TargetClassName>Test`。
  - 用户显式提供测试类列表时，仅作为执行过滤输入，`MUST NOT` 取代“相关测试类原位更新”策略。
  - 覆盖等价测试的删除/合并由 `R13` 判定。

- `R8`：参数化优化（默认执行）
  - `MUST` 报告可合并方法集合与待合并数量。
  - 同时满足以下条件的候选视为“高适配参数化”：
    - A. 目标公开方法与分支骨架一致；
    - B. 场景差异主要来自输入数据；
    - C. 断言骨架一致或仅存在已声明的断言差异；
    - D. 参数样本数不少于 3。
  - “已声明的断言差异”指在交付报告中明确记录的差异项。
  - 高适配候选 `SHOULD` 直接参数化重构；若重构显著降低可读性/可诊断性，`MAY` 保留并记录 `必要性理由标签`。
  - 参数构建 `SHOULD` 优先 `Arguments + @MethodSource`；`MAY` 使用 `@CsvSource`/`@EnumSource` 等更清晰方案。
  - `MUST` 为每个候选给出“推荐重构”或“不推荐重构”结论及理由；无候选时 `MUST` 输出“无候选 + 判定理由”。

- `R9`：死代码与覆盖阻塞
  - 死代码阻塞时 `MUST` 报告类名、文件路径、精确行号与不可达原因。
  - 在本 skill 范围内 `MUST NOT` 通过修改生产代码绕过死代码。

- `R10`：状态机与完成判定
  - `R10-INPUT_BLOCKED`：缺少目标类，或无法确定 `<ResolvedTestModules>`。
  - `R10-A`（完成）：同时满足以下条件：
    - 作用域满足 `R3`；
    - 目标测试执行成功且测试数量大于 0；
    - 覆盖率证据满足目标（默认类/行/分支 100%，除非用户明确下调）；
    - Checkstyle、Spotless、`R14` 两次扫描均通过；
    - `R8` 的分析与合规证据完整。
  - `R10-B`（阻塞）：在“不可改生产代码”前提下，死代码阻塞覆盖目标，且证据满足 `R9`。
  - `R10-C`（阻塞）：失败发生在 `R3` 范围外，且证据满足 `R11`。
  - 判定优先级：`R10-INPUT_BLOCKED` > `R10-B` > `R10-C` > `R10-A`。

- `R11`：失败处理
  - 失败位于 `R3` 范围内：`MUST` 在 `<ResolvedTestFileSet>` 内修复并重跑最小验证。
  - 失败位于 `R3` 范围外：`MUST` 记录阻塞证据（失败命令、退出码、关键报错行、阻塞文件/行）并请求用户决策。
  - 最小验证定义为“目标测试命令 + 一次 `R14` 硬门禁扫描命令”。
  - 最小验证仅用于范围内修复迭代，`MUST NOT` 取代 `R10-A` 的最终门禁验证。
  - 可重试错误（插件解析临时失败、镜像超时、瞬时网络抖动）`MAY` 重试，最多 2 次。

- `R12`：覆盖率 100% 优化模式
  - 若目标类覆盖率证据已为 100%，`MUST` 跳过覆盖率补齐，仅执行 `R8` 参数化优化。
  - 覆盖率判定 `MUST` 可复现（命令 + 报告路径）。
  - 该模式下 `MAY` 省略 `R4` 分支映射输出，但 `MUST` 在规则映射中标注 `R4=N/A（由 R12 触发）` 并附覆盖率证据。

- `R13`：测试必要性裁剪
  - 裁剪顺序 `MUST` 固定为“客观裁剪 -> 例外保留复核”。
  - 客观裁剪阶段 `MUST` 先删除覆盖等价测试并统一复验覆盖率，再删除不影响分支选择/协作对象交互行为（如调用次数、参数）/可观察断言的冗余 mock/stub/assertion 与单次使用局部变量；若保留能显著提升可读性，`MAY` 保留并打 `必要性理由标签`。
  - 每个保留项 `MUST` 携带 `KEEP:<id>:<reason>` 格式标签，并记录在交付报告中；无标签按冗余处理。
  - 每个测试方法 `MUST` 具备唯一价值：覆盖新分支/路径，或新增断言差异。
  - 若删除某测试方法后行/分支覆盖率不变且无断言差异，`MUST` 删除。
  - 除非场景需要，`SHOULD` 使用 Mockito 默认返回值而非额外 stub。

- `R14`：布尔断言硬门禁
  - 布尔断言 `MUST` 使用 `assertTrue`/`assertFalse`。
  - `MUST NOT` 使用：
    - `assertThat(<boolean expression>, is(true|false))`
    - `assertEquals(true|false, ...)`
  - `MUST` 在实现后与交付前各执行一次硬门禁扫描；任一命中均视为未完成。

## 工作流

1. 读取 `AGENTS.md` 与 `CODE_OF_CONDUCT.md`，记录本轮硬约束（`R1`）。
2. 解析目标类、相关测试类与输入阻塞状态（`R10-INPUT_BLOCKED`）。
3. 解析 `<ResolvedTestClass>`、`<ResolvedTestFileSet>`、`<ResolvedTestModules>` 并记录 `pom.xml` 证据（`R3`）。
4. 判断是否命中 `R12`；未命中则输出 `R4` 分支映射。
5. 执行 `R8` 参数化优化分析并落地必要重构。
6. 执行 `R9` 死代码检查并记录证据。
7. 按 `R2-R7` 完成测试实现或扩展。
8. 按 `R13` 进行必要性裁剪与覆盖率复验。
9. 执行验证命令并按 `R11` 处理失败；执行两次 `R14` 扫描。
10. 按 `R10` 判定状态并输出规则-证据映射。

## 验证与命令

标志预设：
- 提供模块输入：
  - `<TestModuleFlags>` = `-pl <module>`
  - `<GateModuleFlags>` = `-pl <module>`
- 未提供模块输入：
  - `<TestModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<GateModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<FallbackGateModuleFlags>` = `<GateModuleFlags> -am`（仅用于跨模块依赖缺失排障，不改变 `R3` 与 `R10`）。

1. 目标测试：
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -DfailIfNoTests=true -Dsurefire.failIfNoSpecifiedTests=true test
```

2. 覆盖率：
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report jacoco:check@jacoco-check -Pcoverage-check
```
若模块未定义 `jacoco-check@jacoco-check`：
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report
```

3. Checkstyle：
```bash
./mvnw <GateModuleFlags> -Pcheck checkstyle:check -DskipTests
```

4. Spotless：
```bash
./mvnw <GateModuleFlags> -Pcheck spotless:check -DskipTests
```
若出现跨模块依赖缺失，可改用 `<FallbackGateModuleFlags>` 对上述 gate 命令重跑一次，并记录触发原因与命令结果。

5. `R8` 参数化合规扫描（注解块解析）：
```bash
bash -lc '
python3 - <ResolvedTestFileSet> <<'"'"'PY'"'"'
import re
import sys
from pathlib import Path

name_pattern = re.compile(r'name\s*=\s*"\{0\}"')
token = "@ParameterizedTest"

def collect_violations(path):
    source = Path(path).read_text(encoding="utf-8")
    violations = []
    pos = 0
    while True:
        token_pos = source.find(token, pos)
        if token_pos < 0:
            return violations
        line = source.count("\n", 0, token_pos) + 1
        cursor = token_pos + len(token)
        while cursor < len(source) and source[cursor].isspace():
            cursor += 1
        if cursor >= len(source) or source[cursor] != "(":
            violations.append(f"{path}:{line}")
            pos = token_pos + len(token)
            continue
        depth = 1
        end = cursor + 1
        while end < len(source) and depth:
            if source[end] == "(":
                depth += 1
            elif source[end] == ")":
                depth -= 1
            end += 1
        if depth:
            violations.append(f"{path}:{line}")
            return violations
        if not name_pattern.search(source[cursor + 1:end - 1]):
            violations.append(f"{path}:{line}")
        pos = end

violations = []
for each in sys.argv[1:]:
    if each.endswith(".java"):
        violations.extend(collect_violations(each))

if violations:
    print("[R8] @ParameterizedTest must use name = \"{0}\"")
    for each in violations:
        print(each)
    sys.exit(1)
PY
'
```

6. `R14` 硬门禁扫描：
```bash
bash -lc '
BOOLEAN_ASSERTION_BAN_REGEX="assertThat\s*\((?s:.*?)is\s*\(\s*(true|false)\s*\)\s*\)|assertEquals\s*\(\s*(true|false)\s*,"
if rg -n -U --pcre2 "$BOOLEAN_ASSERTION_BAN_REGEX" <ResolvedTestFileSet>; then
  echo "[R14] forbidden boolean assertion found"
  exit 1
fi'
```
7. 范围校验：
```bash
git diff --name-only
```
