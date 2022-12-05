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

package org.apache.shardingsphere.test.integration.env;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.cases.value.SQLValueGroup;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * Data set environment manager.
 */
public final class DataSetEnvironmentManager {
    
    // TODO ExecutorEngine.execute and callback
    private static final ExecutorServiceManager EXECUTOR_SERVICE_MANAGER = ExecutorEngine.createExecutorEngineWithCPU().getExecutorServiceManager();
    
    private final DataSet dataSet;
    
    private final Map<String, DataSource> dataSourceMap;
    
    public DataSetEnvironmentManager(final String dataSetFile, final Map<String, DataSource> dataSourceMap) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(dataSetFile)) {
            dataSet = (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
        this.dataSourceMap = dataSourceMap;
    }
    
    /**
     * Fill data.
     *
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     */
    public void fillData() throws SQLException, ParseException {
        Map<DataNode, List<DataSetRow>> dataNodeListMap = getDataSetRowMap();
        List<Callable<Void>> fillDataTasks = new LinkedList<>();
        for (Entry<DataNode, List<DataSetRow>> entry : dataNodeListMap.entrySet()) {
            DataNode dataNode = entry.getKey();
            List<DataSetRow> dataSetRows = entry.getValue();
            DataSetMetaData dataSetMetaData = dataSet.findMetaData(dataNode);
            List<SQLValueGroup> sqlValueGroups = new LinkedList<>();
            for (DataSetRow row : dataSetRows) {
                sqlValueGroups.add(new SQLValueGroup(dataSetMetaData, row.splitValues(",")));
            }
            String insertSQL;
            try (Connection connection = dataSourceMap.get(dataNode.getDataSourceName()).getConnection()) {
                DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(connection.getMetaData().getURL());
                insertSQL = generateInsertSQL(databaseType.getQuoteCharacter().wrap(dataNode.getTableName()), dataSetMetaData.getColumns(), databaseType.getType());
            }
            fillDataTasks.add(new InsertTask(dataSourceMap.get(dataNode.getDataSourceName()), insertSQL, sqlValueGroups));
        }
        try {
            EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(fillDataTasks);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
        }
    }
    
    private Map<DataNode, List<DataSetRow>> getDataSetRowMap() {
        Map<DataNode, List<DataSetRow>> result = new LinkedHashMap<>(dataSet.getRows().size(), 1);
        for (DataSetRow each : dataSet.getRows()) {
            DataNode dataNode = new DataNode(each.getDataNode());
            if (!result.containsKey(dataNode)) {
                result.put(dataNode, new LinkedList<>());
            }
            result.get(dataNode).add(each);
        }
        return result;
    }
    
    private String generateInsertSQL(final String tableName, final Collection<DataSetColumn> columnMetaData, final String databaseTypeName) {
        List<String> columnNames = new LinkedList<>();
        List<String> placeholders = new LinkedList<>();
        for (DataSetColumn each : columnMetaData) {
            columnNames.add(each.getName());
            String type = each.getType();
            if (type.startsWith("enum#") && "PostgreSQL".equals(databaseTypeName)) {
                placeholders.add(generateEnum(type));
                continue;
            }
            placeholders.add("?");
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(",", columnNames), String.join(",", placeholders));
    }
    
    private String generateEnum(final String type) {
        String[] split = type.split("#");
        return split.length == 2 ? String.format("CAST( ? AS %s )", split[1]) : "?";
    }
    
    /**
     * Clean data.
     */
    public void cleanData() {
        List<Callable<Void>> deleteTasks = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : getDataNodeMap().entrySet()) {
            deleteTasks.add(new DeleteTask(dataSourceMap.get(entry.getKey()), entry.getValue()));
        }
        try {
            EXECUTOR_SERVICE_MANAGER.getExecutorService().invokeAll(deleteTasks);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
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
        for (String each : new InlineExpressionParser(dataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            DataNode dataNode = new DataNode(each);
            if (!result.containsKey(dataNode.getDataSourceName())) {
                result.put(dataNode.getDataSourceName(), new LinkedList<>());
            }
            result.get(dataNode.getDataSourceName()).add(dataNode.getTableName());
        }
        return result;
    }
    
    @RequiredArgsConstructor
    private static class InsertTask implements Callable<Void> {
        
        private final DataSource dataSource;
        
        private final String insertSQL;
        
        private final Collection<SQLValueGroup> sqlValueGroups;
        
        @Override
        public Void call() throws SQLException {
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                for (SQLValueGroup each : sqlValueGroups) {
                    setParameters(preparedStatement, each);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
            return null;
        }
        
        private void setParameters(final PreparedStatement preparedStatement, final SQLValueGroup sqlValueGroup) throws SQLException {
            for (SQLValue each : sqlValueGroup.getValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
        }
    }
    
    @RequiredArgsConstructor
    private static class DeleteTask implements Callable<Void> {
        
        private final DataSource dataSource;
        
        private final Collection<String> tableNames;
        
        @Override
        public Void call() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                for (String each : tableNames) {
                    DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(connection.getMetaData().getURL());
                    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s", databaseType.getQuoteCharacter().wrap(each)))) {
                        preparedStatement.execute();
                    }
                }
            }
            return null;
        }
    }
}
