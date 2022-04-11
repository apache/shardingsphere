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

package org.apache.shardingsphere.data.pipeline.postgresql.prepare.datasource;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.ActualTableDefinition;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.TableDefinitionSQLType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.AbstractDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Data source preparer for PostgresSQL.
 */
@Slf4j
public final class PostgreSQLDataSourcePreparer extends AbstractDataSourcePreparer {
    
    private static final String FETCH_CREATE_TABLE_TEMPLATE = "SELECT 'CREATE TABLE IF NOT EXISTS '||'%s'||'('||STRING_AGG"
            + "(column_list.column_expr, ',')||',PRIMARY KEY({PK_placeholder}))' FROM (SELECT *,column_name||' '||data_type||COALESCE('('"
            + " || character_maximum_length || ')', '')||CASE WHEN is_nullable = 'YES' then '' ELSE ' NOT NULL' END AS column_expr "
            + "FROM information_schema.columns WHERE table_name = '%s' AND table_schema='public' ORDER BY ordinal_position) column_list";
    
    private static final String FETCH_NORMAL_INDEXES_TEMPLATE = "SELECT schemaname,indexname,indexdef FROM pg_indexes WHERE tablename = "
            + "'%s' AND indexname != '%s' AND schemaname='public'";
    
    private static final String DROP_INDEX_TEMPLATE = "DROP INDEX IF EXISTS %s.%s";
    
    private static final String FETCH_PRIMARY_KEY_TEMPLATE = "SELECT constraint_name,column_name FROM information_schema"
            + ".key_column_usage WHERE table_name = '%s' AND table_schema='public'";
    
    private static final String COMMENT_TEMPLATE = "COMMENT ON %s %s IS '%s'";
    
    private static final String FETCH_TABLE_COLUMN_TEMPLATE = "SELECT ordinal_position,column_name FROM information_schema.columns WHERE "
            + "table_name = '%s' AND table_schema='public' ORDER BY ordinal_position";
    
