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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.DataSet;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValueGroup;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Data set environment manager.
 */
public final class DataSetEnvironmentManager {
    
    // TODO ExecutorEngine.execute and callback
    private static final ExecutorServiceManager EXECUTOR_SERVICE_MANAGER = ExecutorEngine.createExecutorEngineWithSize(Runtime.getRuntime().availableProcessors() * 2 - 1).getExecutorServiceManager();
    
    private static final String DATA_COLUMN_DELIMITER = ", ";
    
    private final DataSet dataSet;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DatabaseType databaseType;
    
    public DataSetEnvironmentManager(final String dataSetFile, final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(dataSetFile)) {
            dataSet = (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
        this.dataSourceMap = dataSourceMap;
        this.databaseType = databaseType;
    }
    
    /**
     * Fill data.
     */
    @SneakyThrows({SQLException.class, InterruptedException.class, ExecutionException.class})
    public void fillData() {
        Map<DataNode, List<DataSetRow>> dataNodeListMap = getDataSetRowMap();
        List<Callable<Void>> fillDataTasks = new LinkedList<>();
        for (Entry<DataNode, List<DataSetRow>> entry : dataNodeListMap.entrySet()) {
            DataNode dataNode = entry.getKey();
            List<DataSetRow> dataSetRows = entry.getValue();
            DataSetMetaData dataSetMetaData = dataSet.findMetaData(dataNode);
            List<SQLValueGroup> sqlValueGroups = new LinkedList<>();
            for (DataSetRow row : dataSetRows) {
                sqlValueGroups.add(new SQLValueGroup(dataSetMetaData, row.splitValues(DATA_COLUMN_DELIMITER)));
            }
            String insertSQL;
            DatabaseType databaseType;
            try (Connection connection = dataSourceMap.get(dataNode.getDataSourceName()).getConnection()) {
                databaseType = DatabaseTypeFactory.get(connection.getMetaData());
                insertSQL = generateInsertSQL(dataNode.getTableName(), dataSetMetaData.getColumns(), databaseType);
            }
            fillDataTasks.add(new InsertTask(dataSourceMap.get(dataNode.getDataSourceName()), insertSQL, sqlValueGroups, databaseType));
        }
        final List<Future<Void>> futures = EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(fillDataTasks);
        for (Future<Void> future : futures) {
            future.get();
        }
    }
    
    private Map<DataNode, List<DataSetRow>> getDataSetRowMap() {
        Map<DataNode, List<DataSetRow>> result = new LinkedHashMap<>(dataSet.getRows().size(), 1F);
        for (DataSetRow each : dataSet.getRows()) {
            // The data type of the current table is currently only used by mysql.
            if (each.getDataNode().contains("t_product_extend") && !"MySQL".equals(databaseType.getType())) {
                continue;
            }
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!result.containsKey(dataNode)) {
                result.put(dataNode, new LinkedList<>());
            }
            result.get(dataNode).add(each);
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
     * Clean data.
     */
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    public void cleanData() {
        List<Callable<Void>> deleteTasks = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : getDataNodeMap().entrySet()) {
            deleteTasks.add(new DeleteTask(dataSourceMap.get(entry.getKey()), entry.getValue()));
        }
        List<Future<Void>> futures = EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(deleteTasks);
        for (Future<Void> future : futures) {
            future.get();
        }
    }
    
    private Map<String, Collection<String>> getDataNodeMap() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (DataSetMetaData each : dataSet.getMetaDataList()) {
            for (Entry<String, Collection<String>> entry : getDataNodeMap(each).entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new LinkedList<>());
                }
                result.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, Collection<String>> getDataNodeMap(final DataSetMetaData dataSetMetaData) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (String each : InlineExpressionParserFactory.newInstance(dataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            if (!result.containsKey(dataNode.getDataSourceName())) {
                result.put(dataNode.getDataSourceName(), new LinkedList<>());
            }
            result.get(dataNode.getDataSourceName()).add(dataNode.getTableName());
        }
        return result;
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
        
        private final Collection<String> tableNames;
        
        @Override
        public Void call() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseType databaseType = DatabaseTypeFactory.get(connection.getMetaData());
                for (String each : tableNames) {
                    String quotedTableName = getQuotedTableName(each, databaseType);
                    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("TRUNCATE TABLE %s", quotedTableName))) {
                        preparedStatement.execute();
                    }
                }
            }
            return null;
        }
        
        private String getQuotedTableName(final String tableName, final DatabaseType databaseType) {
            DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
            return databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter().wrap(databaseTypeRegistry.formatIdentifierPattern(tableName));
        }
    }
}
