# Research: MCP Runtime Pluginization Completion

## Decision 1: 把“真插件化”限定为启动期 classpath 发现，而不是热插拔

- **Decision**: 本次 feature 只承诺 startup-time classpath discovery，不承诺运行时动态装卸 jar。
- **Rationale**:
  - 当前 `RegisteredShardingSphereSPI` 通过 `ServiceLoader.load(serviceInterface)` 装载服务实例，天然更适合进程启动期 discovery。
  - 如果把“真插件化”扩展成热插拔，会额外引入 classloader 生命周期、缓存失效、重复注册清理等一整套新问题。
  - 按 `CODE_OF_CONDUCT.md` 的 Simplicity 与 Abstraction 原则，当前最小正确边界就是启动期插件发现。
- **Alternatives considered**:
  - 新增动态 plugin manager：拒绝，因为远超当前问题边界。
  - 支持运行中扫描新 jar：拒绝，因为会让 registry、capability、session 语义复杂化。

## Decision 2: 把官方默认 feature 集合的责任从 bootstrap 下放到 distribution

- **Decision**: `mcp/bootstrap` 不再通过主依赖声明 encrypt / mask，`distribution/mcp` 显式决定官方 runtime 默认携带哪些 feature jar。
- **Rationale**:
  - bootstrap 的职责应该是“发布已经在 classpath 上的 surface”，不是“决定默认安装哪些 feature”。
  - 官方发行包仍需要保留 encrypt / mask 的默认可用性，因此打包层必须接住这份责任。
  - 这样可以把库职责和产品发行职责分开。
- **Alternatives considered**:
  - 继续保留 bootstrap 主依赖：拒绝，因为 runtime availability 仍由编译期耦合决定。
  - 新增一个 default-feature-pack 模块：暂不采用，因为会再造一层聚合抽象，收益不大。

## Decision 3: 统一使用 `plugins/` 作为外部 feature 与 JDBC driver 扩展入口

- **Decision**: 发行包统一使用 `plugins/` 作为用户扩展目录，并删除 MCP distribution 中的 `ext-lib/` 语义。
- **Rationale**:
  - `plugins/` 比 `ext-lib/` 更准确地表达“运行时扩展 jar”语义。
  - 官方基线 jar 继续留在 `lib/`，`plugins/` 只承接用户追加的 driver 和可选 feature，更容易理解边界。
  - 当前需求已经明确要求一步到位，不保留 MCP distribution 的 `ext-lib/` 兼容目录。
- **Alternatives considered**:
  - 继续保留 `ext-lib/`：拒绝，因为会留下旧语义和新语义并存的过渡设计。
  - 只允许改 `lib/`：拒绝，因为不利于区分官方包内依赖与用户自加扩展。

## Decision 4: 测试要按边界拆成 bootstrap 发现证明 与 distribution 默认包证明

- **Decision**:
  - bootstrap 测试验证 generic discovery；
  - distribution 验证 official default feature bundle；
  - 如需证明真正插件化，再补一个最小 fixture feature。
- **Rationale**:
  - 现在很多 bootstrap 测试直接断言 encrypt / mask 一定存在，这其实把“发行包默认值”错放成了“bootstrap 固有行为”。
  - 边界拆开后，测试失败能更快定位是 discovery 问题还是 packaging 问题。
- **Alternatives considered**:
  - 继续让 bootstrap 测试兜底全部默认 surfaces：拒绝，因为会持续固化错误职责。
  - 只改文档不改测试：拒绝，因为代码边界和测试边界会继续不一致。

## Decision 5: README 的新增 feature 指南必须同步改写

- **Decision**: 文档不再写“让 bootstrap 依赖新 feature”，而是写“让 feature 进入 distribution 默认包或运行时 classpath”。
- **Rationale**:
  - 当前实现与当前文档共同强化了半插件化认知。
  - 如果只改代码不改 README，新 feature 作者仍会被错误路径带偏。
- **Alternatives considered**:
  - 先改代码、文档后补：拒绝，因为这会在 review 与后续协作中制造额外歧义。

## Decision 6: 用一个最小 fixture feature 证明 bootstrap 无需认识具体 feature

- **Decision**: 在测试层新增一个最小 SPI 注册 fixture，而不是只依赖 encrypt / mask 两个正式 feature 来证明插件化。
- **Rationale**:
  - 如果测试只依赖 encrypt / mask，reviewer 仍可能质疑 discovery 是否隐含依赖“官方已知 feature”。
  - 一个最小 fixture 更能证明“任何符合 SPI 契约的 feature”都能被 bootstrap 发布。
- **Alternatives considered**:
  - 只做 subset-loading 测试：不够强，因为仍可能被解释成“只支持官方两个 feature 的子集组合”。