    private static final String FETCH_COMMENT_TEMPLATE = "SELECT objsubid,description FROM pg_catalog.pg_description WHERE objoid = "
            + "'%s'::regclass";
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter parameter) {
        Collection<ActualTableDefinition> actualTableDefinitions;
        try {
            actualTableDefinitions = getActualTableDefinitions(parameter);
        } catch (final SQLException ex) {
            log.error("failed to get actual table definitions", ex);
            throw new PipelineJobPrepareFailedException("get table definitions failed.", ex);
        }
        Map<String, Collection<String>> createLogicTableSQLs = getCreateLogicTableSQLs(actualTableDefinitions);
        
        try (Connection targetConnection = getTargetCachedDataSource(parameter.getPipelineConfiguration(),
                parameter.getDataSourceManager()).getConnection()) {
            for (Entry<String, Collection<String>> entry : createLogicTableSQLs.entrySet()) {
                for (String each : entry.getValue()) {
                    executeTargetTableSQL(targetConnection, each);
                }
            }
        } catch (final SQLException ex) {
            throw new PipelineJobPrepareFailedException("prepare target tables failed.", ex);
        }
    }
    
    private Map<String, Collection<String>> getCreateLogicTableSQLs(final Collection<ActualTableDefinition> actualTableDefinitions) {
        Map<String, Collection<String>> result = new HashMap<>(actualTableDefinitions.size(), 1);
        for (ActualTableDefinition each : actualTableDefinitions) {
            Collection<String> logicTableSQLs = splitTableDefinitionToSQLs(each).stream().map(sql -> {
                TableDefinitionSQLType sqlType = getTableDefinitionSQLType(sql);
                switch (sqlType) {
                    case CREATE_TABLE:
                    case ALTER_TABLE:
                        return replaceActualTableNameToLogicTableName(sql, each.getActualTableName(), each.getLogicTableName());
                    case CREATE_INDEX:
                        return sql.replace(each.getActualTableName(), each.getLogicTableName());
                    case DROP_INDEX:
                        return sql.replace(each.getActualTableName(), each.getLogicTableName());
                    case COMMENT_ON:
                        return sql.replace(each.getActualTableName(), each.getLogicTableName());
                    default:
                        return "";
                }
            }).filter(sql -> !Strings.isNullOrEmpty(sql)).collect(Collectors.toList());
            result.put(each.getLogicTableName(), logicTableSQLs);
        }
        return result;
    }
    
    private Collection<ActualTableDefinition> getActualTableDefinitions(final PrepareTargetTablesParameter parameter) throws SQLException {
        Collection<ActualTableDefinition> result = new LinkedList<>();
        ShardingSpherePipelineDataSourceConfiguration sourceDataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration)
                PipelineDataSourceConfigurationFactory.newInstance(parameter.getPipelineConfiguration().getSource().getType(),
                        parameter.getPipelineConfiguration().getSource().getParameter());
        try (PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager()) {
            for (JobDataNodeEntry each : parameter.getTablesFirstDataNodes().getEntries()) {
                DataNode dataNode = each.getDataNodes().get(0);
                // TODO to remove if config is fix the problem
                final Map<String, Object> configMap =
                        sourceDataSourceConfig.getRootConfig().getDataSources().get(dataNode.getDataSourceName());
                configMap.put("jdbcUrl", configMap.get("url"));
                PipelineDataSourceWrapper dataSource =
                        dataSourceManager.getDataSource(sourceDataSourceConfig.getActualDataSourceConfig(dataNode.getDataSourceName()));
                try (Connection sourceConnection = dataSource.getConnection()) {
                    String actualTableName = dataNode.getTableName();
                    StringJoiner joiner = new StringJoiner(";");
                    Pair<String, List<String>> primaryKeyPair = queryTablePrimaryKey(sourceConnection, actualTableName);
                    joiner.add(queryCreateTableSql(sourceConnection, actualTableName, primaryKeyPair.getRight()));
                    queryCreateIndexes(sourceConnection, actualTableName, primaryKeyPair.getLeft()).forEach(joiner::add);
                    queryCommentOnList(sourceConnection, actualTableName).forEach(joiner::add);
                    String tableDefinition = joiner.toString();
                    result.add(new ActualTableDefinition(each.getLogicTableName(), actualTableName, tableDefinition));
                }
            }
        }
        return result;
    }
    
    private Pair<String, List<String>> queryTablePrimaryKey(final Connection sourceConnection, final String actualTableName) throws SQLException {
        String primaryKeyName = null;
        List<String> primaryKeyColumns = new LinkedList<>();
        try (Statement statement = sourceConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(String.format(FETCH_PRIMARY_KEY_TEMPLATE, actualTableName))) {
            while (resultSet.next()) {
                primaryKeyName = primaryKeyName == null ? resultSet.getString(1) : primaryKeyName;
                primaryKeyColumns.add(resultSet.getString(2));
            }
            if (primaryKeyColumns.size() == 0 || primaryKeyName == null) {
                throw new PipelineJobPrepareFailedException("not support no primary key table:" + actualTableName);
            }
        }
        return Pair.of(primaryKeyName, primaryKeyColumns);
    }
    
    private String queryCreateTableSql(final Connection sourceConnection, final String actualTableName, final List<String> pkColumns) throws SQLException {
        final String sql = String.format(FETCH_CREATE_TABLE_TEMPLATE, actualTableName, actualTableName);
        try (Statement statement = sourceConnection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new PipelineJobPrepareFailedException("table struct has no result, sql: " + sql);
            }
            return resultSet.getString(1).replace("{PK_placeholder}", String.join(",", pkColumns));
        }
    }
    
    private List<String> queryCreateIndexes(final Connection sourceConnection, final String actualTableName, final String pkName) throws SQLException {
        List<String> result = new LinkedList<>();
        try (Statement statement = sourceConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(String.format(FETCH_NORMAL_INDEXES_TEMPLATE, actualTableName, pkName))) {
            while (resultSet.next()) {
                // TODO add drop index first, make sure the index is not exist
                result.add(String.format(DROP_INDEX_TEMPLATE, resultSet.getString(1), resultSet.getString(2)));
                result.add(resultSet.getString(3));
            }
        }
        return result;
    }
    
    private List<String> queryCommentOnList(final Connection sourceConnection, final String actualTableName) throws SQLException {
        final String fetchCommentSql = String.format(FETCH_COMMENT_TEMPLATE, actualTableName);
        List<String> result = new LinkedList<>();
        Map<Integer, String> commentMap = Maps.newHashMap();
        try (Statement statement = sourceConnection.createStatement();
             ResultSet commentResult = statement.executeQuery(fetchCommentSql)) {
            while (commentResult.next()) {
                commentMap.put(commentResult.getInt(1), commentResult.getString(2));
            }
            String tableComment = commentMap.remove(0);
            if (!Strings.isNullOrEmpty(tableComment)) {
                result.add(String.format(COMMENT_TEMPLATE, "TABLE", actualTableName, tableComment));
            }
        }
        final String fetchColumnSql = String.format(FETCH_TABLE_COLUMN_TEMPLATE, actualTableName);
        try (Statement statement = sourceConnection.createStatement();
             ResultSet columnsResult = statement.executeQuery(fetchColumnSql)) {
            while (columnsResult.next()) {
                String columnComment = commentMap.get(columnsResult.getInt(1));
                if (columnComment != null) {
                    result.add(String.format(COMMENT_TEMPLATE, "COLUMN",
                            new StringJoiner(".").add(actualTableName).add(columnsResult.getString(2)), columnComment));
                }
            }
        }
        return result;
    }
}
