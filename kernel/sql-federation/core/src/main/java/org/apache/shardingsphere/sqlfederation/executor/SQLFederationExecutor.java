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

package org.apache.shardingsphere.sqlfederation.executor;

import com.google.common.base.Preconditions;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sqlfederation.executor.resultset.SQLFederationResultSet;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationCompiler;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL federation executor.
 */
public final class SQLFederationExecutor implements AutoCloseable {
    
    private String databaseName;
    
    private String schemaName;
    
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    private ConfigurationProperties props;
    
    private ShardingSphereData data;
    
    private JDBCExecutor jdbcExecutor;
    
    private ResultSet resultSet;
    
    /**
     * Init SQL federation executor.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param metaData ShardingSphere meta data
     * @param data ShardingSphere data
     * @param jdbcExecutor jdbc executor
     */
    public void init(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final ShardingSphereData data, final JDBCExecutor jdbcExecutor) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.globalRuleMetaData = metaData.getGlobalRuleMetaData();
        this.props = metaData.getProps();
        this.data = data;
        this.jdbcExecutor = jdbcExecutor;
    }
    
    /**
     * Execute query.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param federationContext federation context
     * @return result set
     */
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        SQLStatementContext sqlStatementContext = federationContext.getQueryContext().getSqlStatementContext();
        Preconditions.checkArgument(sqlStatementContext instanceof SelectStatementContext, "SQL statement context must be select statement context.");
        ShardingSphereDatabase database = federationContext.getMetaData().getDatabase(databaseName);
        ShardingSphereSchema schema = database.getSchema(schemaName);
        OptimizerContext optimizerContext = globalRuleMetaData.getSingleRule(SQLFederationRule.class).getOptimizerContext();
        Schema sqlFederationSchema = optimizerContext.getPlannerContext(databaseName).getValidators().get(schemaName).getCatalogReader().getRootSchema().plus().getSubSchema(schemaName);
        registerTableScanExecutor(sqlFederationSchema, prepareEngine, callback, federationContext, optimizerContext);
        Map<String, Object> params = createParameters(federationContext.getQueryContext().getParameters());
        resultSet = execute((SelectStatementContext) sqlStatementContext, schema, sqlFederationSchema, params, optimizerContext);
        return resultSet;
    }
    
    private Map<String, Object> createParameters(final List<Object> params) {
        Map<String, Object> result = new HashMap<>(params.size(), 1F);
        int index = 0;
        for (Object each : params) {
            result.put("?" + index++, each);
        }
        return result;
    }
    
    private void registerTableScanExecutor(final Schema sqlFederationSchema, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                           final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext,
                                           final OptimizerContext optimizerContext) {
        TableScanExecutorContext executorContext = new TableScanExecutorContext(databaseName, schemaName, props, federationContext);
        TableScanExecutor executor = new TranslatableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, globalRuleMetaData, executorContext, data);
        for (String each : federationContext.getQueryContext().getSqlStatementContext().getTablesContext().getTableNames()) {
            Table table = sqlFederationSchema.getTable(each);
            if (table instanceof SQLFederationTable) {
                ((SQLFederationTable) table).setExecutor(executor);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private ResultSet execute(final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema, final Schema sqlFederationSchema, final Map<String, Object> params,
                              final OptimizerContext optimizerContext) {
        OptimizerPlannerContext plannerContext = optimizerContext.getPlannerContext(databaseName);
        SqlValidator sqlValidator = plannerContext.getValidators().get(schemaName);
        SqlToRelConverter sqlToRelConverter = plannerContext.getConverters().get(schemaName);
        SQLFederationExecutionPlan executionPlan = new SQLFederationCompiler(sqlToRelConverter, plannerContext.getHepPlanner()).compile(selectStatementContext.getSqlStatement());
        Bindable<Object> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) executionPlan.getPhysicalPlan(), EnumerableRel.Prefer.ARRAY);
        Enumerator<Object> enumerator = executablePlan.bind(new SQLFederationDataContext(sqlValidator, sqlToRelConverter, params)).enumerator();
        return new SQLFederationResultSet(enumerator, schema, sqlFederationSchema, selectStatementContext, executionPlan.getResultColumnType());
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    /**
     * Close.
     * 
     * @throws SQLException sql exception
     */
    public void close() throws SQLException {
        if (null != resultSet) {
            resultSet.close();
        }
    }
}
