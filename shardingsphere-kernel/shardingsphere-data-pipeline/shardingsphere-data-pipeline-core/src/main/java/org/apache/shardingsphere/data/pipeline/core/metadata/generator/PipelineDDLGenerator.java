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
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLSQLGeneratorFactory;
import org.apache.shardingsphere.infra.metadata.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * Pipeline ddl generator.
 */
@RequiredArgsConstructor
public final class PipelineDDLGenerator {
    
    private static final String DELIMITER = ";";
    
    private static final String NEWLINE = "\n";
    
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
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData(databaseName);
        String sql = generateActualDDLSQL(databaseType, schemaName, tableName, metaData);
        StringBuilder result = new StringBuilder();
        for (String each : sql.split(DELIMITER)) {
            if (!each.trim().isEmpty()) {
                result.append(decorateActualSQL(each.trim(), metaData, databaseType, databaseName)).append(DELIMITER + NEWLINE);
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
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getTables().iterator().next().getTableName();
            return replace(sql, tableNameSegment, prefix + tableNameSegment.getIdentifier().getValue());
        }
        // TODO COMMENT STATEMENT
        return sql;
    }
    
    private String generateActualDDLSQL(final DatabaseType databaseType, final String schemaName, final String tableName, final ShardingSphereMetaData metaData) throws SQLException {
        DataNodes dataNodes = new DataNodes(metaData.getRuleMetaData().getRules());
        Optional<DataNode> optional = dataNodes.getDataNodes(tableName).stream().filter(dataNode -> metaData.getResource().getDataSources().containsKey(dataNode.getDataSourceName().contains(".")
                ? dataNode.getDataSourceName().split("\\.")[0]
                : dataNode.getDataSourceName())).findFirst();
        String dataSourceName = optional.map(DataNode::getDataSourceName).orElseGet(() -> metaData.getResource().getDataSources().keySet().iterator().next());
        String actualTable = optional.map(DataNode::getTableName).orElse(tableName);
        return DialectDDLSQLGeneratorFactory.findInstance(databaseType).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"))
                .generateDDLSQL(actualTable, schemaName, metaData.getResource().getDataSources().get(dataSourceName));
    }
    
    private String decorateActualSQL(final String sql, final ShardingSphereMetaData metaData, final DatabaseType databaseType, final String databaseName) {
        LogicSQL logicSQL = getLogicSQL(sql, databaseType, databaseName);
        String result = logicSQL.getSql();
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            result = decorateIndex(metaData, result, (CreateTableStatementContext) sqlStatementContext);
            result = decorateTable(metaData, result, (CreateTableStatementContext) sqlStatementContext);
        }
        // TODO COMMENT STATEMENT
        return result;
    }
    
    private String decorateTable(final ShardingSphereMetaData metaData, final String sql, final CreateTableStatementContext sqlStatementContext) {
        String result = sql;
        for (SimpleTableSegment each : getAllTableSegments(sqlStatementContext)) {
            String logicTable = findLogicTable(each.getTableName(), metaData);
            if (!logicTable.equals(each.getTableName().getIdentifier().getValue())) {
                result = replace(result, each.getTableName(), logicTable);
            }
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> getAllTableSegments(final CreateTableStatementContext sqlStatementContext) {
        Collection<SimpleTableSegment> result = new LinkedList<>(sqlStatementContext.getTablesContext().getTables());
        for (ConstraintDefinitionSegment each : sqlStatementContext.getSqlStatement().getConstraintDefinitions()) {
            each.getReferencedTable().ifPresent(result::add);
        }
        return result;
    }
    
    private String decorateIndex(final ShardingSphereMetaData metaData, final String sql, final CreateTableStatementContext sqlStatementContext) {
        String result = sql;
        TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getTables().iterator().next().getTableName();
        String logicTable = findLogicTable(tableNameSegment, metaData);
        if (!tableNameSegment.getIdentifier().getValue().equals(logicTable)) {
            for (IndexSegment each : sqlStatementContext.getIndexes()) {
                String logicIndexName = IndexMetaDataUtil.getLogicIndexName(each.getIndexName().getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                result = replace(result, each, logicIndexName);
            }
            for (ConstraintSegment each : sqlStatementContext.getConstraints()) {
                String logicConstraint = IndexMetaDataUtil.getLogicIndexName(each.getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                result = replace(result, each, logicConstraint);
            }
        }
        return result;
    }
    
    private String replace(final String sql, final SQLSegment sqlSegment, final String replaceName) {
        String result = "";
        int start = sqlSegment.getStartIndex();
        int stop = sqlSegment.getStopIndex();
        result += sql.substring(0, start);
        result += replaceName;
        result += sql.substring(stop + 1);
        return result;
    }
    
    private String findLogicTable(final TableNameSegment tableNameSegment, final ShardingSphereMetaData metaData) {
        String actualTable = tableNameSegment.getIdentifier().getValue();
        return metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule)
                .map(each -> ((DataNodeContainedRule) each).findLogicTableByActualTable(actualTable).orElse(null)).filter(Objects::nonNull).findFirst().orElse(actualTable);
    }
    
    private LogicSQL getLogicSQL(final String sql, final DatabaseType databaseType, final String databaseName) {
        Optional<SQLParserRule> sqlParserRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType.getType(), sqlParserRule.get().toParserConfiguration()).parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(contextManager.getMetaDataContexts().getMetaDataMap(),
                sqlStatement, databaseName);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
}
