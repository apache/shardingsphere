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

package org.apache.shardingsphere.sqlfederation.provider.calcite;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationCompilerEngine;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationUnsupportedSQLException;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.compiler.rel.converter.SQLFederationRelConverter;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.provider.calcite.engine.processor.SQLFederationProcessor;
import org.apache.shardingsphere.sqlfederation.provider.calcite.engine.processor.SQLFederationProcessorFactory;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Calcite SQL federation executor.
 */
@Getter
@Slf4j
public final class CalciteSQLFederationExecutor implements SQLFederationExecutor {
    
    private static final Collection<Class<?>> NEED_THROW_EXCEPTION_TYPES = Arrays.asList(SQLExecutionInterruptedException.class, SQLIntegrityConstraintViolationException.class);
    
    private static final int MAX_ERROR_MESSAGE_LENGTH = 5000;
    
    private final ProcessEngine processEngine;
    
    private final String currentDatabaseName;
    
    private final String currentSchemaName;
    
    private final CalciteSQLFederationProvider provider;
    
    private final SQLFederationProcessor processor;
    
    private QueryContext queryContext;
    
    private SchemaPlus schemaPlus;
    
    private ResultSet resultSet;
    
    public CalciteSQLFederationExecutor(final String currentDatabaseName, final String currentSchemaName, final ShardingSphereStatistics statistics,
                                        final JDBCExecutor jdbcExecutor, final ProcessEngine processEngine, final CalciteSQLFederationProvider provider) {
        this.currentDatabaseName = currentDatabaseName;
        this.currentSchemaName = currentSchemaName;
        this.processEngine = processEngine;
        this.provider = provider;
        processor = SQLFederationProcessorFactory.getInstance().newInstance(statistics, jdbcExecutor);
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
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationContext federationContext) {
        return execute0(prepareEngine, callback, federationContext);
    }
    
    private ResultSet execute0(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                               final JDBCExecutorCallback<? extends ExecuteResult> queryCallback, final SQLFederationContext federationContext) {
        queryContext = federationContext.getQueryContext();
        logSQL(queryContext, federationContext.getMetaData().getProps());
        try {
            processEngine.executeSQL(new ExecutionGroupContext<>(Collections.emptyList(),
                    new ExecutionGroupReportContext(federationContext.getProcessId(), currentDatabaseName, queryContext.getConnectionContext().getGrantee())), queryContext);
            SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext() instanceof ExplainStatementContext
                    ? ((ExplainStatementContext) queryContext.getSqlStatementContext()).getExplainableSQLStatementContext()
                    : queryContext.getSqlStatementContext();
            CompilerContext compilerContext = provider.getCompilerContext();
            SQLFederationRelConverter converter = new SQLFederationRelConverter(compilerContext, getSchemaPath(sqlStatementContext),
                    sqlStatementContext.getSqlStatement().getDatabaseType(), processor.getConvention());
            schemaPlus = converter.getSchemaPlus();
            processor.prepare(prepareEngine, queryCallback, currentDatabaseName, currentSchemaName, federationContext, compilerContext, schemaPlus);
            SQLFederationExecutionPlan executionPlan = compileQuery(converter, currentDatabaseName,
                    currentSchemaName, federationContext, sqlStatementContext, queryContext.getSql(), processor.getConvention());
            logExecutionPlan(executionPlan, federationContext.getMetaData().getProps());
            resultSet = processor.executePlan(prepareEngine, queryCallback, executionPlan, converter, federationContext, schemaPlus);
            return resultSet;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            String errorMessage = splitErrorMessage(ex);
            log.error("SQL Federation execute failed, sql {}, parameters {}, reason {}", queryContext.getSql(), queryContext.getParameters(), errorMessage);
            closeResources(federationContext);
            if (NEED_THROW_EXCEPTION_TYPES.stream().anyMatch(each -> each.isAssignableFrom(ex.getClass()))) {
                throw ex;
            }
            throw new SQLFederationUnsupportedSQLException(queryContext.getSql(), errorMessage);
        }
    }
    
    private String splitErrorMessage(final Exception ex) {
        return null == ex.getMessage() ? "" : ex.getMessage().substring(0, Math.min(ex.getMessage().length(), MAX_ERROR_MESSAGE_LENGTH));
    }
    
    private void closeResources(final SQLFederationContext federationContext) {
        try {
            processEngine.completeSQLExecution(federationContext.getProcessId());
            close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("Failed to close SQL federation engine resources: {}", ex.getMessage());
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
    
    private void logExecutionPlan(final SQLFederationExecutionPlan executionPlan, final ConfigurationProperties props) {
        if (props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            log.info("SQL Federation Execution Plan: {}", RelOptUtil.toString(executionPlan.getPhysicalPlan(), SqlExplainLevel.ALL_ATTRIBUTES).replaceAll(", id = \\d+", ""));
        }
    }
    
    private List<String> getSchemaPath(final SQLStatementContext sqlStatementContext) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        // TODO set default schema according to search path result
        if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
            return sqlStatementContext.getTablesContext().getSimpleTables().stream().anyMatch(each -> each.getOwner().isPresent())
                    ? Collections.singletonList(currentDatabaseName)
                    : Arrays.asList(currentDatabaseName, currentSchemaName);
        }
        return Collections.singletonList(currentDatabaseName);
    }
    
    private SQLFederationExecutionPlan compileQuery(final SQLFederationRelConverter converter, final String databaseName, final String schemaName, final SQLFederationContext federationContext,
                                                    final SQLStatementContext sqlStatementContext, final String sql, final Convention convention) {
        SQLStatementCompiler sqlStatementCompiler = new SQLStatementCompiler(converter, convention);
        SQLFederationCompilerEngine compilerEngine = new SQLFederationCompilerEngine(databaseName, schemaName, provider.getExecutionPlanCache());
        return compilerEngine.compile(buildCacheKey(federationContext, sqlStatementContext, sql, sqlStatementCompiler), false);
    }
    
    private ExecutionPlanCacheKey buildCacheKey(final SQLFederationContext federationContext, final SQLStatementContext sqlStatementContext,
                                                final String sql, final SQLStatementCompiler sqlStatementCompiler) {
        ExecutionPlanCacheKey result = new ExecutionPlanCacheKey(sql, sqlStatementContext.getSqlStatement(), sqlStatementCompiler);
        Collection<SimpleTableSegment> tableSegments = sqlStatementContext.getTablesContext().getSimpleTables();
        for (SimpleTableSegment each : tableSegments) {
            IdentifierValue originalDatabase = each.getTableName().getTableBoundInfo().map(TableSegmentBoundInfo::getOriginalDatabase).orElseGet(() -> new IdentifierValue(currentDatabaseName));
            IdentifierValue originalSchema = each.getTableName().getTableBoundInfo().map(TableSegmentBoundInfo::getOriginalSchema).orElseGet(() -> new IdentifierValue(currentSchemaName));
            IdentifierValue tableName = each.getTableName().getIdentifier();
            ShardingSphereTable table = federationContext.getMetaData().getDatabase(originalDatabase).getSchema(originalSchema).getTable(tableName);
            ShardingSpherePreconditions.checkNotNull(table, () -> new NoSuchTableException(tableName.getValue()));
            result.getTableMetaDataVersions().put(Joiner.on(".").join(Arrays.asList(originalDatabase.getValue(), originalSchema.getValue(), table.getName())), 0);
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
