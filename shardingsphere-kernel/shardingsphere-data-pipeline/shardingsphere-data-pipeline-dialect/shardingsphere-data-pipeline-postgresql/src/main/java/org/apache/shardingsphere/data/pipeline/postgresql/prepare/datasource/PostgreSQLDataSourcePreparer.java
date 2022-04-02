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
import lombok.extern.slf4j.Slf4j;
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
 * Data source preparer for MySQL.
 */
@Slf4j
public final class PostgreSQLDataSourcePreparer extends AbstractDataSourcePreparer {
    
    
    private static final String FETCH_CREATE_TABLE_TEMPLATE = "SELECT 'CREATE TABLE IF NOT EXISTS '||'%s'||'" +
            "('||STRING_AGG(column_list.column_expr, ',')||');' FROM (SELECT *,column_name||' '||data_type||COALESCE" +
            "('(' || character_maximum_length || ')', '')||CASE WHEN is_nullable = 'YES' then '' ELSE ' NOT NULL' END AS column_expr " +
            "FROM information_schema.columns WHERE table_name = '%s' AND table_schema = '%s' ORDER BY ordinal_position) column_list;";
    
    private static final String FETCH_CREATE_INDEXES_TEMPLATE = "SELECT indexdef FROM pg_indexes WHERE tablename = '%s'";
    
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter parameter) {
        // 拿到物理数据源
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
                        return replaceActualTableNameToLogicTableName(sql, each.getActualTableName(), each.getLogicTableName());
                    case ALTER_TABLE:
                        return replaceActualTableNameToLogicTableName(sql, each.getActualTableName(), each.getLogicTableName());
                    case CREATE_INDEX:
                        return rewriteActualCreateIndexSql(sql, each.getActualTableName(), each.getLogicTableName());
                    case COMMENT_ON:
                        // todo not support now
                    default:
                        return "";
                }
            }).filter(sql -> !Strings.isNullOrEmpty(sql)).collect(Collectors.toList());
            result.put(each.getLogicTableName(), logicTableSQLs);
        }
        return result;
    }
    
    private String rewriteActualCreateIndexSql(String sql, String actualTableName, String logicTableName) {
        return sql.replace(actualTableName, logicTableName);
    }
    
    private Collection<ActualTableDefinition> getActualTableDefinitions(final PrepareTargetTablesParameter parameter) throws SQLException {
        Collection<ActualTableDefinition> result = new LinkedList<>();
        ShardingSpherePipelineDataSourceConfiguration sourceDataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.
                newInstance(parameter.getPipelineConfiguration().getSource().getType(), parameter.getPipelineConfiguration().getSource().getParameter());
        
        try (PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager()) {
            for (JobDataNodeEntry each : parameter.getTablesFirstDataNodes().getEntries()) {
                DataNode dataNode = each.getDataNodes().get(0);
                final Map<String, Object> configMap = sourceDataSourceConfig.getRootConfig().getDataSources().get(dataNode.getDataSourceName());
                configMap.put("jdbcUrl", configMap.get("url"));
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(sourceDataSourceConfig.getActualDataSourceConfig(dataNode.getDataSourceName()));
                try (Connection sourceConnection = dataSource.getConnection()) {
                    String actualTableName = dataNode.getTableName();
                    StringJoiner joiner = new StringJoiner(";");
                    joiner.add(queryCreateTableSql(sourceConnection, actualTableName));
                    queryCreateIndexes(sourceConnection, actualTableName).forEach(joiner::add);
                    String logicTableName = each.getLogicTableName();
                    String tableDefinition = joiner.toString();
                    log.info("===" + tableDefinition);
                    result.add(new ActualTableDefinition(logicTableName, actualTableName, tableDefinition));
                }
            }
        }
        return result;
    }
    
    private String queryCreateTableSql(final Connection sourceConnection, final String actualTableName) throws SQLException {
        final String sql = String.format(FETCH_CREATE_TABLE_TEMPLATE, actualTableName, actualTableName, "public");
        try (Statement statement = sourceConnection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new PipelineJobPrepareFailedException("table struct has no result, sql: " + sql);
            }
            return resultSet.getString(1);
        }
    }
    
    private List<String> queryCreateIndexes(final Connection sourceConnection, final String actualTableName) throws SQLException {
        List<String> result = new LinkedList<>();
        try (Statement statement = sourceConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(String.format(FETCH_CREATE_INDEXES_TEMPLATE, actualTableName))) {
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        }
        return result;
    }
}
