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

package org.apache.shardingsphere.sqlfederation.executor.enumerable;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.TableScanExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.row.EmptyRowEnumerator;
import org.apache.shardingsphere.sqlfederation.executor.row.MemoryEnumerator;
import org.apache.shardingsphere.sqlfederation.executor.row.SQLFederationRowEnumerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enumerable scan executor.
 */
@RequiredArgsConstructor
public final class EnumerableScanExecutor {
    
    private static final Collection<String> SYSTEM_CATALOG_TABLES = new HashSet<>(3, 1F);
    
    private static final String DAT_COMPATIBILITY = "PG";
    
    private static final String PG_DATABASE = "pg_database";
    
    private static final String PG_TABLES = "pg_tables";
    
    private static final String PG_ROLES = "pg_roles";
    
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> callback;
    
    private final OptimizerContext optimizerContext;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final TableScanExecutorContext executorContext;
    
    private final ShardingSphereStatistics statistics;
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    static {
        SYSTEM_CATALOG_TABLES.add(PG_DATABASE);
        SYSTEM_CATALOG_TABLES.add(PG_TABLES);
        SYSTEM_CATALOG_TABLES.add(PG_ROLES);
    }
    
    /**
     * Execute.
     *
     * @param table table meta data
     * @param scanContext push down table scan context
     * @return query results
     */
    public Enumerable<Object> execute(final ShardingSphereTable table, final EnumerableScanExecutorContext scanContext) {
        String databaseName = executorContext.getDatabaseName().toLowerCase();
        String schemaName = executorContext.getSchemaName().toLowerCase();
        DatabaseType databaseType = DatabaseTypeEngine.getTrunkDatabaseType(optimizerContext.getParserContext(databaseName).getDatabaseType().getType());
        if (databaseType.getSystemSchemas().contains(schemaName)) {
            return executeByShardingSphereData(databaseName, schemaName, table, databaseType);
        }
        SQLFederationExecutorContext federationContext = executorContext.getFederationContext();
        QueryContext queryContext = createQueryContext(federationContext.getMetaData(), scanContext, databaseType, federationContext.getQueryContext().isUseCache());
        ShardingSphereDatabase database = federationContext.getMetaData().getDatabase(databaseName);
        ExecutionContext context = new KernelProcessor().generateExecutionContext(queryContext, database, globalRuleMetaData, executorContext.getProps(), new ConnectionContext());
        if (federationContext.isPreview()) {
            federationContext.getExecutionUnits().addAll(context.getExecutionUnits());
            return createEmptyEnumerable();
        }
        return execute(queryContext, database, context);
    }
    
    private AbstractEnumerable<Object> execute(final QueryContext queryContext, final ShardingSphereDatabase database, final ExecutionContext context) {
        try {
            computeConnectionOffsets(context);
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext =
                    prepareEngine.prepare(context.getRouteContext(), executorContext.getConnectionOffsets(), context.getExecutionUnits(), new ExecutionGroupReportContext(database.getName()));
            setParameters(executionGroupContext.getInputGroups());
            processEngine.executeSQL(executionGroupContext, context.getQueryContext());
            List<QueryResult> queryResults = jdbcExecutor.execute(executionGroupContext, callback).stream().map(QueryResult.class::cast).collect(Collectors.toList());
            MergeEngine mergeEngine = new MergeEngine(database, executorContext.getProps(), new ConnectionContext());
            MergedResult mergedResult = mergeEngine.merge(queryResults, queryContext.getSqlStatementContext());
            Collection<Statement> statements = getStatements(executionGroupContext.getInputGroups());
            return createEnumerable(mergedResult, queryResults.get(0).getMetaData(), statements);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        } finally {
            processEngine.completeSQLExecution();
        }
    }
    
    private void computeConnectionOffsets(final ExecutionContext context) {
        for (ExecutionUnit each : context.getExecutionUnits()) {
            if (executorContext.getConnectionOffsets().containsKey(each.getDataSourceName())) {
                int connectionOffset = executorContext.getConnectionOffsets().get(each.getDataSourceName());
                executorContext.getConnectionOffsets().put(each.getDataSourceName(), ++connectionOffset);
            } else {
                executorContext.getConnectionOffsets().put(each.getDataSourceName(), 0);
            }
        }
    }
    
    private Enumerable<Object> executeByShardingSphereData(final String databaseName, final String schemaName, final ShardingSphereTable table, final DatabaseType databaseType) {
        // TODO move this logic to ShardingSphere statistics
        if (databaseType instanceof OpenGaussDatabaseType && SYSTEM_CATALOG_TABLES.contains(table.getName().toLowerCase())) {
            return createMemoryEnumerator(createSystemCatalogTableData(table));
        }
        Optional<ShardingSphereTableData> tableData = Optional.ofNullable(statistics.getDatabaseData().get(databaseName)).map(optional -> optional.getSchemaData().get(schemaName))
                .map(ShardingSphereSchemaData::getTableData).map(shardingSphereData -> shardingSphereData.get(table.getName()));
        return tableData.map(this::createMemoryEnumerator).orElseGet(this::createEmptyEnumerable);
    }
    
