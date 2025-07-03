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

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationCompilerEngine;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationUnsupportedSQLException;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.engine.processor.SQLFederationProcessorFactory;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * SQL federation engine.
 */
@Slf4j
@Getter
public final class SQLFederationEngine implements AutoCloseable {
    
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
        processor = SQLFederationProcessorFactory.getInstance().newInstance(statistics, jdbcExecutor);
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
        if (!sqlFederationRule.getConfiguration().isSqlFederationEnabled() || !isSupportedSQLStatementContext(sqlStatementContext)) {
            return false;
        }
        boolean allQueryUseSQLFederation = sqlFederationRule.getConfiguration().isAllQueryUseSQLFederation();
        if (allQueryUseSQLFederation) {
            return true;
        }
        Collection<String> databaseNames = sqlStatementContext.getTablesContext().getDatabaseNames();
        if (databaseNames.size() > 1) {
            return true;
        }
        ShardingSphereDatabase usedDatabase = queryContext.getUsedDatabase();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : deciders.entrySet()) {
            boolean isUseSQLFederation = entry.getValue().decide(sqlStatementContext, queryContext.getParameters(), globalRuleMetaData, usedDatabase, entry.getKey(), includedDataNodes);
            if (isUseSQLFederation) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSupportedSQLStatementContext(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof ExplainStatementContext) {
            return ((ExplainStatementContext) sqlStatementContext).getSqlStatement().getExplainableSQLStatement() instanceof SelectStatement;
        }
        return sqlStatementContext instanceof SelectStatementContext;
    }
    
    // TODO remove this logic when statistic pass through to db finish
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
        logSQL(queryContext, federationContext.getMetaData().getProps());
        try {
            SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
            CompilerContext compilerContext = sqlFederationRule.getCompilerContext();
            SQLFederationRelConverter converter = new SQLFederationRelConverter(compilerContext,
                    getSchemaPath(sqlStatementContext), sqlStatementContext.getDatabaseType(), processor.getConvention());
            schemaPlus = converter.getSchemaPlus();
            processor.prepare(prepareEngine, callback, currentDatabaseName, currentSchemaName, federationContext, compilerContext, schemaPlus);
            SQLFederationExecutionPlan executionPlan = compileQuery(converter, currentDatabaseName,
                    currentSchemaName, federationContext.getMetaData(), sqlStatementContext, queryContext.getSql(), processor.getConvention());
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
    
    private void logSQL(final QueryContext queryContext, final ConfigurationProperties props) {
        if (!props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            return;
        }
        if (queryContext.getParameters().isEmpty()) {
            log.info("SQL Federation Logic SQL: {} ::: {} ::: {}", queryContext.getSql(), currentDatabaseName, currentSchemaName);
        } else {
            log.info("SQL Federation Logic SQL: {} ::: {} ::: {} ::: {}", queryContext.getSql(), queryContext.getParameters(), currentDatabaseName, currentSchemaName);
        }
    }
    
    private List<String> getSchemaPath(final SQLStatementContext sqlStatementContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDialectDatabaseMetaData();
        // TODO set default schema according to search path result
        if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
            return sqlStatementContext.getTablesContext().getSimpleTables().stream().anyMatch(each -> each.getOwner().isPresent())
                    ? Collections.singletonList(currentDatabaseName)
                    : Arrays.asList(currentDatabaseName, currentSchemaName);
        }
        return Collections.singletonList(currentDatabaseName);
    }
    
    private SQLFederationExecutionPlan compileQuery(final SQLFederationRelConverter converter, final String databaseName, final String schemaName, final ShardingSphereMetaData metaData,
                                                    final SQLStatementContext sqlStatementContext, final String sql, final Convention convention) {
        SQLStatementCompiler sqlStatementCompiler = new SQLStatementCompiler(converter, convention);
        SQLFederationCompilerEngine compilerEngine = new SQLFederationCompilerEngine(databaseName, schemaName, sqlFederationRule.getConfiguration().getExecutionPlanCache());
        return compilerEngine.compile(buildCacheKey(metaData, sqlStatementContext, sql, sqlStatementCompiler), false);
    }
    
    private ExecutionPlanCacheKey buildCacheKey(final ShardingSphereMetaData metaData, final SQLStatementContext sqlStatementContext,
                                                final String sql, final SQLStatementCompiler sqlStatementCompiler) {
        ExecutionPlanCacheKey result = new ExecutionPlanCacheKey(sql, sqlStatementContext.getSqlStatement(), sqlStatementContext.getDatabaseType().getType(), sqlStatementCompiler);
        Collection<SimpleTableSegment> tableSegments = sqlStatementContext.getTablesContext().getSimpleTables();
        for (SimpleTableSegment each : tableSegments) {
            String originalDatabase = each.getTableName().getTableBoundInfo().map(optional -> optional.getOriginalDatabase().getValue()).orElse(currentDatabaseName);
            String originalSchema = each.getTableName().getTableBoundInfo().map(optional -> optional.getOriginalSchema().getValue()).orElse(currentSchemaName);
            ShardingSphereTable table = metaData.getDatabase(originalDatabase).getSchema(originalSchema).getTable(each.getTableName().getIdentifier().getValue());
            ShardingSpherePreconditions.checkNotNull(table, () -> new NoSuchTableException(each.getTableName().getIdentifier().getValue()));
            result.getTableMetaDataVersions().put(Joiner.on(".").join(Arrays.asList(originalDatabase, originalSchema, table.getName())), 0);
        }
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        closeResultSet().ifPresent(result::add);
        release();
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
    
    private void release() {
        if (null != queryContext && null != schemaPlus) {
            processor.release(currentDatabaseName, currentSchemaName, queryContext, schemaPlus);
        }
    }
}
