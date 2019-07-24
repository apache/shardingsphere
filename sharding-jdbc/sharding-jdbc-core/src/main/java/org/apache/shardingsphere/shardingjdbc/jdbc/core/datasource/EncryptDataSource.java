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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.SQLParseEngineFactory;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.util.ConfigurationLogger;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.spi.database.DatabaseType;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Encrypt data source.
 *
 * @author panjuan
 */
@Getter
public class EncryptDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final DataSource dataSource;
    
    private final DatabaseType databaseType;
    
    private final EncryptRule encryptRule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final ShardingProperties shardingProperties;
    
    private final SQLParseEngine parseEngine;
    
    @Setter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    @SneakyThrows
    public EncryptDataSource(final DataSource dataSource, final EncryptRuleConfiguration encryptRuleConfiguration, final Properties props) {
        ConfigurationLogger.log(encryptRuleConfiguration);
        ConfigurationLogger.log(props);
        this.dataSource = dataSource;
        databaseType = getDatabaseType();
        encryptRule = new EncryptRule(encryptRuleConfiguration);
        shardingTableMetaData = createEncryptTableMetaData();
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        parseEngine = SQLParseEngineFactory.getSQLParseEngine(databaseType);
    }
    
    @SneakyThrows
    private ShardingTableMetaData createEncryptTableMetaData() {
        Map<String, TableMetaData> tables = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            for (String each : encryptRule.getEncryptTableNames()) {
                if (isTableExist(connection, each)) {
                    tables.put(each, new TableMetaData(getColumnMetaDataList(connection, each), getLogicIndexes(connection, each)));
                }
            }
        }
        return new ShardingTableMetaData(tables);
    }
    
    private boolean isTableExist(final Connection connection, final String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, null)) {
            return resultSet.next();
        }
    }
    
    private List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String tableName) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, tableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, tableName, "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String columnType = resultSet.getString("TYPE_NAME");
                result.add(new ColumnMetaData(columnName, columnType, primaryKeys.contains(columnName)));
            }
        }
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String tableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
    
    private Set<String> getLogicIndexes(final Connection connection, final String actualTableName) throws SQLException {
        Set<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getCatalog(), actualTableName, false, false)) {
            while (resultSet.next()) {
                Optional<String> logicIndex = getLogicIndex(resultSet.getString("INDEX_NAME"), actualTableName);
                if (logicIndex.isPresent()) {
                    result.add(logicIndex.get());
                }
            }
        }
        return result;
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        if (actualIndexName.contains(indexNameSuffix)) {
            return Optional.of(actualIndexName.replace(indexNameSuffix, ""));
        }
        return Optional.absent();
    }
    
    private DatabaseType getDatabaseType() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypes.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    @Override
    @SneakyThrows
    public EncryptConnection getConnection() {
        return new EncryptConnection(databaseType, dataSource.getConnection(), encryptRule, shardingTableMetaData, parseEngine, shardingProperties);
    }
    
    @Override
    @SneakyThrows
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public void close() {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
