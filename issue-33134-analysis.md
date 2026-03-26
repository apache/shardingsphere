# 问题理解

这条 issue 我会把它定性为一个 `Bug`，而且不是 `readwrite-splitting` 规则本身的语义问题，而是 **Cluster + ETCD 模式下的元数据删除事件分发错误**。

- `OBS-1` Issue #33134 明确给出了拓扑和现象：`5.5.0`、`Proxy`、`cluster`、`etcd`，执行 `drop readwrite_splitting rule group0;` 后，`show readwrite_splitting rules` 为空，但再次 `create` 同名规则时报 `[group0] already exists in storage unit`。  
  Source: <https://github.com/apache/shardingsphere/issues/33134>
- `OBS-2` 本地校验逻辑里，这个报错来自 [ReadwriteSplittingRuleStatementChecker.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/distsql/handler/src/main/java/org/apache/shardingsphere/readwritesplitting/distsql/handler/checker/ReadwriteSplittingRuleStatementChecker.java)，其中会基于当前内存中的已构建规则判断规则名/存储单元是否“已存在”。
- `OBS-3` `readwrite-splitting` 的内存规则项删除逻辑本身是存在的：  
  [ReadwriteSplittingDataSourceChangedProcessor.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/changed/ReadwriteSplittingDataSourceChangedProcessor.java) 的 `dropRuleItemConfiguration` 会按名称移除 `dataSourceGroups`。  
- `OBS-4` Cluster/Standalone 两条路径最终都会调用 [DatabaseRuleItemManager.java](D:/Coding/Java/shardingsphere/mode/core/src/main/java/org/apache/shardingsphere/mode/metadata/manager/rule/DatabaseRuleItemManager.java) 的 `drop(...)`，只要删除事件能被正确识别，`group0` 就应从当前 `ReadwriteSplittingRuleConfiguration` 中去掉。
- `OBS-5` `readwrite-splitting` 在规则节点里是一个“命名规则项”，路径形态是 `.../rules/readwrite_splitting/data_source_groups/<groupName>`，这个映射由  
  [YamlReadwriteSplittingRuleConfiguration.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/config/YamlReadwriteSplittingRuleConfiguration.java)  
  和测试 [ReadwriteSplittingConfigurationYamlRuleNodeTupleSwapperEngineIT.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/core/src/test/java/org/apache/shardingsphere/readwritesplitting/yaml/ReadwriteSplittingConfigurationYamlRuleNodeTupleSwapperEngineIT.java) 证明。
- `OBS-6` Cluster ETCD 仓储的事件类型判定在 [EtcdRepository.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java)：

```java
private Type getEventChangedType(final WatchEvent event) {
    if (1 == event.getKeyValue().getVersion()) {
        return Type.ADDED;
    }
    switch (event.getEventType()) {
        case PUT:
            return Type.UPDATED;
        case DELETE:
            return Type.DELETED;
        default:
            return Type.IGNORED;
    }
}
```

- `OBS-7` ETCD 的 `DELETE` 事件在这里会先经过 `version == 1` 判断；而规则节点中的很多 key 首次创建后直到删除前都可能一直是 version 1，尤其是 `active_version`、中间父节点、首次创建后未更新的条目。
- `OBS-8` Cluster 监听器 [DatabaseMetaDataChangedListener.java](D:/Coding/Java/shardingsphere/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/dispatch/listener/type/DatabaseMetaDataChangedListener.java) 对 `ADDED/UPDATED` 事件会先做 `ActiveVersionChecker.checkSame(...)` 校验；如果一个真实删除事件被误判成 `ADDED`，它通常不会进入真正的 drop 刷新分支。

# 根因

根因是 **ETCD 仓储把一部分 `DELETE` 事件错误地识别成了 `ADDED`**。

- `INF-1` 由 `OBS-6` + `OBS-7` 可推断：当 ETCD watch 收到 `DELETE` 事件且被删 key 的 version 恰好是 `1` 时，`getEventChangedType(...)` 会直接返回 `Type.ADDED`，根本不会走到 `DELETE` 分支。
- `INF-2` 由 `OBS-8` 可推断：这些被误判的删除事件不会触发 `DatabaseRuleItemManager.drop(...)`，因此内存中的 `ReadwriteSplittingRuleConfiguration` 不会删掉 `group0`。
- `INF-3` 由 `OBS-2` + `OBS-3` + `OBS-4` 可推断：再次执行 `create readwrite_splitting rule group0 ...` 时，校验器仍然看见内存里旧规则还在，于是抛出 “already exists in storage unit”。
- `INF-4` 这个问题并不局限于 `readwrite-splitting`。凡是 **Cluster + ETCD + 依赖删除事件刷新内存规则/元数据** 的路径，只要删除的是 version 1 的 key，都有同类风险。  
  依据：`OBS-6` 的错误发生在仓储公共层，而不是 readwrite-splitting 私有实现里。

# 问题分析

1. 这不是 quick start 层面能发现的问题。  
   quick start 只覆盖产品启动与使用，不覆盖 cluster repository 的事件语义。

2. 这也不是 parser、DistSQL 解析或 readwrite-splitting checker 的主责问题。  
   `drop` 语句已经执行成功，`show` 结果也能变空，说明持久化层删除动作本身大概率已经发生；真正没同步的是运行中内存上下文。

3. 本地规则删除代码是通的。  
   [ReadwriteSplittingDataSourceChangedProcessor.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/changed/ReadwriteSplittingDataSourceChangedProcessor.java) 的 `dropRuleItemConfiguration` 很直接，没有明显缺陷。

