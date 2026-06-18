+++
title = "How does ShardingSphere’s Show processlist & Kill Work?"
weight = 75
chapter = true 
+++

For those of you who often use databases, you may wonder:

1. How do I check what SQL is currently being executed by the database, and in what state?

2. How do I terminate abnormal SQL? For instance, if a `SELECT` statement used to query a table with massive data does not carry query conditions, it would drag down the performance of the entire database. This may push to want to terminate this abnormal SQL.

In response to the above issues, [Apache ShardingSphere](https://shardingsphere.apache.org/) introduced functions such as `Show processlist` and `Kill <processID>`.

![img](https://shardingsphere.apache.org/blog/img/2022_09_22_How_does_ShardingSphere’s_Show_processlist_&_Kill_Work1.png)

# 1. Introduction

`Show processlist`: this command can display the list of SQL currently being executed by ShardingSphere and the execution progress of each SQL. If ShardingSphere is deployed in cluster mode, the `Show processlist` function aggregates the SQL running for all Proxy instances in the cluster and then displays the result, so you can always see all the SQL running at that moment.

```mysql
mysql> show processlist \G;
*************************** 1. row ***************************
     Id: 82a67f254959e0a0807a00f3cd695d87
   User: root
   Host: 10.200.79.156
     db: root
Command: Execute
   Time: 19
  State: Executing 0/1
   Info: update t_order set version = 456
1 row in set (0.24 sec)
```

`Kill <processID>`: This command is implemented based on `Show processlist` and can terminate the running SQL listed in the `Show processlist`.

```mysql
mysql> kill 82a67f254959e0a0807a00f3cd695d87;
Query OK, 0 rows affected (0.17 sec)
```

# 2. How do they work?

Now that you understand the functions of `Show processlist` and `Kill <processID>`, let's see how the two commands work. As the working principle behind `Kill <processID>` is similar to that of `Show processlist`, we'll focus on the interpretation of `Show processlist`.

## 2.1 How is SQL saved and destroyed?

Each SQL executed in ShardingSphere will generate an `ExecutionGroupContext` object. The object contains all the information about this SQL, among which there is an `processID` field to ensure its uniqueness.

When ShardingSphere receives a SQL command, the `GovernanceExecuteProcessReporter# report` is called to store `ExecutionGroupContext` information into the cache of `ConcurrentHashMap `(currently only DML and DDL statements of MySQL are supported; other types of databases will be supported in later versions. Query statements are also classified into DML).

```java
public final class GovernanceExecuteProcessReporter implements ExecuteProcessReporter {
    
    @Override
    public void report(final QueryContext queryContext, final ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext,
                       final ExecuteProcessConstants constants, final EventBusContext eventBusContext) {
        ExecuteProcessContext process = new ExecuteProcessContext(queryContext.getSql(), executionGroupContext, constants);
        ShowProcessListManager.getInstance().putProcessContext(process.getProcessID(), process);
        ShowProcessListManager.getInstance().putProcessStatement(process.getProcessID(), process.getProcessStatements());
    }
}@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowProcessListManager {
    
    private static final ShowProcessListManager INSTANCE = new ShowProcessListManager();
    
    @Getter
    private final Map<String, ExecuteProcessContext> processes = new ConcurrentHashMap<>();
    
    @Getter
    private final Map<String, Collection<Statement>> processStatements = new ConcurrentHashMap<>();
 
    public static ShowProcessListManager getInstance() {
        return INSTANCE;
    }
    
    public void putProcessContext(final String processID, final ExecuteProcessContext process) {
        processes.put(processID, process);
    }
    
    public void putProcessStatement(final String processID, final Collection<Statement> statements) {
        if (statements.isEmpty()) {
            return;
        }
        processStatements.put(processID, statements);
    }
}
```

As shown above, the `ShowProcessListManager` class has two cache Maps, namely `processes` and `processStatements`. The former stores the mapping between `processID` and `ExecuteProcessContext`.

The latter contains the mapping between `processID` and `Statement objects` that may generate multiple statements after the SQL is overwritten.

Every time ShardingSphere receives a SQL statement, the SQL information will be cached into the two Maps. After SQL is executed, the cache of Map will be deleted.

```java
@RequiredArgsConstructor
public final class ProxyJDBCExecutor {
    
    private final String type;
    
    private final ConnectionSession connectionSession;
    
    private final JDBCDatabaseCommunicationEngine databaseCommunicationEngine;
    
    private final JDBCExecutor jdbcExecutor;
    
    public List<ExecuteResult> execute(final QueryContext queryContext, final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                       final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        try {
            MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
            EventBusContext eventBusContext = ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext();
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName());
            DatabaseType protocolType = database.getProtocolType();
            DatabaseType databaseType = database.getResource().getDatabaseType();
            ExecuteProcessEngine.initialize(queryContext, executionGroupContext, eventBusContext);
            SQLStatementContext<?> context = queryContext.getSqlStatementContext();
            List<ExecuteResult> result = jdbcExecutor.execute(executionGroupContext,
                    ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, databaseType, context.getSqlStatement(), databaseCommunicationEngine, isReturnGeneratedKeys, isExceptionThrown,
                            true),
                    ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, databaseType, context.getSqlStatement(), databaseCommunicationEngine, isReturnGeneratedKeys, isExceptionThrown,
                            false));
            ExecuteProcessEngine.finish(executionGroupContext.getProcessID(), eventBusContext);
            return result;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
```

As shown above, `ExecuteProcessEngine.initialize(queryContext, executionGroupContext, eventBusContext);` will store the SQL information in the two cache Maps. Finally, `ExecuteProcessEngine.clean();` in the code block will clear up the Map in the cache.

The SQL shown in the `Show processlist` was obtained from `processes`. But this Map is just a local cache. If ShardingSphere is deployed in cluster mode, how does `Show processlist` obtain SQL running on other machines in the cluster? Let's see how ShardingSphere handles it.

## 2.2 How does `Show processlist` work?

When ShardingSphere receives the `Show process` command, it is sent to the executor `ShowProcessListExecutor#execute` for processing. The implementation of the `getQueryResult()` is the focus.

```java
public final class ShowProcessListExecutor implements DatabaseAdminQueryExecutor {
    
    private Collection<String> batchProcessContexts;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    public ShowProcessListExecutor() {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().register(this);
    }
    
    @Subscribe
    public void receiveProcessListData(final ShowProcessListResponseEvent event) {
        batchProcessContexts = event.getBatchProcessContexts();
    }
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new TransparentMergedResult(getQueryResult());
    }
    
    private QueryResult getQueryResult() {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(new ShowProcessListRequestEvent());
        if (null == batchProcessContexts || batchProcessContexts.isEmpty()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        Collection<YamlExecuteProcessContext> processes = new LinkedList<>();
        for (String each : batchProcessContexts) {
            processes.addAll(YamlEngine.unmarshal(each, BatchYamlExecuteProcessContext.class).getContexts());
        }
        List<MemoryQueryResultDataRow> rows = processes.stream().map(process -> {
            List<Object> rowValues = new ArrayList<>(8);
            rowValues.add(process.getProcessIDID());
            rowValues.add(process.getUsername());
            rowValues.add(process.getHostname());
            rowValues.add(process.getDatabaseName());
            rowValues.add("Execute");
            rowValues.add(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - process.getStartTimeMillis()));
            int processDoneCount = process.getUnitStatuses().stream().map(each -> ExecuteProcessConstants.EXECUTE_STATUS_DONE == each.getStatus() ? 1 : 0).reduce(0, Integer::sum);
            String statePrefix = "Executing ";
            rowValues.add(statePrefix + processDoneCount + "/" + process.getUnitStatuses().size());
            String sql = process.getSql();
            if (null != sql && sql.length() > 100) {
                sql = sql.substring(0, 100);
            }
            rowValues.add(null != sql ? sql : "");
            return new MemoryQueryResultDataRow(rowValues);
        }).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>();
        columns.add(new RawQueryResultColumnMetaData("", "Id", "Id", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "User", "User", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Host", "Host", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "db", "db", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Command", "Command", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Time", "Time", Types.VARCHAR, "VARCHAR", 10, 0));
        columns.add(new RawQueryResultColumnMetaData("", "State", "State", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Info", "Info", Types.VARCHAR, "VARCHAR", 120, 0));
        return new RawQueryResultMetaData(columns);
    }
}
```

You’ll use the `guava` package's `EventBus` function, which is an information publish/subscribe database that is an elegant implementation of the [Observer pattern](https://en.wikipedia.org/wiki/Observer_pattern). `EventBus` decouples classes from each other, and you'll find out more about it below.

`getQueryResult()` method will post `ShowProcessListRequestEvent`. `ProcessRegistrySubscriber#loadShowProcessListData` uses the `@Subscribe` annotations to subscribe to the event.

This method is the core to implementing `Show processlist`. Next, we'll introduce specific procedures of this method.

```java
public final class ProcessRegistrySubscriber {    
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        String processId = new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong()).toString().replace("-", "");
        boolean triggerIsComplete = false;
        // 1. Obtain the Process List path of all existing proxy nodes in cluster mode
        Collection<String> triggerPaths = getTriggerPaths(processId);
        try {
            // 2. Iterate through the path and write an empty string to the node, to trigger the node monitoring.
            triggerPaths.forEach(each -> repository.persist(each, ""));
            // 3. Lock and wait 5 seconds for each node to write the information of currently running SQL to the persistence layer. 
            triggerIsComplete = waitAllNodeDataReady(processId, triggerPaths);
            // 4. Fetch and aggregate the data written by each proxy node from the persistence layer. Then EventBus will post a ShowProcessListResponseEvent command, which means the operation is completed.
            sendShowProcessList(processId);
        } finally {
            // 5. Delete resources
            repository.delete(ProcessNode.getProcessIdPath(processId));
            if (!triggerIsComplete) {
                triggerPaths.forEach(repository::delete);
            }
        }
    }
}
```

It contains five steps and steps 2 & 3 are the focus.

### 2.2.1 Step 2: the cluster obtains the data implementation

In this step, an empty string will be written to the node `/nodes/compute_nodes/show_process_list_trigger/<instanceId>:<processId>`, which will trigger ShardingSphere's monitoring logic.

When ShardingSphere is started, the persistence layer will `watch` to monitor a series of path changes, such as the addition, deletion, and modification operations of the path `/nodes/compute_nodes`.

However, monitoring is an asynchronous process and the main thread does not block, so step 3 is required to lock and wait for each ShardingSphere node to write its currently running SQL information into the persistence layer.

Let's take a look at how the ShardingSphere handles the monitoring logic.

```java
public final class ComputeNodeStateChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final String databaseName) {
        return Collections.singleton(ComputeNode.getComputeNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String instanceId = ComputeNode.getInstanceIdByComputeNode(event.getKey());
        if (!Strings.isNullOrEmpty(instanceId)) {
            ...
        } else if (event.getKey().startsWith(ComputeNode.getOnlineInstanceNodePath())) {
            return createInstanceEvent(event);
            // show processlist
        } else if (event.getKey().startsWith(ComputeNode.getProcessTriggerNodePatch())) {
            return createShowProcessListTriggerEvent(event);
            // kill processId
        } else if (event.getKey().startsWith(ComputeNode.getProcessKillNodePatch())) {
            return createKillProcessIdEvent(event);
        }
        return Optional.empty();
    }
    
        
    private Optional<GovernanceEvent> createShowProcessListTriggerEvent(final DataChangedEvent event) {
        Matcher matcher = getShowProcessTriggerMatcher(event);
        if (!matcher.find()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new ShowProcessListTriggerEvent(matcher.group(1), matcher.group(2)));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new ShowProcessListUnitCompleteEvent(matcher.group(2)));
        }
        return Optional.empty();
    }
}
```

After `ComputeNodeStateChangedWatcher#createGovernanceEvent` monitored the information, it would distinguish which event to create according to the path.

As shown in the above code, it is a new node, so `ShowProcessListTriggerEvent` will be posted. As each ShardingSphere instance will monitor `/nodes/compute_nodes`, each instance will process `ShowProcessListTriggerEvent`.

In this case, single-machine processing is transformed into cluster processing. Let's look at how ShardingSphere handles it.

```java
public final class ClusterContextManagerCoordinator {    @Subscribe
    public synchronized void triggerShowProcessList(final ShowProcessListTriggerEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<ExecuteProcessContext> processes = ShowProcessListManager.getInstance().getAllProcessContext();
        if (!processes.isEmpty()) {
            registryCenter.getRepository().persist(ProcessNode.getProcessListInstancePath(event.getProcessId(), event.getInstanceId()),
                    YamlEngine.marshal(new BatchYamlExecuteProcessContext(processes)));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceIdNodePath(event.getInstanceId(), event.getProcessId()));
    }
}
```

`ClusterContextManagerCoordinator#triggerShowProcessList` will subscribe to `ShowProcessListTriggerEvent`, in which `process` data is processed by itself. `ShowProcessListManager.getInstance().getAllProcessContext()` retrieves the `process` that is currently running (here the data refers to the SQL information that ShardingSphere stores in the Map before each SQL execution, which is described at the beginning of the article) and transfers it to the persistence layer. If the `/nodes/compute_nodes/show_process_list_trigger/<instanceId>:<processId>` node is deleted, the processing is completed.

When you delete the node, monitoring will also be triggered and `ShowProcessListUnitCompleteEvent` will be posted. This event will finally awake the pending lock.

```java
public final class ClusterContextManagerCoordinator {
    
    @Subscribe
    public synchronized void completeUnitShowProcessList(final ShowProcessListUnitCompleteEvent event) {
        ShowProcessListLock lock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessId());
        if (null != lock) {
            lock.doNotify();
        }
    }
}
```

### 2.2.2 Step 3: lock and wait for the data implementation

ShardingSphere uses the `isReady(Paths)` method to determine whether all instances have been processed. It returns `true` only when all instances have been processed.

There is a maximum waiting time of 5 seconds for data processing. If the processing is not completed in 5 seconds, then `false` is returned.

```java
public final class ClusterContextManagerCoordinator {
    
    @Subscribe
    public synchronized void completeUnitShowProcessList(final ShowProcessListUnitCompleteEvent event) {
        ShowProcessListLock lock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessId());
        if (null != lock) {
            lock.doNotify();
        }
    }
}
```

### 2.2.3 Aggregate the `processList` data and return it

After each instance processed the data, the instance that received the `Show processlist` command needs to aggregate the data and then display the result.

```java
public final class ProcessRegistrySubscriber {  
    
    private void sendShowProcessList(final String processId) {
        List<String> childrenKeys = repository.getChildrenKeys(ProcessNode.getProcessIdPath(processId));
        Collection<String> batchProcessContexts = new LinkedList<>();
        for (String each : childrenKeys) {
            batchProcessContexts.add(repository.get(ProcessNode.getProcessListInstancePath(processId, each)));
        }
        eventBusContext.post(new ShowProcessListResponseEvent(batchProcessContexts));
    }
}
```

`ProcessRegistrySubscriber#sendShowProcessList` will aggregate the running SQL data into `batchProcessContexts`, and then post `ShowProcessListResponseEvent`.

This event will be consumed by `ShowProcessListExecutor#receiveProcessListData`, and the `getQueryResult()` method will proceed to show the `queryResult`.

So far, we’ve completed the execution process of `Show processlist` command.

## 2.3 How does `Kill <processId>` work?

`Kill <processId>` shares a similar logic with `Show processlist`, that is to combine `EventBus` with the `watch` mechanism.

Since we do not know which SQL the `processId` belongs to, it is also necessary to add empty nodes for each instance.

Through the `watch` mechanism, each ShardingSphere instance watches to the new node and checks whether the `processId` key is in the cache Map. If yes, fetch the value corresponding to the key.

The value is a `Collection<Statement>` collection. Then you only have to iterate through the `Statement` collection and call `statement.cancel()` in turn. The underlying layer is `java.sql.Statement#cancel()` method called to cancel SQL execution.

# 3. Conclusion

Currently, Apache ShardingSphere can only implement the `Show processlist` and `Kill <processId>` functions for [MySQL](https://www.mysql.com/) dialects.

Once you get to know how they work, and if you’re interested, you‘re welcome to participate in the development of related functions. Our community is very open and anyone who is interested in contributing to open source code is welcome.

# Relevant Links:

[Apache ShardingSphere Official Website](https://shardingsphere.apache.org/)

[Apache ShardingSphere GitHub](https://github.com/apache/shardingsphere)

[Apache ShardingSphere Slack Channel](https://apacheshardingsphere.slack.com/)

# Author

Xu Yang, a middleware R&D engineer at [Servyou Group](https://www.crunchbase.com/organization/servyou-group). Responsible for the table and database sharding with massive data. An open source enthusiast and ShardingSphere contributor. Currently, he’s interested in developing the kernel module of the ShardingSphere project.
