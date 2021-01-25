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

package org.apache.shardingsphere.test.integration.env.dataset;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect.DatabaseMetaDataDialectHandler;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect.DatabaseMetaDataDialectHandlerFactory;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.dataset.DataSet;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
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
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Data set environment manager.
 */
public final class DataSetEnvironmentManager {
    
    private static final ExecutorServiceManager EXECUTOR_SERVICE_MANAGER = new ExecutorServiceManager(20);
    
    private final DataSet dataSet;
    
    private final Map<String, DataSource> actualDataSources;
    
    public DataSetEnvironmentManager(final String dataSetFile, final Map<String, DataSource> actualDataSources) throws IOException, JAXBException {
        try (FileReader reader = new FileReader(dataSetFile)) {
            dataSet = (DataSet) JAXBContext.newInstance(DataSet.class).createUnmarshaller().unmarshal(reader);
        }
        this.actualDataSources = actualDataSources;
    }
    
    private static String generateTableName(final String tableName, final DatabaseType databaseType) {
        Optional<DatabaseMetaDataDialectHandler> databaseMetaDataDialectHandler = DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType);
        if (databaseMetaDataDialectHandler.isPresent()) {
            return databaseMetaDataDialectHandler.get().getQuoteCharacter().wrap(tableName);
        }
        throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
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
            DataSetMetadata dataSetMetadata = dataSet.findMetadata(dataNode);
            List<SQLValueGroup> sqlValueGroups = new LinkedList<>();
            for (DataSetRow row : dataSetRows) {
                sqlValueGroups.add(new SQLValueGroup(dataSetMetadata, row.getValues()));
            }
            String insertSQL;
            try (Connection connection = actualDataSources.get(dataNode.getDataSourceName()).getConnection()) {
                insertSQL = generateInsertSQL(generateTableName(dataNode.getTableName(), DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL())), dataSetMetadata.getColumns());
            }
            fillDataTasks.add(new InsertTask(actualDataSources.get(dataNode.getDataSourceName()), insertSQL, sqlValueGroups));
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
    
    private String generateInsertSQL(final String tableName, final Collection<DataSetColumn> columnMetadata) {
        List<String> columnNames = new LinkedList<>();
        List<String> placeholders = new LinkedList<>();
        for (DataSetColumn each : columnMetadata) {
            columnNames.add(each.getName());
            placeholders.add("?");
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, Joiner.on(",").join(columnNames), Joiner.on(",").join(placeholders));
    }
    
    /**
     * Clear data.
     * 
     */
    public void clearData() {
        List<Callable<Void>> deleteTasks = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : getDataNodeMap().entrySet()) {
            deleteTasks.add(new DeleteTask(actualDataSources.get(entry.getKey()), entry.getValue()));
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
        for (DataSetMetadata each : dataSet.getMetadataList()) {
            for (Entry<String, Collection<String>> entry : getDataNodeMap(each).entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new LinkedList<>());
                }
                result.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, Collection<String>> getDataNodeMap(final DataSetMetadata dataSetMetadata) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (String each : new InlineExpressionParser(dataSetMetadata.getDataNodes()).splitAndEvaluate()) {
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
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                    for (SQLValueGroup each : sqlValueGroups) {
                        setParameters(preparedStatement, each);
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                }
            }
            return null;
        }
        
        private void setParameters(final PreparedStatement preparedStatement, final SQLValueGroup sqlValueGroup) throws SQLException {
            for (SQLValue each : sqlValueGroup.getSqlValues()) {
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
                    String tableName = generateTableName(each, DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL()));
                    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s", tableName))) {
                        preparedStatement.execute();
                    }
                }
            }
            return null;
        }
    }
}
