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

package org.apache.shardingsphere.database.connector.postgresql.metadata.data.loader;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.SchemaMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Meta data loader for PostgreSQL.
 */
public final class PostgreSQLMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String BASIC_TABLE_META_DATA_SQL = "SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default, table_schema, is_nullable"
            + " FROM information_schema.columns WHERE table_schema IN (%s)";
    
    private static final String TABLE_META_DATA_SQL_WITHOUT_TABLES = BASIC_TABLE_META_DATA_SQL + " ORDER BY ordinal_position";
    
    private static final String TABLE_META_DATA_SQL_WITH_TABLES = BASIC_TABLE_META_DATA_SQL + " AND table_name IN (%s) ORDER BY ordinal_position";
    
    private static final String FOREIGN_KEY_META_DATA_SQL = "SELECT tc.table_schema,tc.table_name,tc.constraint_name,pgo.relname refer_table_name "
            + "FROM information_schema.table_constraints tc "
            + "JOIN pg_constraint pgc ON tc.constraint_name = pgc.conname AND contype='f' "
            + "JOIN pg_class pgo ON pgc.confrelid = pgo.oid WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema IN (%s)";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT tc.table_name, kc.column_name, kc.table_schema FROM information_schema.table_constraints tc"
            + " JOIN information_schema.key_column_usage kc ON kc.table_schema = tc.table_schema AND kc.table_name = tc.table_name AND kc.constraint_name = tc.constraint_name"
            + " WHERE tc.constraint_type = 'PRIMARY KEY' AND kc.ordinal_position IS NOT NULL AND kc.table_schema IN (%s)";
    
    private static final String BASIC_INDEX_META_DATA_SQL = "SELECT tablename, indexname, schemaname FROM pg_indexes WHERE schemaname IN (%s)";
    
    private static final String ADVANCE_INDEX_META_DATA_SQL =
            "SELECT idx.relname as index_name, insp.nspname as index_schema, tbl.relname as table_name, att.attname AS column_name, pgi.indisunique as is_unique"
                    + " FROM pg_index pgi JOIN pg_class idx ON idx.oid = pgi.indexrelid JOIN pg_namespace insp ON insp.oid = idx.relnamespace JOIN pg_class tbl ON tbl.oid = pgi.indrelid"
                    + " JOIN pg_namespace tnsp ON tnsp.oid = tbl.relnamespace JOIN pg_attribute att ON att.attrelid = tbl.oid AND att.attnum = ANY(pgi.indkey) WHERE tnsp.nspname IN (%s)";
    
    private static final String LOAD_ALL_ROLE_TABLE_GRANTS_SQL = "SELECT table_name FROM information_schema.role_table_grants";
    
    private static final String LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL = LOAD_ALL_ROLE_TABLE_GRANTS_SQL + " WHERE table_name IN (%s)";
    
    private static final String VIEW_META_DATA_SQL = "SELECT table_schema, table_name FROM information_schema.views WHERE table_schema IN (%s) and table_name IN (%s)";
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        try (Connection connection = material.getDataSource().getConnection()) {
            Collection<String> schemaNames = new SchemaMetaDataLoader(getType()).loadSchemaNames(connection);
            Map<String, Multimap<String, IndexMetaData>> schemaIndexMetaDataMap = loadIndexMetaDataMap(connection, schemaNames);
            Map<String, Multimap<String, ColumnMetaData>> schemaColumnMetaDataMap = loadColumnMetaDataMap(connection, material.getActualTableNames(), schemaNames);
            Map<String, Multimap<String, ConstraintMetaData>> schemaConstraintMetaDataMap = loadConstraintMetaDataMap(connection, schemaNames);
            Map<String, Collection<String>> schemaViewNames = loadViewNames(connection, schemaNames, material.getActualTableNames());
            Collection<SchemaMetaData> result = new LinkedList<>();
            for (String each : schemaNames) {
                Multimap<String, IndexMetaData> tableIndexMetaDataMap = schemaIndexMetaDataMap.getOrDefault(each, LinkedHashMultimap.create());
                Multimap<String, ColumnMetaData> tableColumnMetaDataMap = schemaColumnMetaDataMap.getOrDefault(each, LinkedHashMultimap.create());
                Multimap<String, ConstraintMetaData> tableConstraintMetaDataMap = schemaConstraintMetaDataMap.getOrDefault(each, LinkedHashMultimap.create());
                Collection<String> viewNames = schemaViewNames.getOrDefault(each, Collections.emptySet());
                result.add(new SchemaMetaData(each, createTableMetaDataList(tableIndexMetaDataMap, tableColumnMetaDataMap, tableConstraintMetaDataMap, viewNames)));
            }
            return result;
        }
    }
    
    private Map<String, Multimap<String, IndexMetaData>> loadIndexMetaDataMap(final Connection connection, final Collection<String> schemaNames) throws SQLException {
        Map<String, Multimap<String, IndexMetaData>> result = new LinkedHashMap<>(schemaNames.size(), 1F);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(schemaNames));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("schemaname");
                String tableName = resultSet.getString("tablename");
                String indexName = resultSet.getString("indexname");
                Multimap<String, IndexMetaData> indexMetaDataMap = result.computeIfAbsent(schemaName, key -> LinkedHashMultimap.create());
                indexMetaDataMap.put(tableName, new IndexMetaData(indexName));
            }
        }
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getAdvanceIndexMetaDataSQL(schemaNames));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("index_schema");
                String tableName = resultSet.getString("table_name");
                String columnName = resultSet.getString("column_name");
                String indexName = resultSet.getString("index_name");
                boolean isUnique = resultSet.getBoolean("is_unique");
                Collection<IndexMetaData> indexMetaDatas = result.getOrDefault(schemaName, LinkedHashMultimap.create()).get(tableName);
                if (indexMetaDatas.isEmpty()) {
                    continue;
                }
                Optional<IndexMetaData> indexMetaData = indexMetaDatas.stream().filter(each -> each.getName().equals(indexName)).findFirst();
                if (indexMetaData.isPresent()) {
                    indexMetaData.get().setUnique(isUnique);
                    indexMetaData.get().getColumns().add(columnName);
                }
            }
        }
        return result;
    }
    
    private String getIndexMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(BASIC_INDEX_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private String getAdvanceIndexMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(ADVANCE_INDEX_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Multimap<String, ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables,
                                                                                final Collection<String> schemaNames) throws SQLException {
        Map<String, Multimap<String, ColumnMetaData>> result = new LinkedHashMap<>(schemaNames.size(), 1F);
        Collection<String> roleTableGrants = loadRoleTableGrants(connection, tables);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getColumnMetaDataSQL(schemaNames, tables));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            Collection<String> primaryKeys = loadPrimaryKeys(connection, schemaNames);
            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                if (!roleTableGrants.contains(tableName)) {
                    continue;
                }
                String schemaName = resultSet.getString("table_schema");
                Multimap<String, ColumnMetaData> columnMetaDataMap = result.computeIfAbsent(schemaName, key -> LinkedHashMultimap.create());
                columnMetaDataMap.put(tableName, loadColumnMetaData(primaryKeys, resultSet));
            }
        }
        return result;
    }
    
    private Collection<String> loadRoleTableGrants(final Connection connection, final Collection<String> tables) throws SQLException {
        Collection<String> result = new HashSet<>(tables.size(), 1F);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getLoadRoleTableGrantsSQL(tables));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                result.add(resultSet.getString("table_name"));
            }
        }
        return result;
    }
    
    private String getLoadRoleTableGrantsSQL(final Collection<String> tables) {
        return tables.isEmpty() ? LOAD_ALL_ROLE_TABLE_GRANTS_SQL
                : String.format(LOAD_FILTERED_ROLE_TABLE_GRANTS_SQL, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private String getColumnMetaDataSQL(final Collection<String> schemaNames, final Collection<String> tables) {
        String schemaNameParam = schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(","));
        return tables.isEmpty() ? String.format(TABLE_META_DATA_SQL_WITHOUT_TABLES, schemaNameParam)
                : String.format(TABLE_META_DATA_SQL_WITH_TABLES, schemaNameParam, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Set<String> loadPrimaryKeys(final Connection connection, final Collection<String> schemaNames) throws SQLException {
        Set<String> result = new HashSet<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getPrimaryKeyMetaDataSQL(schemaNames));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("table_schema");
                String tableName = resultSet.getString("table_name");
                String columnName = resultSet.getString("column_name");
                result.add(schemaName + "," + tableName + "," + columnName);
            }
        }
        return result;
    }
    
    private String getPrimaryKeyMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(PRIMARY_KEY_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private ColumnMetaData loadColumnMetaData(final Collection<String> primaryKeys, final ResultSet resultSet) throws SQLException {
        String schemaName = resultSet.getString("table_schema");
        String tableName = resultSet.getString("table_name");
        String columnName = resultSet.getString("column_name");
        String dataType = resultSet.getString("udt_name");
        boolean isPrimaryKey = primaryKeys.contains(schemaName + "," + tableName + "," + columnName);
        String columnDefault = resultSet.getString("column_default");
        boolean generated = null != columnDefault && columnDefault.startsWith("nextval(");
        // TODO user defined collation which deterministic is false
        boolean caseSensitive = true;
        boolean isNullable = "YES".equals(resultSet.getString("is_nullable"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), isPrimaryKey, generated,dataType, caseSensitive, true, false, isNullable);
    }
    
    private Map<String, Multimap<String, ConstraintMetaData>> loadConstraintMetaDataMap(final Connection connection, final Collection<String> schemaNames) throws SQLException {
        Map<String, Multimap<String, ConstraintMetaData>> result = new LinkedHashMap<>(schemaNames.size(), 1F);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getConstraintKeyMetaDataSQL(schemaNames));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("table_schema");
                Multimap<String, ConstraintMetaData> constraintMetaData = result.computeIfAbsent(schemaName, key -> LinkedHashMultimap.create());
                String tableName = resultSet.getString("table_name");
                String constraintName = resultSet.getString("constraint_name");
                String referencedTableName = resultSet.getString("refer_table_name");
                constraintMetaData.put(tableName, new ConstraintMetaData(constraintName, referencedTableName));
            }
        }
        return result;
    }
    
    private String getConstraintKeyMetaDataSQL(final Collection<String> schemaNames) {
        return String.format(FOREIGN_KEY_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<String>> loadViewNames(final Connection connection, final Collection<String> schemaNames, final Collection<String> tables) throws SQLException {
        Map<String, Collection<String>> result = new LinkedHashMap<>(schemaNames.size(), 1F);
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getViewMetaDataSQL(schemaNames, tables));
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String schemaName = resultSet.getString("table_schema");
                Collection<String> viewMetaData = result.computeIfAbsent(schemaName, key -> new HashSet<>());
                String tableName = resultSet.getString("table_name");
                viewMetaData.add(tableName);
            }
        }
        return result;
    }
    
    private String getViewMetaDataSQL(final Collection<String> schemaNames, final Collection<String> tables) {
        return String.format(VIEW_META_DATA_SQL, schemaNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")),
                tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Collection<TableMetaData> createTableMetaDataList(final Multimap<String, IndexMetaData> tableIndexMetaDataMap, final Multimap<String, ColumnMetaData> tableColumnMetaDataMap,
                                                              final Multimap<String, ConstraintMetaData> tableConstraintMetaDataMap, final Collection<String> viewNames) {
        Collection<TableMetaData> result = new LinkedList<>();
        for (String each : tableColumnMetaDataMap.keySet()) {
            Collection<ColumnMetaData> columnMetaDataList = tableColumnMetaDataMap.get(each);
            Collection<IndexMetaData> indexMetaDataList = tableIndexMetaDataMap.get(each);
            Collection<ConstraintMetaData> constraintMetaDataList = tableConstraintMetaDataMap.get(each);
            result.add(new TableMetaData(each, columnMetaDataList, indexMetaDataList, constraintMetaDataList, viewNames.contains(each) ? TableType.VIEW : TableType.TABLE));
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
