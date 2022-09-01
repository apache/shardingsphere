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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.dialect;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ViewMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DataTypeLoaderFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schema meta data loader for openGauss.
 */
public final class OpenGaussSchemaMetaDataLoader implements DialectSchemaMetaDataLoader {
    
    private static final String BASIC_TABLE_META_DATA_SQL = "SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default, table_schema"
            + " FROM information_schema.columns WHERE table_schema IN (%s)";
    
    private static final String TABLE_META_DATA_SQL_WITHOUT_TABLES = BASIC_TABLE_META_DATA_SQL + " ORDER BY ordinal_position";
    
    private static final String VIEW_META_DATA_SQL = "SELECT table_name, view_definition FROM information_schema.views WHERE table_schema = ?";
    
    private static final String TABLE_META_DATA_SQL_WITH_TABLES = BASIC_TABLE_META_DATA_SQL + " AND table_name IN (%s) ORDER BY ordinal_position";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT tc.table_name, kc.column_name, kc.table_schema FROM information_schema.table_constraints tc"
            + " JOIN information_schema.key_column_usage kc ON kc.table_schema = tc.table_schema AND kc.table_name = tc.table_name AND kc.constraint_name = tc.constraint_name"
            + " WHERE tc.constraint_type = 'PRIMARY KEY' AND kc.ordinal_position IS NOT NULL AND kc.table_schema IN (%s)";
    
    private static final String BASIC_INDEX_META_DATA_SQL = "SELECT tablename, indexname, schemaname FROM pg_indexes WHERE schemaname IN (%s)";
    
    @Override
    public Collection<SchemaMetaData> load(final DataSource dataSource, final Collection<String> tables, final String defaultSchemaName) throws SQLException {
        Collection<String> schemaNames = loadSchemaNames(dataSource, DatabaseTypeFactory.getInstance(getType()));
        Map<String, Multimap<String, IndexMetaData>> schemaIndexMetaDataMap = loadIndexMetaDataMap(dataSource, schemaNames);
        Map<String, Multimap<String, ColumnMetaData>> schemaColumnMetaDataMap = loadColumnMetaDataMap(dataSource, tables, schemaNames);
        Collection<SchemaMetaData> result = new LinkedList<>();
        for (String each : schemaNames) {
            Multimap<String, IndexMetaData> tableIndexMetaDataMap = schemaIndexMetaDataMap.getOrDefault(each, LinkedHashMultimap.create());
            Multimap<String, ColumnMetaData> tableColumnMetaDataMap = schemaColumnMetaDataMap.getOrDefault(each, LinkedHashMultimap.create());
            result.add(new SchemaMetaData(each, createTableMetaDataList(tableIndexMetaDataMap, tableColumnMetaDataMap), loadViewMetaData(dataSource, each)));
        }
        return result;
    }
    
    private Collection<TableMetaData> createTableMetaDataList(final Multimap<String, IndexMetaData> tableIndexMetaDataMap, final Multimap<String, ColumnMetaData> tableColumnMetaDataMap) {
        Collection<TableMetaData> result = new LinkedList<>();
        for (String each : tableColumnMetaDataMap.keySet()) {
            Collection<ColumnMetaData> columnMetaDataList = tableColumnMetaDataMap.get(each);
            Collection<IndexMetaData> indexMetaDataList = tableIndexMetaDataMap.get(each);
            result.add(new TableMetaData(each, columnMetaDataList, indexMetaDataList, Collections.emptyList()));
        }
        return result;
    }
    
    private Collection<ViewMetaData> loadViewMetaData(final DataSource dataSource, final String schemaName) throws SQLException {
        Collection<ViewMetaData> result = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(VIEW_META_DATA_SQL)) {
            preparedStatement.setString(1, schemaName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("table_name");
                    String viewDefinition = resultSet.getString("view_definition");
                    result.add(new ViewMetaData(tableName, viewDefinition));
                }
            }
        }
        return result;
    }
    
    private Map<String, Multimap<String, ColumnMetaData>> loadColumnMetaDataMap(final DataSource dataSource, final Collection<String> tables,
                                                                                final Collection<String> schemaNames) throws SQLException {
        Map<String, Multimap<String, ColumnMetaData>> result = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(getColumnMetaDataSQL(schemaNames, tables))) {
            Map<String, Integer> dataTypes = DataTypeLoaderFactory.getInstance(DatabaseTypeFactory.getInstance("openGauss")).load(connection.getMetaData());
            Set<String> primaryKeys = loadPrimaryKeys(connection, schemaNames);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("table_name");
                    String schemaName = resultSet.getString("table_schema");
                    Multimap<String, ColumnMetaData> columnMetaDataMap = result.computeIfAbsent(schemaName, key -> LinkedHashMultimap.create());
                    columnMetaDataMap.put(tableName, loadColumnMetaData(dataTypes, primaryKeys, resultSet));
                }
            }
        }
        return result;
    }
    
    private Set<String> loadPrimaryKeys(final Connection connection, final Collection<String> schemaNames) throws SQLException {
        Set<String> result = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getPrimaryKeyMetaDataSQL(schemaNames))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("table_schema");
                    String tableName = resultSet.getString("table_name");
                    String columnName = resultSet.getString("column_name");
                    result.add(schemaName + "," + tableName + "," + columnName);
                }
            }
        }
        return result;
    }
    
    private String getPrimaryKeyMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(PRIMARY_KEY_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private ColumnMetaData loadColumnMetaData(final Map<String, Integer> dataTypeMap, final Set<String> primaryKeys, final ResultSet resultSet) throws SQLException {
        String schemaName = resultSet.getString("table_schema");
        String tableName = resultSet.getString("table_name");
        String columnName = resultSet.getString("column_name");
        String dataType = resultSet.getString("udt_name");
        boolean isPrimaryKey = primaryKeys.contains(schemaName + "," + tableName + "," + columnName);
        String columnDefault = resultSet.getString("column_default");
        boolean generated = null != columnDefault && columnDefault.startsWith("nextval(");
        // TODO user defined collation which deterministic is false
        boolean caseSensitive = true;
        return new ColumnMetaData(columnName, dataTypeMap.get(dataType), isPrimaryKey, generated, caseSensitive, true);
    }
    
    private String getColumnMetaDataSQL(final Collection<String> schemaNames, final Collection<String> tables) {
        String schemaNameParameter = schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(","));
        return tables.isEmpty() ? String.format(TABLE_META_DATA_SQL_WITHOUT_TABLES, schemaNameParameter)
                : String.format(TABLE_META_DATA_SQL_WITH_TABLES, schemaNameParameter, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Multimap<String, IndexMetaData>> loadIndexMetaDataMap(final DataSource dataSource, final Collection<String> schemaNames) throws SQLException {
        Map<String, Multimap<String, IndexMetaData>> result = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(schemaNames))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("schemaname");
                    String tableName = resultSet.getString("tablename");
                    String indexName = resultSet.getString("indexname");
                    Multimap<String, IndexMetaData> indexMetaDataMap = result.computeIfAbsent(schemaName, key -> LinkedHashMultimap.create());
                    indexMetaDataMap.put(tableName, new IndexMetaData(indexName));
                }
            }
        }
        return result;
    }
    
    private String getIndexMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(BASIC_INDEX_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
