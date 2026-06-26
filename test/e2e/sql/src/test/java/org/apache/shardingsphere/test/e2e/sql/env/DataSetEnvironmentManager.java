/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.sql.env;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sqlbatch.DialectSQLBatchOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValueGroup;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Data set environment manager.
 */
public final class DataSetEnvironmentManager {
    
    // TODO ExecutorEngine.execute and callback
    private static final ExecutorServiceManager EXECUTOR_SERVICE_MANAGER = ExecutorEngine.createExecutorEngineWithSize(Runtime.getRuntime().availableProcessors() * 2 - 1).getExecutorServiceManager();
    
    private static final String DATA_COLUMN_DELIMITER = ", ";
    
    private static final Map<String, ResetPlan> RESET_PLAN_CACHE = new ConcurrentHashMap<>();
    
    private static final Map<String, String> INSERT_SQL_CACHE = new ConcurrentHashMap<>();
    
    private static final Map<String, String> TRUNCATE_SQL_CACHE = new ConcurrentHashMap<>();
    
    private final String dataSetFile;
    
    private final DataSet dataSet;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DatabaseType databaseType;
    
    public DataSetEnvironmentManager(final String dataSetFile, final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        this.dataSetFile = dataSetFile;
        dataSet = DataSetLoader.load(dataSetFile);
        this.dataSourceMap = dataSourceMap;
        this.databaseType = databaseType;
    }
    
    /**
     * Fill data for specified tables, or all tables when table names are empty.
     *
     * @param tableNames table names
     */
    @SneakyThrows({SQLException.class, InterruptedException.class, ExecutionException.class})
    public void fillData(final Collection<String> tableNames) {
        List<Callable<Void>> fillDataTasks = createFillDataTasks(getResetPlan(tableNames));
        List<Future<Void>> futures = EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(fillDataTasks);
        for (Future<Void> future : futures) {
            future.get();
        }
    }
    
    private List<Callable<Void>> createFillDataTasks(final ResetPlan resetPlan) {
        List<Callable<Void>> result = new LinkedList<>();
        for (Entry<DataNode, InsertDataNodePlan> entry : resetPlan.getInsertDataNodePlans().entrySet()) {
            DataNode dataNode = entry.getKey();
            DataSource dataSource = dataSourceMap.get(dataNode.getDataSourceName());
            InsertDataNodePlan insertDataNodePlan = entry.getValue();
            result.add(new InsertTask(dataSource, getInsertSQL(dataNode.getTableName(), insertDataNodePlan.getColumnMetaData(), databaseType),
                    insertDataNodePlan.getSqlValueGroups(), databaseType));
        }
        return result;
    }
    
    private String getInsertSQL(final String tableName, final Collection<DataSetColumn> columnMetaData, final DatabaseType databaseType) {
        return INSERT_SQL_CACHE.computeIfAbsent(getInsertSQLCacheKey(tableName, columnMetaData, databaseType), unused -> generateInsertSQL(tableName, columnMetaData, databaseType));
    }
    
    private String getInsertSQLCacheKey(final String tableName, final Collection<DataSetColumn> columnMetaData, final DatabaseType databaseType) {
        StringBuilder result = new StringBuilder(databaseType.getType()).append(':').append(tableName);
        for (DataSetColumn each : columnMetaData) {
            result.append(':').append(each.getName()).append('#').append(each.getType());
        }
        return result.toString();
    }
    
