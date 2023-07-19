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

package org.apache.shardingsphere.data.pipeline.common.metadata.generator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CommentStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.type.ConstraintAvailable;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Pipeline DDL generator.
 */
@Slf4j
public final class PipelineDDLGenerator {
    
    private static final String DELIMITER = ";";
    
    private static final String SET_SEARCH_PATH_PREFIX = "set search_path";
    
    /**
     * Generate logic DDL.
     * 
     * @param databaseType database type
     * @param sourceDataSource source data source
     * @param schemaName schema name
     * @param sourceTableName source table name
     * @param targetTableName target table name
     * @param parserEngine parser engine
     * @return DDL SQL
     * @throws SQLException SQL exception 
     */
    public String generateLogicDDL(final DatabaseType databaseType, final DataSource sourceDataSource,
                                   final String schemaName, final String sourceTableName, final String targetTableName, final SQLParserEngine parserEngine) throws SQLException {
        long startTimeMillis = System.currentTimeMillis();
        StringBuilder result = new StringBuilder();
        for (String each : DatabaseTypedSPILoader.getService(CreateTableSQLGenerator.class, databaseType).generate(sourceDataSource, schemaName, sourceTableName)) {
            Optional<String> queryContext = decorate(databaseType, sourceDataSource, schemaName, targetTableName, parserEngine, each);
            queryContext.ifPresent(optional -> result.append(optional).append(DELIMITER).append(System.lineSeparator()));
        }
        log.info("generateLogicDDL, databaseType={}, schemaName={}, sourceTableName={}, targetTableName={}, cost {} ms",
                databaseType.getType(), schemaName, sourceTableName, targetTableName, System.currentTimeMillis() - startTimeMillis);
        return result.toString();
    }
    
    private Optional<String> decorate(final DatabaseType databaseType, final DataSource dataSource, final String schemaName, final String targetTableName,
                                      final SQLParserEngine parserEngine, final String sql) throws SQLException {
        if (StringUtils.isBlank(sql)) {
            return Optional.empty();
        }
        String databaseName;
        try (Connection connection = dataSource.getConnection()) {
            databaseName = connection.getCatalog();
        }
        String result = decorateActualSQL(databaseName, targetTableName, parserEngine, sql.trim());
        // TODO remove it after set search_path is supported.
        if ("openGauss".equals(databaseType.getType())) {
            return decorateOpenGauss(databaseName, schemaName, result, parserEngine);
        }
        return Optional.of(result);
    }
    
    private String decorateActualSQL(final String databaseName, final String targetTableName, final SQLParserEngine parserEngine, final String sql) {
        QueryContext queryContext = getQueryContext(databaseName, parserEngine, sql);
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
        if (sqlStatementContext instanceof CreateTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, targetTableName, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CommentStatementContext) {
            appendFromTable(replaceMap, targetTableName, (TableAvailable) sqlStatementContext);
        }
        if (sqlStatementContext instanceof CreateIndexStatementContext) {
            appendFromTable(replaceMap, targetTableName, (TableAvailable) sqlStatementContext);
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
        }
        if (sqlStatementContext instanceof AlterTableStatementContext) {
            appendFromIndexAndConstraint(replaceMap, targetTableName, sqlStatementContext);
            appendFromTable(replaceMap, targetTableName, (TableAvailable) sqlStatementContext);
        }
        return doDecorateActualTable(replaceMap, sql);
    }
    
    private QueryContext getQueryContext(final String databaseName, final SQLParserEngine parserEngine, final String sql) {
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(null, parserEngine.parse(sql, false), databaseName);
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private void appendFromIndexAndConstraint(final Map<SQLSegment, String> replaceMap, final String targetTableName, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable) || ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTableSegments().isEmpty()) {
            return;
        }
        TableNameSegment tableNameSegment = ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTableSegments().iterator().next().getTableName();
        if (!tableNameSegment.getIdentifier().getValue().equals(targetTableName)) {
            if (sqlStatementContext instanceof IndexAvailable) {
                for (IndexSegment each : ((IndexAvailable) sqlStatementContext).getIndexes()) {
                    String logicIndexName = IndexMetaDataUtils.getLogicIndexName(each.getIndexName().getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each.getIndexName(), logicIndexName);
                }
            }
            if (sqlStatementContext instanceof ConstraintAvailable) {
                for (ConstraintSegment each : ((ConstraintAvailable) sqlStatementContext).getConstraints()) {
                    String logicConstraint = IndexMetaDataUtils.getLogicIndexName(each.getIdentifier().getValue(), tableNameSegment.getIdentifier().getValue());
                    replaceMap.put(each, logicConstraint);
                }
            }
        }
    }
    
    private void appendFromTable(final Map<SQLSegment, String> replaceMap, final String targetTableName, final TableAvailable sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getAllTables()) {
            if (!targetTableName.equals(each.getTableName().getIdentifier().getValue())) {
                replaceMap.put(each.getTableName(), targetTableName);
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
    
    // TODO remove it after set search_path is supported.
    private Optional<String> decorateOpenGauss(final String databaseName, final String schemaName, final String queryContext,
                                               final SQLParserEngine parserEngine) {
        if (queryContext.toLowerCase().startsWith(SET_SEARCH_PATH_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(replaceTableNameWithPrefix(queryContext, schemaName + ".", databaseName, parserEngine));
    }
    
    private String replaceTableNameWithPrefix(final String sql, final String prefix, final String databaseName, final SQLParserEngine parserEngine) {
        QueryContext queryContext = getQueryContext(databaseName, parserEngine, sql);
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        if (sqlStatementContext instanceof CreateTableStatementContext || sqlStatementContext instanceof CommentStatementContext
                || sqlStatementContext instanceof CreateIndexStatementContext || sqlStatementContext instanceof AlterTableStatementContext) {
            if (sqlStatementContext.getTablesContext().getSimpleTableSegments().isEmpty()) {
                return sql;
            }
            if (sqlStatementContext.getTablesContext().getSchemaName().isPresent()) {
                return sql;
            }
            Map<SQLSegment, String> replaceMap = new TreeMap<>(Comparator.comparing(SQLSegment::getStartIndex));
            TableNameSegment tableNameSegment = sqlStatementContext.getTablesContext().getSimpleTableSegments().iterator().next().getTableName();
            replaceMap.put(tableNameSegment, prefix + tableNameSegment.getIdentifier().getValue());
            return doDecorateActualTable(replaceMap, sql);
        }
        return sql;
    }
}
