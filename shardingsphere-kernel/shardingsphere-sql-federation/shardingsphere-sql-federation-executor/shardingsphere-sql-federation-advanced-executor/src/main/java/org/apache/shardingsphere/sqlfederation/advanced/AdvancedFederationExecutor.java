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

package org.apache.shardingsphere.sqlfederation.advanced;

import com.google.common.base.Preconditions;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.runtime.Bindable;
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
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.advanced.resultset.FederationResultSet;
import org.apache.shardingsphere.sqlfederation.advanced.table.TranslatableTableScanExecutor;
import org.apache.shardingsphere.sqlfederation.common.CommonExecuteDataContext;
import org.apache.shardingsphere.sqlfederation.common.table.CommonTableScanExecutorContext;
import org.apache.shardingsphere.sqlfederation.optimizer.ShardingSphereOptimizer;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.QueryOptimizePlannerFactory;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced federation executor.
 */
public final class AdvancedFederationExecutor implements SQLFederationExecutor {
    
    private String databaseName;
    
    private String schemaName;
    
    private OptimizerContext optimizerContext;
    
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    private ConfigurationProperties props;
    
    private JDBCExecutor jdbcExecutor;
    
    private EventBusContext eventBusContext;
    
    private ResultSet resultSet;
    
    @Override
    public void init(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final JDBCExecutor jdbcExecutor, final EventBusContext eventBusContext) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.optimizerContext = OptimizerContextFactory.create(metaData.getDatabases(), metaData.getGlobalRuleMetaData());
        this.globalRuleMetaData = metaData.getGlobalRuleMetaData();
        this.props = metaData.getProps();
        this.jdbcExecutor = jdbcExecutor;
        this.eventBusContext = eventBusContext;
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        SQLStatementContext<?> sqlStatementContext = federationContext.getQueryContext().getSqlStatementContext();
        Preconditions.checkArgument(sqlStatementContext instanceof SelectStatementContext, "SQL statement context must be select statement context.");
        ShardingSphereSchema schema = federationContext.getDatabases().get(databaseName.toLowerCase()).getSchema(schemaName);
        TranslatableSchema translatableSchema = createTranslatableSchema(prepareEngine, schema, callback, federationContext);
        Map<String, Object> parameters = createParameters(federationContext.getQueryContext().getParameters());
        Enumerator<Object[]> enumerator = execute(sqlStatementContext.getSqlStatement(), translatableSchema, parameters).enumerator();
        resultSet = new FederationResultSet(enumerator, schema, translatableSchema, sqlStatementContext);
        return resultSet;
    }
    
    private Map<String, Object> createParameters(final List<Object> parameters) {
        Map<String, Object> result = new HashMap<>(parameters.size(), 1);
        int index = 0;
        for (Object each : parameters) {
            result.put("?" + index++, each);
        }
        return result;
    }
    
    private TranslatableSchema createTranslatableSchema(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final ShardingSphereSchema schema,
                                                        final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        CommonTableScanExecutorContext executorContext = new CommonTableScanExecutorContext(databaseName, schemaName, props, federationContext);
        TranslatableTableScanExecutor executor = new TranslatableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, globalRuleMetaData, executorContext, eventBusContext);
        return new TranslatableSchema(schemaName, schema, executor);
    }
    
    @SuppressWarnings("unchecked")
    private Enumerable<Object[]> execute(final SQLStatement sqlStatement, final TranslatableSchema translatableSchema, final Map<String, Object> parameters) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(OptimizerPlannerContextFactory.createConnectionProperties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        CalciteCatalogReader catalogReader = OptimizerPlannerContextFactory.createCatalogReader(schemaName, translatableSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = OptimizerPlannerContextFactory.createValidator(catalogReader, relDataTypeFactory, connectionConfig);
        SqlToRelConverter converter = OptimizerPlannerContextFactory.createConverter(catalogReader, validator, relDataTypeFactory);
        RelNode bestPlan =
                new ShardingSphereOptimizer(converter, QueryOptimizePlannerFactory.createHepPlanner()).optimize(sqlStatement);
        Bindable<Object[]> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) bestPlan, EnumerableRel.Prefer.ARRAY);
        return executablePlan.bind(new CommonExecuteDataContext(validator, converter, parameters));
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    @Override
    public void close() throws SQLException {
        if (null != resultSet) {
            resultSet.close();
        }
    }
    
    @Override
    public String getType() {
        return "ADVANCED";
    }
}
