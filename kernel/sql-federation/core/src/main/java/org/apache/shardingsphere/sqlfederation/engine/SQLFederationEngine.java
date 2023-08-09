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

package org.apache.shardingsphere.sqlfederation.engine;

import lombok.Getter;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationCompilerEngine;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.compiler.statement.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationDataContext;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.TableScanExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.EnumerableScanExecutor;
import org.apache.shardingsphere.sqlfederation.resultset.SQLFederationResultSet;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL federation engine.
 */
@Getter
public final class SQLFederationEngine implements AutoCloseable {
    
    private static final int DEFAULT_METADATA_VERSION = 0;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLFederationDecider> deciders;
    
    private final String databaseName;
    
    private final String schemaName;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereStatistics statistics;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final SQLFederationRule sqlFederationRule;
    
    private ResultSet resultSet;
    
    public SQLFederationEngine(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics, final JDBCExecutor jdbcExecutor) {
        deciders = OrderedSPILoader.getServices(SQLFederationDecider.class, metaData.getDatabase(databaseName).getRuleMetaData().getRules());
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.metaData = metaData;
        this.statistics = statistics;
        this.jdbcExecutor = jdbcExecutor;
        sqlFederationRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class);
    }
    
    /**
     * Decide use SQL federation or not.
     *
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     * @param database ShardingSphere database
     * @param globalRuleMetaData global rule meta data
     * @return use SQL federation or not
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean decide(final SQLStatementContext sqlStatementContext, final List<Object> parameters,
                          final ShardingSphereDatabase database, final ShardingSphereRuleMetaData globalRuleMetaData) {
        // TODO BEGIN: move this logic to SQLFederationDecider implement class when we remove sql federation type
        if (isQuerySystemSchema(sqlStatementContext, database)) {
            return true;
        }
        // TODO END
        boolean sqlFederationEnabled = sqlFederationRule.getConfiguration().isSqlFederationEnabled();
        if (!sqlFederationEnabled || !(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        Collection<DataNode> includedDataNodes = new HashSet<>();
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : deciders.entrySet()) {
            boolean isUseSQLFederation = entry.getValue().decide((SelectStatementContext) sqlStatementContext, parameters, globalRuleMetaData, database, entry.getKey(), includedDataNodes);
            if (isUseSQLFederation) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isQuerySystemSchema(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        return sqlStatementContext instanceof SelectStatementContext
                && (SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database)
                        || SystemSchemaUtils.isOpenGaussSystemCatalogQuery(sqlStatementContext.getDatabaseType(),
                                ((SelectStatementContext) sqlStatementContext).getSqlStatement().getProjections().getProjections()));
    }
    
    /**
     * Execute query.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param federationContext federation context
     * @return result set
     */
    @SuppressWarnings("unchecked")
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        String databaseName = federationContext.getQueryContext().getDatabaseNameFromSQLStatement().orElse(this.databaseName);
        String schemaName = federationContext.getQueryContext().getSchemaNameFromSQLStatement().orElse(this.schemaName);
        SQLFederationExecutionPlan executionPlan = compileQuery(prepareEngine, callback, federationContext, databaseName, schemaName);
        Bindable<Object> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) executionPlan.getPhysicalPlan(), EnumerableRel.Prefer.ARRAY);
        Map<String, Object> params = createParameters(federationContext.getQueryContext().getParameters());
        OptimizerPlannerContext plannerContext = sqlFederationRule.getOptimizerContext().getPlannerContext(databaseName);
        Enumerator<Object> enumerator = executablePlan.bind(new SQLFederationDataContext(plannerContext.getValidator(schemaName), plannerContext.getConverter(schemaName), params)).enumerator();
        ShardingSphereSchema schema = federationContext.getMetaData().getDatabase(databaseName).getSchema(schemaName);
        Schema sqlFederationSchema = plannerContext.getValidator(schemaName).getCatalogReader().getRootSchema().plus().getSubSchema(schemaName);
        resultSet = new SQLFederationResultSet(enumerator, schema, sqlFederationSchema, (SelectStatementContext) federationContext.getQueryContext().getSqlStatementContext(),
                executionPlan.getResultColumnType());
        return resultSet;
    }
    
    private SQLFederationExecutionPlan compileQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                                    final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext, final String databaseName,
                                                    final String schemaName) {
        SQLStatementContext sqlStatementContext = federationContext.getQueryContext().getSqlStatementContext();
        ShardingSpherePreconditions.checkState(sqlStatementContext instanceof SelectStatementContext, () -> new IllegalArgumentException("SQL statement context must be select statement context."));
        OptimizerPlannerContext plannerContext = sqlFederationRule.getOptimizerContext().getPlannerContext(databaseName);
        Schema sqlFederationSchema = plannerContext.getValidator(schemaName).getCatalogReader().getRootSchema().plus().getSubSchema(schemaName);
        registerTableScanExecutor(sqlFederationSchema, prepareEngine, callback, federationContext, sqlFederationRule.getOptimizerContext(), databaseName, schemaName);
        SQLStatementCompiler sqlStatementCompiler = new SQLStatementCompiler(plannerContext.getConverter(schemaName));
        SQLFederationCompilerEngine compilerEngine = new SQLFederationCompilerEngine(databaseName, schemaName, sqlFederationRule.getConfiguration().getExecutionPlanCache());
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        // TODO open useCache flag when ShardingSphereTable contains version
        return compilerEngine.compile(buildCacheKey(federationContext, selectStatementContext, sqlStatementCompiler, databaseName, schemaName), false);
    }
    
    private ExecutionPlanCacheKey buildCacheKey(final SQLFederationExecutorContext federationContext, final SelectStatementContext selectStatementContext,
                                                final SQLStatementCompiler sqlStatementCompiler, final String databaseName, final String schemaName) {
        ShardingSphereSchema schema = federationContext.getMetaData().getDatabase(databaseName).getSchema(schemaName);
        ExecutionPlanCacheKey result =
                new ExecutionPlanCacheKey(federationContext.getQueryContext().getSql(), selectStatementContext.getSqlStatement(), selectStatementContext.getDatabaseType().getType(),
                        sqlStatementCompiler);
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            ShardingSphereTable table = schema.getTable(each);
            ShardingSpherePreconditions.checkState(null != table, () -> new NoSuchTableException(each));
            // TODO replace DEFAULT_METADATA_VERSION with actual version in ShardingSphereTable
            result.getTableMetaDataVersions().put(table.getName(), DEFAULT_METADATA_VERSION);
        }
        return result;
    }
    
    private void registerTableScanExecutor(final Schema sqlFederationSchema, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                           final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext,
                                           final OptimizerContext optimizerContext, final String databaseName, final String schemaName) {
        if (null == sqlFederationSchema) {
            return;
        }
        TableScanExecutorContext executorContext = new TableScanExecutorContext(databaseName, schemaName, metaData.getProps(), federationContext);
        EnumerableScanExecutor scanExecutor = new EnumerableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, metaData.getGlobalRuleMetaData(), executorContext, statistics);
        // TODO register only the required tables
        for (String each : metaData.getDatabase(databaseName).getSchema(schemaName).getAllTableNames()) {
            Table table = sqlFederationSchema.getTable(each);
            if (table instanceof SQLFederationTable) {
                ((SQLFederationTable) table).setScanExecutor(scanExecutor);
            }
        }
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
    public void close() throws SQLException {
        if (null != resultSet) {
            resultSet.close();
        }
    }
}
