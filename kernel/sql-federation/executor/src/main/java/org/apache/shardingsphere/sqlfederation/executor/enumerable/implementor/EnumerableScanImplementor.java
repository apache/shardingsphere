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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.implementor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.connection.SQLExecutionInterruptedException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementor;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementorContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.enumerator.EmptyDataRowEnumerator;
import org.apache.shardingsphere.sqlfederation.executor.context.ExecutorContext;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.jdbc.JDBCDataRowEnumerator;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory.MemoryDataRowEnumerator;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory.MemoryTableStatisticsBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enumerable scan implementor.
 */
@RequiredArgsConstructor
public final class EnumerableScanImplementor implements ScanImplementor {
    
    private final QueryContext queryContext;
    
    private final CompilerContext compilerContext;
    
    private final ExecutorContext executorContext;
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    @Override
    public Enumerable<Object> implement(final ShardingSphereTable table, final ScanImplementorContext scanContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        DatabaseType databaseType = sqlStatementContext.getSqlStatement().getDatabaseType();
        Collection<String> systemSchemas = new SystemDatabase(databaseType).getSystemSchemas();
        if (sqlStatementContext.getTablesContext().getSchemaNames().stream().anyMatch(systemSchemas::contains)) {
            return createMemoryEnumerable(sqlStatementContext, databaseType, table);
        }
        QueryContext scanQueryContext = createQueryContext(queryContext.getMetaData(), scanContext, databaseType, queryContext.isUseCache());
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(
                scanQueryContext, queryContext.getMetaData().getGlobalRuleMetaData(), queryContext.getMetaData().getProps());
        if (executorContext.isPreview()) {
            executorContext.getPreviewExecutionUnits().addAll(executionContext.getExecutionUnits());
            return createEmptyEnumerable();
        }
        return createJDBCEnumerable(scanQueryContext, queryContext.getMetaData().getDatabase(executorContext.getCurrentDatabaseName()), executionContext);
    }
    
    private Enumerable<Object> createMemoryEnumerable(final SQLStatementContext sqlStatementContext, final DatabaseType databaseType, final ShardingSphereTable table) {
        Optional<DialectDriverQuerySystemCatalogOption> driverQuerySystemCatalogOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getDriverQuerySystemCatalogOption();
        if (driverQuerySystemCatalogOption.isPresent() && driverQuerySystemCatalogOption.get().isSystemTable(table.getName())) {
            return createMemoryEnumerator(MemoryTableStatisticsBuilder.buildTableStatistics(table, queryContext.getMetaData(), driverQuerySystemCatalogOption.get()), table, databaseType);
        }
        String databaseName = sqlStatementContext.getTablesContext().getDatabaseName().orElse(executorContext.getCurrentDatabaseName());
        String schemaName = sqlStatementContext.getTablesContext().getSchemaName().orElse(executorContext.getCurrentSchemaName());
        Optional<TableStatistics> tableStatistics = Optional.ofNullable(executorContext.getStatistics().getDatabaseStatistics(databaseName))
                .map(optional -> optional.getSchemaStatistics(schemaName)).map(optional -> optional.getTableStatistics(table.getName()));
        return tableStatistics.map(optional -> createMemoryEnumerator(optional, table, databaseType)).orElseGet(this::createEmptyEnumerable);
    }
    
    private Enumerable<Object> createMemoryEnumerator(final TableStatistics tableStatistics, final ShardingSphereTable table, final DatabaseType databaseType) {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new MemoryDataRowEnumerator(tableStatistics.getRows(), table.getAllColumns(), databaseType);
            }
        };
    }
    
    private QueryContext createQueryContext(final ShardingSphereMetaData metaData, final ScanImplementorContext sqlString, final DatabaseType databaseType, final boolean useCache) {
        String sql = sqlString.getSql().replace(System.lineSeparator(), " ");
        SQLStatement sqlStatement = compilerContext.getSqlParserRule().getSQLParserEngine(databaseType).parse(sql, useCache);
        HintValueContext hintValueContext = new HintValueContext();
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, executorContext.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
        return new QueryContext(sqlStatementContext, sql, getParameters(sqlString.getParamIndexes()), hintValueContext, queryContext.getConnectionContext(), metaData, useCache);
    }
    
    private List<Object> getParameters(final int[] paramIndexes) {
        return null == paramIndexes
                ? Collections.emptyList()
                : Arrays.stream(paramIndexes).mapToObj(each -> queryContext.getParameters().get(each)).collect(Collectors.toCollection(() -> new ArrayList<>(paramIndexes.length)));
    }
    
    private AbstractEnumerable<Object> createEmptyEnumerable() {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new EmptyDataRowEnumerator();
            }
        };
    }
    
    private AbstractEnumerable<Object> createJDBCEnumerable(final QueryContext queryContext, final ShardingSphereDatabase database, final ExecutionContext executionContext) {
        return new AbstractEnumerable<Object>() {
            
            @SneakyThrows
            @Override
            public Enumerator<Object> enumerator() {
                computeConnectionOffsets(executionContext);
                ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepare(database, executionContext);
                setParameters(executionGroupContext.getInputGroups());
                ShardingSpherePreconditions.checkState(!ProcessRegistry.getInstance().get(executorContext.getProcessId()).isInterrupted(), SQLExecutionInterruptedException::new);
                processEngine.executeSQL(executionGroupContext, queryContext);
                List<QueryResult> queryResults =
                        executorContext.getJdbcExecutor().execute(executionGroupContext, executorContext.getQueryCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
                MergeEngine mergeEngine = new MergeEngine(queryContext.getMetaData(), database, queryContext.getMetaData().getProps(),
                        queryContext.getConnectionContext());
                MergedResult mergedResult = mergeEngine.merge(queryResults, queryContext);
                Collection<Statement> statements = getStatements(executionGroupContext.getInputGroups());
                return new JDBCDataRowEnumerator(mergedResult, queryResults.get(0).getMetaData(), statements);
            }
        };
    }
    
    private void computeConnectionOffsets(final ExecutionContext context) {
        for (ExecutionUnit each : context.getExecutionUnits()) {
            int connectionOffset = executorContext.getConnectionOffsets().containsKey(each.getDataSourceName()) ? executorContext.getConnectionOffsets().get(each.getDataSourceName()) + 1 : 0;
            executorContext.getConnectionOffsets().put(each.getDataSourceName(), connectionOffset);
        }
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> prepare(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        // TODO pass grantee from proxy and jdbc adapter
        return executorContext.getPrepareEngine().prepare(database.getName(), executionContext, executorContext.getConnectionOffsets(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(executorContext.getProcessId(), database.getName()));
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
                if (executionUnit.getStorageResource() instanceof PreparedStatement) {
                    setParameters((PreparedStatement) executionUnit.getStorageResource(), executionUnit.getExecutionUnit().getSqlUnit().getParameters());
                }
            }
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void setParameters(final PreparedStatement preparedStatement, final List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            preparedStatement.setObject(i + 1, params.get(i));
        }
    }
}
