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

package org.apache.shardingsphere.sqlfederation.engine.processor.impl;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRel.Prefer;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationBindContext;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.EnumerableScanExecutor;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationSchemaNotFoundException;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.resultset.SQLFederationResultSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard sql federation processor.
 */
@RequiredArgsConstructor
public final class StandardSQLFederationProcessor implements SQLFederationProcessor {
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereStatistics statistics;
    
    private final JDBCExecutor jdbcExecutor;
    
    @Override
    public void prepare(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final JDBCExecutorCallback<? extends ExecuteResult> callback,
                        final String currentDatabaseName, final String currentSchemaName, final SQLFederationContext federationContext, final CompilerContext compilerContext,
                        final SchemaPlus schemaPlus) {
        if (null == schemaPlus) {
            return;
        }
        SQLFederationExecutorContext executorContext = new SQLFederationExecutorContext(currentDatabaseName, currentSchemaName, metaData.getProps());
        EnumerableScanExecutor scanExecutor =
                new EnumerableScanExecutor(prepareEngine, jdbcExecutor, callback, compilerContext, executorContext, federationContext, metaData.getGlobalRuleMetaData(), statistics);
        SQLStatementContext sqlStatementContext = federationContext.getQueryContext().getSqlStatementContext();
        Collection<SimpleTableSegment> simpleTables = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables()
                : Collections.emptyList();
        for (SimpleTableSegment each : simpleTables) {
            Table table = getTable(currentDatabaseName, currentSchemaName, schemaPlus, each, sqlStatementContext.getDatabaseType(), federationContext.getQueryContext().getSql());
            if (table instanceof SQLFederationTable) {
                ((SQLFederationTable) table).setScanExecutor(scanExecutor);
            }
        }
    }
    
    private Table getTable(final String currentDatabaseName, final String currentSchemaName, final SchemaPlus schemaPlus, final SimpleTableSegment tableSegment, final DatabaseType databaseType,
                           final String sql) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        String originalDatabase = tableSegment.getTableName().getTableBoundInfo().map(optional -> optional.getOriginalDatabase().getValue()).orElse(currentDatabaseName);
        String originalScheme = tableSegment.getTableName().getTableBoundInfo().map(optional -> optional.getOriginalSchema().getValue()).orElse(currentSchemaName);
        SchemaPlus currentSchemaPlus = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? getNestedSchemaPlus(schemaPlus, originalDatabase, originalScheme, sql)
                : getSchemaPlus(schemaPlus, originalDatabase, sql);
        return currentSchemaPlus.tables().get(tableSegment.getTableName().getIdentifier().getValue());
    }
    
    private SchemaPlus getNestedSchemaPlus(final SchemaPlus schemaPlus, final String databaseName, final String schemaName, final String sql) {
        SchemaPlus databaseSchema = getSchemaPlus(schemaPlus, databaseName, sql);
        return getSchemaPlus(databaseSchema, schemaName, sql);
    }
    
    private SchemaPlus getSchemaPlus(final SchemaPlus schemaPlus, final String schemaName, final String sql) {
        SchemaPlus result = schemaPlus.subSchemas().get(schemaName);
        ShardingSpherePreconditions.checkNotNull(result, () -> new SQLFederationSchemaNotFoundException(schemaName, sql));
        return result;
    }
    
    @Override
    public void release(final String currentDatabaseName, final String currentSchemaName, final QueryContext queryContext, final SchemaPlus schemaPlus) {
        Collection<SimpleTableSegment> simpleTables = queryContext.getSqlStatementContext() instanceof TableAvailable
                ? ((TableAvailable) queryContext.getSqlStatementContext()).getTablesContext().getSimpleTables()
                : Collections.emptyList();
        for (SimpleTableSegment each : simpleTables) {
            Table table = getTable(currentDatabaseName, currentSchemaName, schemaPlus, each, queryContext.getSqlStatementContext().getDatabaseType(), queryContext.getSql());
            if (table instanceof SQLFederationTable) {
                ((SQLFederationTable) table).clearScanExecutor();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ResultSet executePlan(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final JDBCExecutorCallback<? extends ExecuteResult> callback,
                                 final SQLFederationExecutionPlan executionPlan, final SqlToRelConverter converter, final SQLFederationContext federationContext,
                                 final SchemaPlus schemaPlus) {
        Bindable<Object> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) executionPlan.getPhysicalPlan(), Prefer.ARRAY);
        Map<String, Object> params = createParameters(federationContext.getQueryContext().getParameters());
        Enumerator<Object> enumerator = executablePlan.bind(new SQLFederationBindContext(converter, params)).enumerator();
        SelectStatementContext selectStatementContext = (SelectStatementContext) federationContext.getQueryContext().getSqlStatementContext();
        return new SQLFederationResultSet(enumerator, schemaPlus, selectStatementContext.getProjectionsContext().getExpandProjections(), selectStatementContext.getDatabaseType(),
                executionPlan.getResultColumnType());
    }
    
    private Map<String, Object> createParameters(final List<Object> params) {
        Map<String, Object> result = new HashMap<>(params.size(), 1F);
        int index = 0;
        for (Object each : params) {
            result.put("?" + index++, each);
        }
        return result;
    }
    
    @Override
    public Convention getConvention() {
        return EnumerableConvention.INSTANCE;
    }
}
