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
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.sqlfederation.SQLFederationDataContext;
import org.apache.shardingsphere.sqlfederation.advanced.resultset.SQLFederationResultSet;
import org.apache.shardingsphere.sqlfederation.executor.FilterableTableScanExecutor;
import org.apache.shardingsphere.sqlfederation.executor.TableScanExecutorContext;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLOptimizeContext;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLOptimizeEngine;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter.FilterableSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationPlannerUtil;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced sql federation executor.
 */
public final class AdvancedSQLFederationExecutor implements SQLFederationExecutor {
    
    private static final JavaTypeFactory JAVA_TYPE_FACTORY = new JavaTypeFactoryImpl();
    
    private String databaseName;
    
    private String schemaName;
    
    private OptimizerContext optimizerContext;
    
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    private ConfigurationProperties props;
    
    private ShardingSphereData data;
    
    private JDBCExecutor jdbcExecutor;
    
    private EventBusContext eventBusContext;
    
    private ResultSet resultSet;
    
    @Override
    public void init(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final ShardingSphereData data,
                     final JDBCExecutor jdbcExecutor, final EventBusContext eventBusContext) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.optimizerContext = OptimizerContextFactory.create(metaData.getDatabases(), metaData.getGlobalRuleMetaData());
        this.globalRuleMetaData = metaData.getGlobalRuleMetaData();
        this.props = metaData.getProps();
        this.data = data;
        this.jdbcExecutor = jdbcExecutor;
        this.eventBusContext = eventBusContext;
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        SQLStatementContext<?> sqlStatementContext = federationContext.getQueryContext().getSqlStatementContext();
        Preconditions.checkArgument(sqlStatementContext instanceof SelectStatementContext, "SQL statement context must be select statement context.");
        ShardingSphereDatabase database = federationContext.getMetaData().getDatabase(databaseName);
        ShardingSphereSchema schema = database.getSchema(schemaName);
        AbstractSchema sqlFederationSchema = createSQLFederationSchema(prepareEngine, database.getProtocolType(), schema, callback, federationContext);
        Map<String, Object> params = createParameters(federationContext.getQueryContext().getParameters());
        resultSet = execute((SelectStatementContext) sqlStatementContext, schema, sqlFederationSchema, params);
        return resultSet;
    }
    
    private Map<String, Object> createParameters(final List<Object> params) {
        Map<String, Object> result = new HashMap<>(params.size(), 1);
        int index = 0;
        for (Object each : params) {
            result.put("?" + index++, each);
        }
        return result;
    }
    
    private AbstractSchema createSQLFederationSchema(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final DatabaseType protocolType,
                                                     final ShardingSphereSchema schema,
                                                     final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationExecutorContext federationContext) {
        TableScanExecutorContext executorContext = new TableScanExecutorContext(databaseName, schemaName, props, federationContext);
        // TODO replace FilterableTableScanExecutor with TranslatableTableScanExecutor
        TableScanExecutor executor = new FilterableTableScanExecutor(prepareEngine, jdbcExecutor, callback, optimizerContext, globalRuleMetaData, executorContext, data, eventBusContext);
        // TODO replace FilterableSchema with TranslatableSchema
        return new FilterableSchema(schemaName, schema, protocolType, JAVA_TYPE_FACTORY, executor);
    }
    
    @SuppressWarnings("unchecked")
    private ResultSet execute(final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema, final AbstractSchema sqlFederationSchema, final Map<String, Object> params) {
        OptimizerParserContext parserContext = optimizerContext.getParserContext(databaseName);
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(parserContext.getDialectProps());
        CalciteCatalogReader catalogReader = SQLFederationPlannerUtil.createCatalogReader(schemaName, sqlFederationSchema, JAVA_TYPE_FACTORY, connectionConfig);
        SqlValidator validator = SQLFederationPlannerUtil.createSqlValidator(catalogReader, JAVA_TYPE_FACTORY, parserContext.getDatabaseType(), connectionConfig);
        SqlToRelConverter converter = SQLFederationPlannerUtil.createSqlToRelConverter(catalogReader, validator,
                SQLFederationPlannerUtil.createRelOptCluster(JAVA_TYPE_FACTORY), optimizerContext.getSqlParserRule(), parserContext.getDatabaseType(), true);
        RelOptPlanner hepPlanner = optimizerContext.getPlannerContext(databaseName).getHepPlanner();
        SQLOptimizeContext optimizeContext = new SQLOptimizeEngine(converter, hepPlanner).optimize(selectStatementContext.getSqlStatement());
        Bindable<Object> executablePlan = EnumerableInterpretable.toBindable(Collections.emptyMap(), null, (EnumerableRel) optimizeContext.getBestPlan(), EnumerableRel.Prefer.ARRAY);
        Enumerator<Object> enumerator = executablePlan.bind(new SQLFederationDataContext(validator, converter, params)).enumerator();
        return new SQLFederationResultSet(enumerator, schema, sqlFederationSchema, selectStatementContext, optimizeContext.getValidatedNodeType());
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
    public boolean isDefault() {
        return true;
    }
    
    @Override
    public String getType() {
        return "ADVANCED";
    }
}