    private ShardingSphereTableData createSystemCatalogTableData(final ShardingSphereTable table) {
        ShardingSphereTableData result = new ShardingSphereTableData(table.getName());
        ShardingSphereMetaData metaData = executorContext.getFederationContext().getMetaData();
        if (PG_DATABASE.equalsIgnoreCase(table.getName())) {
            appendOpenGaussDatabaseData(result, metaData.getDatabases().values());
        } else if (PG_TABLES.equalsIgnoreCase(table.getName())) {
            for (ShardingSphereDatabase each : metaData.getDatabases().values()) {
                appendOpenGaussTableData(result, each.getSchemas());
            }
        } else if (PG_ROLES.equalsIgnoreCase(table.getName())) {
            appendOpenGaussRoleData(result, metaData);
        }
        return result;
    }
    
    private void appendOpenGaussDatabaseData(final ShardingSphereTableData tableData, final Collection<ShardingSphereDatabase> databases) {
        for (ShardingSphereDatabase each : databases) {
            Object[] rows = new Object[15];
            rows[0] = each.getName();
            rows[11] = DAT_COMPATIBILITY;
            tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
        }
    }
    
    private void appendOpenGaussTableData(final ShardingSphereTableData tableData, final Map<String, ShardingSphereSchema> schemas) {
        for (Entry<String, ShardingSphereSchema> entry : schemas.entrySet()) {
            for (String each : entry.getValue().getAllTableNames()) {
                Object[] rows = new Object[10];
                rows[0] = entry.getKey();
                rows[1] = each;
                tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
            }
        }
    }
    
    private void appendOpenGaussRoleData(final ShardingSphereTableData tableData, final ShardingSphereMetaData metaData) {
        for (ShardingSphereUser each : metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class).getConfiguration().getUsers()) {
            Object[] rows = new Object[27];
            rows[0] = each.getGrantee().getUsername();
            tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
        }
    }
    
    private Enumerable<Object> createMemoryEnumerator(final ShardingSphereTableData tableData) {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new MemoryEnumerator(tableData.getRows());
            }
        };
    }
    
    private Collection<Statement> getStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups) {
        Collection<Statement> result = new LinkedList<>();
        for (ExecutionGroup<JDBCExecutionUnit> each : inputGroups) {
            for (JDBCExecutionUnit executionUnit : each.getInputs()) {
                result.add(executionUnit.getStorageResource());
            }
        }
        return result;
    }
    
    private void setParameters(final Collection<ExecutionGroup<JDBCExecutionUnit>> inputGroups) {
        for (ExecutionGroup<JDBCExecutionUnit> each : inputGroups) {
            for (JDBCExecutionUnit executionUnit : each.getInputs()) {
                if (!(executionUnit.getStorageResource() instanceof PreparedStatement)) {
                    continue;
                }
                setParameters((PreparedStatement) executionUnit.getStorageResource(), executionUnit.getExecutionUnit().getSqlUnit().getParameters());
            }
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void setParameters(final PreparedStatement preparedStatement, final List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            preparedStatement.setObject(i + 1, params.get(i));
        }
    }
    
    private AbstractEnumerable<Object> createEnumerable(final MergedResult mergedResult, final QueryResultMetaData metaData, final Collection<Statement> statements) {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new SQLFederationRowEnumerator(mergedResult, metaData, statements);
            }
        };
    }
    
    private QueryContext createQueryContext(final ShardingSphereMetaData metaData, final EnumerableScanExecutorContext sqlString, final DatabaseType databaseType, final boolean useCache) {
        String sql = sqlString.getSql().replace("\n", " ");
        SQLStatement sqlStatement = new SQLStatementParserEngine(databaseType.getType(),
                optimizerContext.getSqlParserRule().getSqlStatementCache(), optimizerContext.getSqlParserRule().getParseTreeCache(),
                optimizerContext.getSqlParserRule().isSqlCommentParseEnabled()).parse(sql, useCache);
        List<Object> params = getParameters(sqlString.getParamIndexes());
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(metaData, params, sqlStatement, executorContext.getDatabaseName());
        return new QueryContext(sqlStatementContext, sql, params, new HintValueContext(), useCache);
    }
    
    private List<Object> getParameters(final int[] paramIndexes) {
        if (null == paramIndexes) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>();
        for (int each : paramIndexes) {
            result.add(executorContext.getFederationContext().getQueryContext().getParameters().get(each));
        }
        return result;
    }
    
    private AbstractEnumerable<Object> createEmptyEnumerable() {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new EmptyRowEnumerator();
            }
        };
    }
}