    private Map<DataNode, InsertDataNodePlan> createInsertDataNodePlans(final Collection<String> tableNames, final Map<DataNode, DataSetMetaData> metaDataMap) {
        Map<DataNode, InsertDataNodePlan> result = new LinkedHashMap<>(dataSet.getRows().size(), 1F);
        for (DataSetRow each : dataSet.getRows()) {
            if (each.getDataNode().contains("t_product_extend") && !"MySQL".equals(databaseType.getType())) {
                continue;
            }
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!isMatchedDataNode(dataNode, tableNames)) {
                continue;
            }
            DataSetMetaData dataSetMetaData = metaDataMap.get(dataNode);
            if (null == dataSetMetaData) {
                throw new IllegalArgumentException(String.format("Cannot find data node: %s", dataNode));
            }
            if (!result.containsKey(dataNode)) {
                result.put(dataNode, new InsertDataNodePlan(dataSetMetaData.getColumns()));
            }
            result.get(dataNode).addSQLValueGroup(new SQLValueGroup(dataSetMetaData, each.splitValues(DATA_COLUMN_DELIMITER)));
        }
        return result;
    }
    
    private String generateInsertSQL(final String tableName, final Collection<DataSetColumn> columnMetaData, final DatabaseType databaseType) {
        List<String> columnNames = new LinkedList<>();
        List<String> placeholders = new LinkedList<>();
        for (DataSetColumn each : columnMetaData) {
            columnNames.add(each.getName());
            placeholders.add(generateProperPlaceholderExpression(databaseType, each));
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(",", columnNames), String.join(",", placeholders));
    }
    
    private String generateProperPlaceholderExpression(final DatabaseType databaseType, final DataSetColumn dataSetColumn) {
        String type = dataSetColumn.getType();
        return (type.startsWith("enum#") || type.startsWith("cast#")) && (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType)
                ? generateTypeCastPlaceholder(type)
                : "?";
    }
    
    private String generateTypeCastPlaceholder(final String type) {
        String[] split = type.split("#");
        return split.length == 2 ? String.format("CAST( ? AS %s )", split[1]) : "?";
    }
    
    /**
     * Clean data for specified tables, or all tables when table names are empty.
     *
     * @param tableNames table names
     */
    @SneakyThrows({SQLException.class, InterruptedException.class, ExecutionException.class})
    public void cleanData(final Collection<String> tableNames) {
        List<Callable<Void>> deleteTasks = createDeleteTasks(getResetPlan(tableNames));
        List<Future<Void>> futures = EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(deleteTasks);
        for (Future<Void> future : futures) {
            future.get();
        }
    }
    
    private List<Callable<Void>> createDeleteTasks(final ResetPlan resetPlan) {
        List<Callable<Void>> result = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : resetPlan.getTableNamesByDataSourceName().entrySet()) {
            Collection<String> truncateSQLs = new LinkedList<>();
            for (String each : entry.getValue()) {
                truncateSQLs.add(getTruncateSQL(each, databaseType));
            }
            result.add(new DeleteTask(dataSourceMap.get(entry.getKey()), truncateSQLs));
        }
        return result;
    }
    
    private String getTruncateSQL(final String tableName, final DatabaseType databaseType) {
        return TRUNCATE_SQL_CACHE.computeIfAbsent(databaseType.getType() + ':' + tableName, unused -> String.format("TRUNCATE TABLE %s", getQuotedTableName(tableName, databaseType)));
    }
    
    private static String getQuotedTableName(final String tableName, final DatabaseType databaseType) {
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        return databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter().wrap(databaseTypeRegistry.formatIdentifierPattern(tableName));
    }
    
    private ResetPlan getResetPlan(final Collection<String> tableNames) {
        return RESET_PLAN_CACHE.computeIfAbsent(getResetPlanCacheKey(tableNames), unused -> createResetPlan(tableNames));
    }
    
    private String getResetPlanCacheKey(final Collection<String> tableNames) {
        return dataSetFile + ':' + databaseType.getType() + ':' + String.join(",", new TreeSet<>(getEffectiveTableNames(tableNames)));
    }
    
    private ResetPlan createResetPlan(final Collection<String> tableNames) {
        Collection<String> effectiveTableNames = getEffectiveTableNames(tableNames);
        if (!effectiveTableNames.isEmpty() && !containsMatchedMetaData(effectiveTableNames)) {
            effectiveTableNames = Collections.emptySet();
        }
        Map<String, Collection<String>> tableNamesByDataSourceName = new LinkedHashMap<>();
        Map<DataNode, DataSetMetaData> metaDataMap = createMetaDataMap(effectiveTableNames, tableNamesByDataSourceName);
        return new ResetPlan(createInsertDataNodePlans(effectiveTableNames, metaDataMap), tableNamesByDataSourceName);
    }
    
    private Map<DataNode, DataSetMetaData> createMetaDataMap(final Collection<String> tableNames, final Map<String, Collection<String>> tableNamesByDataSourceName) {
        Map<DataNode, DataSetMetaData> result = new LinkedHashMap<>();
        for (DataSetMetaData each : dataSet.getMetaDataList()) {
            if (!isMatchedMetaData(each, tableNames)) {
                continue;
            }
            for (String dataNodeText : InlineExpressionParserFactory.newInstance(each.getDataNodes()).splitAndEvaluate()) {
                DataNode dataNode = new DataNode(dataNodeText);
                result.put(dataNode, each);
                addTableName(tableNamesByDataSourceName, dataNode);
            }
        }
        return result;
    }
    
    private void addTableName(final Map<String, Collection<String>> tableNamesByDataSourceName, final DataNode dataNode) {
        if (!tableNamesByDataSourceName.containsKey(dataNode.getDataSourceName())) {
            tableNamesByDataSourceName.put(dataNode.getDataSourceName(), new LinkedList<>());
        }
        tableNamesByDataSourceName.get(dataNode.getDataSourceName()).add(dataNode.getTableName());
    }
    
    private Collection<String> getEffectiveTableNames(final Collection<String> tableNames) {
        return tableNames.stream().filter(each -> null != each && !each.isEmpty()).map(each -> each.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
    }
    
    private boolean isMatchedMetaData(final DataSetMetaData dataSetMetaData, final Collection<String> tableNames) {
        if (tableNames.isEmpty()) {
            return true;
        }
        if (null != dataSetMetaData.getTableName() && tableNames.contains(dataSetMetaData.getTableName().toLowerCase(Locale.ENGLISH))) {
            return true;
        }
        for (String each : InlineExpressionParserFactory.newInstance(dataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            if (isMatchedDataNode(new DataNode(each), tableNames)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsMatchedMetaData(final Collection<String> tableNames) {
        for (DataSetMetaData each : dataSet.getMetaDataList()) {
            if (isMatchedMetaData(each, tableNames)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchedDataNode(final DataNode dataNode, final Collection<String> tableNames) {
        return tableNames.isEmpty() || getPossibleTableNames(dataNode.getTableName()).stream().anyMatch(tableNames::contains);
    }
    
    private Collection<String> getPossibleTableNames(final String tableName) {
        Collection<String> result = new HashSet<>();
        result.add(tableName.toLowerCase(Locale.ENGLISH));
        result.add(tableName.replaceFirst("_[0-9]+$", "").toLowerCase(Locale.ENGLISH));
        result.add(tableName.replaceFirst("[0-9]+$", "").toLowerCase(Locale.ENGLISH));
        return result;
    }
    
    @RequiredArgsConstructor
    private static final class ResetPlan {
        
        private final Map<DataNode, InsertDataNodePlan> insertDataNodePlans;
        
        private final Map<String, Collection<String>> tableNamesByDataSourceName;
        
        private Map<DataNode, InsertDataNodePlan> getInsertDataNodePlans() {
            return insertDataNodePlans;
        }
        
        private Map<String, Collection<String>> getTableNamesByDataSourceName() {
            return tableNamesByDataSourceName;
        }
    }
    
    @RequiredArgsConstructor
    private static final class InsertDataNodePlan {
        
        private final Collection<DataSetColumn> columnMetaData;
        
        private final Collection<SQLValueGroup> sqlValueGroups = new LinkedList<>();
        
        private Collection<DataSetColumn> getColumnMetaData() {
            return columnMetaData;
        }
        
        private Collection<SQLValueGroup> getSqlValueGroups() {
            return sqlValueGroups;
        }
        
        private void addSQLValueGroup(final SQLValueGroup sqlValueGroup) {
            sqlValueGroups.add(sqlValueGroup);
        }
    }
    
    @RequiredArgsConstructor
    private static final class InsertTask implements Callable<Void> {
        
        private final DataSource dataSource;
        
        private final String insertSQL;
        
        private final Collection<SQLValueGroup> sqlValueGroups;
        
        private final DatabaseType databaseType;
        
        @Override
        public Void call() throws SQLException {
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                DialectSQLBatchOption sqlBatchOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSQLBatchOption();
                if (sqlBatchOption.isSupportSQLBatch()) {
                    executeBatch(preparedStatement);
                } else {
                    executeUpdate(preparedStatement);
                }
            }
            return null;
        }
        
        private void executeBatch(final PreparedStatement preparedStatement) throws SQLException {
            for (SQLValueGroup each : sqlValueGroups) {
                setParameters(preparedStatement, each);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        
        private void executeUpdate(final PreparedStatement preparedStatement) throws SQLException {
            for (SQLValueGroup each : sqlValueGroups) {
                setParameters(preparedStatement, each);
                preparedStatement.executeUpdate();
            }
        }
        
        private void setParameters(final PreparedStatement preparedStatement, final SQLValueGroup sqlValueGroup) throws SQLException {
            for (SQLValue each : sqlValueGroup.getValues()) {
                if ("Hive".equalsIgnoreCase(databaseType.getType()) && each.getValue() instanceof Date) {
                    preparedStatement.setDate(each.getIndex(), (java.sql.Date) each.getValue());
                } else {
                    preparedStatement.setObject(each.getIndex(), each.getValue());
                }
            }
        }
    }
    
    @RequiredArgsConstructor
    private static final class DeleteTask implements Callable<Void> {
        
        private final DataSource dataSource;
        
        private final Collection<String> truncateSQLs;
        
        @Override
        public Void call() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                for (String each : truncateSQLs) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                        preparedStatement.execute();
                    }
                }
            }
            return null;
        }
    }
}
