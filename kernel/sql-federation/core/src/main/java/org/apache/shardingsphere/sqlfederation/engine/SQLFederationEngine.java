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
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessorFactory;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationCompilerEngine;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.optimizer.exception.SQLFederationSchemaNotFoundException;
import org.apache.shardingsphere.sqlfederation.optimizer.exception.SQLFederationUnsupportedSQLException;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.util.SQLFederationValidatorUtils;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.optimizer.statement.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * SQL federation engine.
 */
@Getter
public final class SQLFederationEngine implements AutoCloseable {
    
    private static final int DEFAULT_METADATA_VERSION = 0;
    
    private static final JavaTypeFactory DEFAULT_DATA_TYPE_FACTORY = new JavaTypeFactoryImpl();
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, SQLFederationDecider> deciders;
    
    private final String currentDatabaseName;
    
    private final String currentSchemaName;
    
    private final SQLFederationRule sqlFederationRule;
    
    private final SQLFederationProcessor processor;
    
    private QueryContext queryContext;
    
    private SchemaPlus schemaPlus;
    
    private ResultSet resultSet;
    
    public SQLFederationEngine(final String currentDatabaseName, final String currentSchemaName, final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics,
                               final JDBCExecutor jdbcExecutor) {
        deciders = OrderedSPILoader.getServices(SQLFederationDecider.class, metaData.getDatabase(currentDatabaseName).getRuleMetaData().getRules());
        this.currentDatabaseName = currentDatabaseName;
        this.currentSchemaName = currentSchemaName;
        sqlFederationRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class);
        processor = SQLFederationProcessorFactory.getInstance().newInstance(metaData, statistics, jdbcExecutor);
    }
    
    /**
     * Judge whether SQL federation enabled or not.
     *
     * @return whether SQL federation enabled or not
     */
    public boolean isSqlFederationEnabled() {
        return sqlFederationRule.getConfiguration().isSqlFederationEnabled();
    }
    
    /**
     * Decide use SQL federation or not.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @return use SQL federation or not
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean decide(final QueryContext queryContext, final RuleMetaData globalRuleMetaData) {
        if (isQuerySystemSchema(queryContext)) {
            return true;
        }
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        if (!sqlFederationRule.getConfiguration().isSqlFederationEnabled() || !(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        boolean allQueryUseSQLFederation = sqlFederationRule.getConfiguration().isAllQueryUseSQLFederation();
        if (allQueryUseSQLFederation) {
            return true;
        }
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        Collection<String> databaseNames = selectStatementContext.getTablesContext().getDatabaseNames();
        if (databaseNames.size() > 1) {
            return true;
        }
        ShardingSphereDatabase usedDatabase = queryContext.getUsedDatabase();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : deciders.entrySet()) {
            boolean isUseSQLFederation = entry.getValue().decide(selectStatementContext, queryContext.getParameters(), globalRuleMetaData, usedDatabase, entry.getKey(), includedDataNodes);
            if (isUseSQLFederation) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isQuerySystemSchema(final QueryContext queryContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        ShardingSphereDatabase database = queryContext.getUsedDatabase();
        return SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(), selectStatementContext.getTablesContext().getSchemaNames(), database)
                || SystemSchemaUtils.isDriverQuerySystemCatalog(sqlStatementContext.getDatabaseType(), selectStatementContext.getSqlStatement().getProjections().getProjections());
    }
    
    /**
     * Execute query.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param federationContext federation context
     * @return result set
     * @throws SQLFederationUnsupportedSQLException SQL federation unsupported SQL exception
     */
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final JDBCExecutorCallback<? extends ExecuteResult> callback,
                                  final SQLFederationContext federationContext) {
        queryContext = federationContext.getQueryContext();
        try {
            ShardingSpherePreconditions.checkState(queryContext.getSqlStatementContext() instanceof SelectStatementContext,
                    () -> new IllegalArgumentException("SQL statement must be select statement in sql federation engine."));
            SelectStatementContext selectStatementContext = (SelectStatementContext) queryContext.getSqlStatementContext();
            String databaseName = selectStatementContext.getTablesContext().getDatabaseNames().stream().findFirst().orElse(currentDatabaseName);
            String schemaName = selectStatementContext.getTablesContext().getSchemaName().orElse(currentSchemaName);
            SqlToRelConverter converter = creeateSQLToRelConverter(databaseName, schemaName, selectStatementContext.getDatabaseType(), processor.getConvention());
            SQLFederationExecutionPlan executionPlan = compileQuery(converter, databaseName, schemaName,
                    federationContext.getMetaData(), selectStatementContext, queryContext.getSql(), processor.getConvention());
            schemaPlus = getSqlFederationSchema(converter, schemaName, queryContext.getSql());
            processor.registerExecutor(prepareEngine, callback, databaseName, schemaName, federationContext, sqlFederationRule.getOptimizerContext(), schemaPlus);
            resultSet = processor.executePlan(prepareEngine, callback, executionPlan, converter, federationContext, schemaPlus);
            return resultSet;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new SQLFederationUnsupportedSQLException(queryContext.getSql(), ex);
        } finally {
            processEngine.completeSQLExecution(federationContext.getProcessId());
        }
    }
    
    private SqlToRelConverter creeateSQLToRelConverter(final String databaseName, final String schemaName, final DatabaseType databaseType, final Convention convention) {
        OptimizerContext optimizerContext = sqlFederationRule.getOptimizerContext();
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(optimizerContext.getParserContext(databaseName).getDialectProps());
        Schema schema = optimizerContext.getMetaData(databaseName).getSchema(schemaName);
        CalciteCatalogReader catalogReader = SQLFederationValidatorUtils.createCatalogReader(schemaName, schema, DEFAULT_DATA_TYPE_FACTORY, connectionConfig, databaseType);
        SqlValidator validator = SQLFederationValidatorUtils.createSqlValidator(catalogReader, DEFAULT_DATA_TYPE_FACTORY, databaseType, connectionConfig);
        RelOptCluster relOptCluster = SQLFederationValidatorUtils.createRelOptCluster(DEFAULT_DATA_TYPE_FACTORY, convention);
        return SQLFederationValidatorUtils.createSqlToRelConverter(catalogReader, validator, relOptCluster, optimizerContext.getSqlParserRule(), databaseType, true);
    }
    
    private SQLFederationExecutionPlan compileQuery(final SqlToRelConverter converter, final String databaseName, final String schemaName, final ShardingSphereMetaData metaData,
                                                    final SelectStatementContext selectStatementContext, final String sql, final Convention convention) {
        SQLStatementCompiler sqlStatementCompiler = new SQLStatementCompiler(converter, convention);
        SQLFederationCompilerEngine compilerEngine = new SQLFederationCompilerEngine(databaseName, schemaName, sqlFederationRule.getConfiguration().getExecutionPlanCache());
        // TODO open useCache flag when ShardingSphereTable contains version
        return compilerEngine.compile(buildCacheKey(metaData, databaseName, schemaName, selectStatementContext, sql, sqlStatementCompiler), false);
    }
    
    private SchemaPlus getSqlFederationSchema(final SqlToRelConverter converter, final String schemaName, final String sql) {
        SchemaPlus result = converter.validator.getCatalogReader().getRootSchema().plus().subSchemas().get(schemaName);
        ShardingSpherePreconditions.checkNotNull(result, () -> new SQLFederationSchemaNotFoundException(schemaName, sql));
        return result;
    }
    
    private ExecutionPlanCacheKey buildCacheKey(final ShardingSphereMetaData metaData, final String databaseName, final String schemaName, final SelectStatementContext selectStatementContext,
                                                final String sql, final SQLStatementCompiler sqlStatementCompiler) {
        ShardingSphereSchema schema = metaData.getDatabase(databaseName).getSchema(schemaName);
        ExecutionPlanCacheKey result = new ExecutionPlanCacheKey(sql, selectStatementContext.getSqlStatement(), selectStatementContext.getDatabaseType().getType(), sqlStatementCompiler);
        for (String each : selectStatementContext.getTablesContext().getTableNames()) {
            ShardingSphereTable table = schema.getTable(each);
            ShardingSpherePreconditions.checkNotNull(table, () -> new NoSuchTableException(each));
            // TODO replace DEFAULT_METADATA_VERSION with actual version in ShardingSphereTable
            result.getTableMetaDataVersions().put(new QualifiedTable(schema.getName(), table.getName()), DEFAULT_METADATA_VERSION);
        }
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        closeResultSet().ifPresent(result::add);
        unregisterExecutor();
        if (result.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        result.forEach(ex::setNextException);
        throw ex;
    }
    
    private Optional<SQLException> closeResultSet() {
        try {
            if (null != resultSet && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (final SQLException ex) {
            return Optional.of(ex);
        }
        return Optional.empty();
    }
    
    private void unregisterExecutor() {
        if (null != queryContext && null != schemaPlus) {
            processor.unregisterExecutor(queryContext, schemaPlus);
        }
    }
}
