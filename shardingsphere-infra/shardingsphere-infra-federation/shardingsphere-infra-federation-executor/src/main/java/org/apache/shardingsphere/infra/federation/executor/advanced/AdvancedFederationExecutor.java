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

import org.apache.calcite.interpreter.InterpretableConvention;
import org.apache.calcite.interpreter.InterpretableConverter;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.federation.executor.FederationContext;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.advanced.resultset.FederationResultSet;
import org.apache.shardingsphere.infra.federation.optimizer.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.enumerable.EnumerableMergedResult;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Advanced federation executor.
 */
public final class AdvancedFederationExecutor implements FederationExecutor {
    
    private final String databaseName;
    
    private final String schemaName;
    
    private final OptimizerContext optimizerContext;
    
    private final ShardingSphereOptimizer optimizer;
    
    private ResultSet federationResultSet;
    
    public AdvancedFederationExecutor(final String databaseName, final String schemaName, final OptimizerContext optimizerContext) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.optimizerContext = optimizerContext;
        optimizer = new ShardingSphereOptimizer(optimizerContext);
    }
    
    @Override
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final FederationContext federationContext) throws SQLException {
        String sql = federationContext.getLogicSQL().getSql();
        ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(
                federationContext.getDatabases().get(databaseName).getProtocolType().getType(), new CacheOption(1, 1), new CacheOption(1, 1), false);
        SQLStatement sqlStatement = parserEngine.parse(sql, false);
        Enumerable<Object[]> enumerableResult = execute(sqlStatement);
        MergedResult mergedResult = new EnumerableMergedResult(enumerableResult);
        federationResultSet = new FederationResultSet(mergedResult);
        return federationResultSet;
    }
    
    private Enumerable<Object[]> execute(final SQLStatement sqlStatement) {
        // TODO
        return execute(optimizer.optimize(databaseName, schemaName, sqlStatement));
    }
    
    private Enumerable<Object[]> execute(final RelNode bestPlan) {
        RelOptCluster cluster = bestPlan.getCluster();
        SqlValidator validator = optimizerContext.getPlannerContexts().get(databaseName).getValidators().get(schemaName);
        SqlToRelConverter converter = optimizerContext.getPlannerContexts().get(databaseName).getConverters().get(schemaName);
        return new FederateInterpretableConverter(
                cluster, cluster.traitSetOf(InterpretableConvention.INSTANCE), bestPlan).bind(new AdvancedExecuteDataContext(validator, converter));
    }
    
    @Override
    public ResultSet getResultSet() {
        return federationResultSet;
    }
    
    @Override
    public void close() {
        // TODO
    }
    
    public static final class FederateInterpretableConverter extends InterpretableConverter {
        
        public FederateInterpretableConverter(final RelOptCluster cluster, final RelTraitSet traits, final RelNode input) {
            super(cluster, traits, input);
        }
    }
}
