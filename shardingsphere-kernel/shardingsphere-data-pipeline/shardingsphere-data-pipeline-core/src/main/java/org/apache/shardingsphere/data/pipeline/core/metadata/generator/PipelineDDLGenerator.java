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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CommentStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.type.ConstraintAvailable;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLSQLGeneratorFactory;
import org.apache.shardingsphere.infra.metadata.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Pipeline ddl generator.
 */
@RequiredArgsConstructor
public final class PipelineDDLGenerator {
    
    private static final String DELIMITER = ";";
    
    private final ContextManager contextManager;
    
    /**
     * Generate logic ddl sql.
     *
     * @param databaseType database type
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return ddl SQL
     */
    @SneakyThrows
    public String generateLogicDDLSQL(final DatabaseType databaseType, final String databaseName, final String schemaName, final String tableName) {
        ShardingSphereDatabaseMetaData databaseMetaData = contextManager.getMetaDataContexts().getDatabaseMetaData(databaseName);
        String sql = generateActualDDLSQL(databaseType, schemaName, tableName, databaseMetaData);
        StringBuilder result = new StringBuilder();
        for (String each : sql.split(DELIMITER)) {
            if (!each.trim().isEmpty()) {
                result.append(decorateActualSQL(each.trim(), databaseMetaData, databaseType, databaseName)).append(DELIMITER).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    /**
     * Replace table name with prefix.
     *
     * @param sql sql
     * @param prefix prefix
     * @param databaseType database type
     * @param databaseName database name
     * @return replaced sql
     */
    public String replaceTableNameWithPrefix(final String sql, final String prefix, final DatabaseType databaseType, final String databaseName) {
        LogicSQL logicSQL = getLogicSQL(sql, databaseType, databaseName);
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        if (sqlStatementContext instanceof CreateTableStatementContext || sqlStatementContext instanceof CommentStatementContext || sqlStatementContext instanceof CreateIndexStatementContext) {
            if (!sqlStatementContext.getTablesContext().getTables().isEmpty()) {
                TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getTables().iterator().next().getTableName();
                Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
                replaceMap.put(tableNameSegment, prefix + tableNameSegment.getIdentifier().getValue());
                return doDecorateActualTable(replaceMap, sql);
            }
        }
        return sql;
    }
    
    private String generateActualDDLSQL(final DatabaseType databaseType, final String schemaName, final String tableName, final ShardingSphereDatabaseMetaData databaseMetaData) throws SQLException {
        DataNodes dataNodes = new DataNodes(databaseMetaData.getRuleMetaData().getRules());
        Optional<DataNode> optional = dataNodes.getDataNodes(tableName).stream()
                .filter(dataNode -> databaseMetaData.getResource().getDataSources().containsKey(dataNode.getDataSourceName().contains(".")
                        ? dataNode.getDataSourceName().split("\\.")[0]
                        : dataNode.getDataSourceName()))
                .findFirst();
        String dataSourceName = optional.map(DataNode::getDataSourceName).orElseGet(() -> databaseMetaData.getResource().getDataSources().keySet().iterator().next());
        String actualTable = optional.map(DataNode::getTableName).orElse(tableName);
        return DialectDDLSQLGeneratorFactory.findInstance(databaseType).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"))
                .generateDDLSQL(actualTable, schemaName, databaseMetaData.getResource().getDataSources().get(dataSourceName));
    }
    
    private String decorateActualSQL(final String sql, final ShardingSphereDatabaseMetaData databaseMetaData, final DatabaseType databaseType, final String databaseName) {
        LogicSQL logicSQL = getLogicSQL(sql, databaseType, databaseName);
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, databaseMetaData, sqlStatementContext);
            appendFromTable(replaceMap, databaseMetaData, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CommentStatementContext) {
            appendFromTable(replaceMap, databaseMetaData, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CreateIndexStatementContext) {
            appendFromTable(replaceMap, databaseMetaData, (TableAvailable) sqlStatementContext);
            appendFromIndexAndConstraint(replaceMap, databaseMetaData, sqlStatementContext);
        }
        if (sqlStatementContext instanceof AlterTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, databaseMetaData, sqlStatementContext);
            appendFromTable(replaceMap, databaseMetaData, (TableAvailable) sqlStatementContext);
        }
        return doDecorateActualTable(replaceMap, sql);
    }
    
    private void appendFromIndexAndConstraint(final Map<SQLSegment, String> replaceMap, final ShardingSphereDatabaseMetaData databaseMetaData, final SQLStatementContext<?> sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable) || ((TableAvailable) sqlStatementContext).getTablesContext().getTables().isEmpty()) {
            return;
        }
        TableNameSegment tableNameSegment = ((TableAvailable) sqlStatementContext).getTablesContext().getTables().iterator().next().getTableName();
        String logicTable = findLogicTable(tableNameSegment, databaseMetaData);
        if (!tableNameSegment.getIdentifier().getValue().equals(logicTable)) {
            if (sqlStatementContext instanceof IndexAvailable) {
                for (IndexSegment each : ((IndexAvailable) sqlStatementContext).getIndexes()) {
                    String logicIndexName = IndexMetaDataUtil.getLogicIndexName(each.getIndexName().getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each.getIndexName(), logicIndexName);
                }
            }
            if (sqlStatementContext instanceof ConstraintAvailable) {
                for (ConstraintSegment each : ((ConstraintAvailable) sqlStatementContext).getConstraints()) {
                    String logicConstraint = IndexMetaDataUtil.getLogicIndexName(each.getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each, logicConstraint);
                }
            }
        }
    }
    
    private void appendFromTable(final Map<SQLSegment, String> replaceMap, final ShardingSphereDatabaseMetaData databaseMetaData, final TableAvailable sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getAllTables()) {
            String logicTable = findLogicTable(each.getTableName(), databaseMetaData);
            if (!logicTable.equals(each.getTableName().getIdentifier().getValue())) {
                replaceMap.put(each.getTableName(), logicTable);
            }
        }
    }
    
    private String doDecorateActualTable(final Map<SQLSegment, String> replaceMap, final String sql) {
        StringBuilder result = new StringBuilder();
        int lastStopIndex = 0;
        for (Entry<SQLSegment, String> entry : replaceMap.entrySet()) {
            result.append(sql, lastStopIndex, entry.getKey().getStartIndex());
            result.append(entry.getValue());
            lastStopIndex = entry.getKey().getStopIndex() + 1;
        }
        if (lastStopIndex < sql.length()) {
            result.append(sql, lastStopIndex, sql.length());
        }
        return result.toString();
    }
    
    private String findLogicTable(final TableNameSegment tableNameSegment, final ShardingSphereDatabaseMetaData databaseMetaData) {
        String actualTable = tableNameSegment.getIdentifier().getValue();
        return databaseMetaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule)
                .map(each -> ((DataNodeContainedRule) each).findLogicTableByActualTable(actualTable).orElse(null)).filter(Objects::nonNull).findFirst().orElse(actualTable);
    }
    
    private LogicSQL getLogicSQL(final String sql, final DatabaseType databaseType, final String databaseName) {
        Optional<SQLParserRule> sqlParserRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType.getType(), sqlParserRule.get().toParserConfiguration()).parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(contextManager.getMetaDataContexts().getDatabaseMetaDataMap(),
                sqlStatement, databaseName);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
}
