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

package org.apache.shardingsphere.infra.federation.executor.advanced;

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
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.federation.executor.FederationContext;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.advanced.resultset.FederationResultSet;
import org.apache.shardingsphere.infra.federation.executor.original.schema.FilterableSchema;
import org.apache.shardingsphere.infra.federation.executor.original.table.FilterableTableScanExecutor;
import org.apache.shardingsphere.infra.federation.executor.original.table.FilterableTableScanExecutorContext;
import org.apache.shardingsphere.infra.federation.optimizer.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Advanced federation executor.
 */
public final class AdvancedFederationExecutor implements FederationExecutor {
    
    private final String databaseName;
    
    private final String schemaName;
    
    private final OptimizerContext optimizerContext;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final EventBusContext eventBusContext;
    
    private ResultSet resultSet;
    
    public AdvancedFederationExecutor(final String databaseName, final String schemaName, final OptimizerContext optimizerContext,
                                      final ShardingSphereRuleMetaData globalRuleMetaData, final ConfigurationProperties props, final JDBCExecutor jdbcExecutor,
                                      final EventBusContext eventBusContext) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.optimizerContext = optimizerContext;
        this.globalRuleMetaData = globalRuleMetaData;
        this.props = props;
        this.jdbcExecutor = jdbcExecutor;
        this.eventBusContext = eventBusContext;
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) throws SQLException {
        SQLStatementContext<?> sqlStatementContext = federationContext.getLogicSQL().getSqlStatementContext();
        Preconditions.checkArgument(sqlStatementContext instanceof SelectStatementContext, "SQL statement context must be select statement context.");
        ShardingSphereSchema schema = federationContext.getDatabases().get(databaseName.toLowerCase()).getSchema(schemaName);
        FilterableSchema filterableSchema = createFilterableSchema(prepareEngine, schema, callback, federationContext);
        Enumerator<Object[]> enumerator = execute(sqlStatementContext.getSqlStatement(), filterableSchema).enumerator();
        resultSet = new FederationResultSet(enumerator, schema, filterableSchema, sqlStatementContext);
        return resultSet;
    }
    
    private FilterableSchema createFilterableSchema(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final ShardingSphereSchema schema,
                                                    final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) {
        FilterableTableScanExecutorContext executorContext = new FilterableTableScanExecutorContext(databaseName, schemaName, props, federationContext);
        FilterableTableScanExecutor executor = new FilterableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, globalRuleMetaData, executorContext, eventBusContext);
        FederationSchemaMetaData schemaMetaData = new FederationSchemaMetaData(schemaName, schema.getTables());
        return new FilterableSchema(schemaMetaData, executor);
    }
    
    @SuppressWarnings("unchecked")
    private Enumerable<Object[]> execute(final SQLStatement sqlStatement, final FilterableSchema filterableSchema) {
        // TODO remove OptimizerPlannerContextFactory call and use setup executor to handle this logic
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(OptimizerPlannerContextFactory.createConnectionProperties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        CalciteCatalogReader catalogReader = OptimizerPlannerContextFactory.createCatalogReader(schemaName, filterableSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = OptimizerPlannerContextFactory.createValidator(catalogReader, relDataTypeFactory, connectionConfig);
        SqlToRelConverter converter = OptimizerPlannerContextFactory.createConverter(catalogReader, validator, relDataTypeFactory);
        RelNode bestPlan = new ShardingSphereOptimizer(optimizerContext, converter).optimize(databaseName, schemaName, sqlStatement);
        Bindable<Object[]> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) bestPlan, EnumerableRel.Prefer.ARRAY);
        return executablePlan.bind(new AdvancedExecuteDataContext(validator, converter));
    }
    
    @Override
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