4. 问题更像 ETCD 与 ZooKeeper 行为不一致。  
   ZooKeeper 仓储的删除分发在 [ZookeeperRepository.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/zookeeper/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/ZookeeperRepository.java) 里是直接把删除映射为 `Type.DELETED`，没有类似“version==1 优先返回 ADDED”的逻辑。  
   这也解释了为什么 issue 被打上 `mode: cluster`，但从代码看它更精确地说是 `cluster + etcd`。

5. 现有 ETCD 单测没有真正校验事件类型。  
   [EtcdRepositoryTest.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/etcd/src/test/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepositoryTest.java) 里有 `assertWatchDelete`，但只验证了 `watch()` 被调用，没有断言 listener 收到的 `DataChangedEvent.Type` 一定是 `DELETED`。这就是这个 bug 能漏过去的直接原因之一。

# 代码级设计建议

建议按最小安全改动修复，优先改公共仓储层，再补回归测试。

1. 修复事件类型判定顺序  
   文件：
   [EtcdRepository.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java)

   建议把 `getEventChangedType(...)` 改成“先看事件类型，再看 version”：
   - `DELETE` 必须永远返回 `Type.DELETED`
   - `PUT` 再根据 `version == 1` 区分 `ADDED` / `UPDATED`

   逻辑上应接近：

```java
private Type getEventChangedType(final WatchEvent event) {
    switch (event.getEventType()) {
        case DELETE:
            return Type.DELETED;
        case PUT:
            return 1 == event.getKeyValue().getVersion() ? Type.ADDED : Type.UPDATED;
        default:
            return Type.IGNORED;
    }
}
```

2. 给 ETCD 仓储补精确单测  
   文件：
   [EtcdRepositoryTest.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/etcd/src/test/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepositoryTest.java)

   至少补两类用例：
   - `DELETE + version=1 -> DELETED`
   - `PUT + version=1 -> ADDED`
   - 可选：`PUT + version>1 -> UPDATED`

   这一步最关键，因为它能把这个 bug 固化成仓储层回归测试。

3. 补一条 cluster 分发层的防回归测试  
   优先文件：
   [NamedRuleItemConfigurationChangedHandlerTest.java](D:/Coding/Java/shardingsphere/mode/type/cluster/core/src/test/java/org/apache/shardingsphere/mode/manager/cluster/dispatch/handler/database/rule/type/NamedRuleItemConfigurationChangedHandlerTest.java)

   可以新增一个更贴近真实路径的 case，例如：
   - 路径：`/metadata/foo_db/rules/readwrite_splitting/data_source_groups/group0`
   - 事件：`DELETED`
   - 断言：`DatabaseRuleItemManager.drop(new DatabaseRuleNodePath(... "data_source_groups", "group0"))` 被调用

4. 如果想把问题彻底锁死到业务层，再加 readwrite-splitting 侧回归  
   位置可考虑：
   [ReadwriteSplittingDataSourceChangedProcessorTest.java](D:/Coding/Java/shardingsphere/features/readwrite-splitting/core/src/test/java/org/apache/shardingsphere/readwritesplitting/rule/changed/ReadwriteSplittingDataSourceChangedProcessorTest.java)
   
   但这一层更多是补信心，不是主修复点，因为根因不在 processor。

5. 回归范围
   - Affected modules:
     - `mode/type/cluster/repository/provider/etcd`
     - `mode/type/cluster/core`
     - 间接受影响：所有依赖 cluster delete 事件做内存刷新的规则模块
   - Compatibility impact:
     - 行为修复，仅纠正错误事件类型
   - Required test scope:
     - unit: ETCD repository event type mapping
     - unit: cluster rule item delete dispatch
     - optional integration: cluster + etcd rule drop/create replay
   - Backward-compatibility notes:
     - 不改 API / SPI / 配置格式
     - 只是把错误的 delete 事件语义修正为正确语义
   - Rollback hint:
     - 若回滚该修复，ETCD cluster 下同类 drop 后内存残留问题会继续存在

# 问题结论

- 结论：这是一个 **真实可复现的 Bug**，且根因在 `ETCD cluster repository` 的删除事件类型判定，不在 `readwrite-splitting` 规则删除实现本身。`OBS-1`, `OBS-3`, `OBS-6`, `OBS-8`, `INF-1`, `INF-2`
- 对当前项目的正确修复方式：优先修改 [EtcdRepository.java](D:/Coding/Java/shardingsphere/mode/type/cluster/repository/provider/etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java) 中 `getEventChangedType(...)` 的判定顺序，并补上仓储层与 cluster 分发层测试。`OBS-6`, `INF-1`
- 从影响面看，这不是单一 SQL 的问题，而是 **Cluster + ETCD 下所有依赖删除事件进行元数据/规则内存清理的路径** 都可能受影响。`INF-4`

Evidence Confidence: High  
Severity: S2  
Impact Scope: cross-module, cluster-mode, ETCD-backed metadata delete propagation  
Topology: Proxy + Cluster + ETCD  
Issue Type: Bug  
Recommended Labels: `type: bug`, `mode: cluster`, `status: volunteer wanted`  
Next Action: 优先提交一个最小修复 PR，修改 ETCD 删除事件判定，并补 ETCD 仓储事件类型回归测试

Compatibility: Behavior yes / Config no change / API-SPI no change / SQL no change  
Regression Scope: `mode/type/cluster/repository/provider/etcd`, `mode/type/cluster/core`, all cluster delete-event-driven rule refresh paths

如果你愿意，我下一步可以直接继续做实现，把这个修复补到代码里，并顺手把对应单元测试一起加上。
