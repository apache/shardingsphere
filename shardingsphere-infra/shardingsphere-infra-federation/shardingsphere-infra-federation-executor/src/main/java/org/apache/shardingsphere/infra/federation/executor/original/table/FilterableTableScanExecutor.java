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

package org.apache.shardingsphere.infra.federation.executor.original.table;

import lombok.SneakyThrows;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.tools.RelBuilder;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.federation.executor.original.row.FilterableRowEnumerator;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationTableMetaData;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Filterable table scan executor.
 */
public final class FilterableTableScanExecutor {
    
    private final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCExecutorCallback<? extends ExecuteResult> callback;
    
    private final ConfigurationProperties props;
    
    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final RelOptCluster cluster;
    
    private final RelOptSchema relOptSchema;
    
    public FilterableTableScanExecutor(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                       final JDBCExecutor jdbcExecutor, final JDBCExecutorCallback<? extends ExecuteResult> callback,
                                       final ConfigurationProperties props, final OptimizerContext optimizerContext) {
        this.jdbcExecutor = jdbcExecutor;
        this.callback = callback;
        this.prepareEngine = prepareEngine;
        this.props = props;
        this.metaDataMap = optimizerContext.getMetaDataMap();
        this.cluster = optimizerContext.getPlannerContexts().get("sharding_db").getConverter().getCluster();
        this.relOptSchema = (RelOptSchema) optimizerContext.getPlannerContexts().get("sharding_db").getValidator().getCatalogReader();
    }
    
    /**
     * Execute.
     *
     * @param tableMetaData federation table meta data
     * @param scanContext filterable table scan context
     * @return query results
     */
    public Enumerable<Object[]> execute(final FederationTableMetaData tableMetaData, final FilterableTableScanContext scanContext) {
        RelBuilder relBuilder = RelFactories.LOGICAL_BUILDER.create(cluster, relOptSchema);
        RelNode relNode = relBuilder.scan(tableMetaData.getName()).filter(scanContext.getFilters()).build();
        RelToSqlConverter relToSqlConverter = new RelToSqlConverter(MysqlSqlDialect.DEFAULT);
        String sql = relToSqlConverter.visitRoot(relNode).asStatement().toString();
        SQLStatement sqlStatement = new SQLStatementParserEngine("MySQL", new ConfigurationProperties(new Properties())).parse(sql, true);
        LogicSQL logicSQL = createLogicSQL(metaDataMap, sql, sqlStatement);
        // TODO: 2021/9/29 convert sqlstatement to sqlnode 
        ShardingSphereMetaData sphereMetaData = metaDataMap.get("sharding_db");
        ExecutionContext context = new KernelProcessor().generateExecutionContext(logicSQL, sphereMetaData, props);
        try {
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(context.getRouteContext(), context.getExecutionUnits());
            ExecuteProcessEngine.initialize(context.getLogicSQL(), executionGroupContext, props);
            List<QueryResult> result = jdbcExecutor.execute(executionGroupContext, callback).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return createAbstractEnumerable(sphereMetaData, context, result);
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
    
    @SneakyThrows
    private AbstractEnumerable<Object[]> createAbstractEnumerable(final ShardingSphereMetaData sphereMetaData, final ExecutionContext context, final List<QueryResult> result) {
        MergedResult mergedResult = mergeQuery(result, sphereMetaData, context.getSqlStatementContext());
        return new AbstractEnumerable<Object[]>() {
            
            @Override
            public Enumerator<Object[]> enumerator() {
                return new FilterableRowEnumerator(mergedResult, result.get(0).getMetaData());
            }
        };
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults, final ShardingSphereMetaData sphereMetaData, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine("sharding_db", sphereMetaData.getResource().getDatabaseType(), sphereMetaData.getSchema(),
                props, sphereMetaData.getRuleMetaData().getRules());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    private LogicSQL createLogicSQL(final Map<String, ShardingSphereMetaData> metaDataMap, final String sql, final SQLStatement sqlStatement) {
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataMap, Collections.emptyList(), sqlStatement, sql);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
}
