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

package org.apache.shardingsphere.infra.executor.sql.federate.customized;

import org.apache.calcite.interpreter.InterpretableConvention;
import org.apache.calcite.interpreter.InterpretableConverter;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.federate.FederationExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.customized.CustomizedOptimizerContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Customized filterable executor.
 */
public final class CustomizedFilterableExecutor implements FederationExecutor {
    
    private final String schemaName;
    
    private final ShardingSphereOptimizer optimizer;
    
    public CustomizedFilterableExecutor(final String schemaName, final CustomizedOptimizerContext context) {
        this.schemaName = schemaName;
        optimizer = new ShardingSphereOptimizer(context);
    }
    
    @Override
    public List<QueryResult> executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, 
                                          final JDBCExecutorCallback<? extends ExecuteResult> callback, final ExecutionContext executionContext) throws SQLException {
        // TODO
        return Collections.emptyList();
    }
    
    @Override
    public ResultSet getResultSet() {
        return null;
    }
    
    private Enumerable<Object[]> execute(final SQLStatement sqlStatement) {
        // TODO
        return execute(optimizer.optimize(schemaName, sqlStatement));
    }
    
    private Enumerable<Object[]> execute(final RelNode bestPlan) {
        RelOptCluster cluster = optimizer.getContext().getConverters().get(schemaName).getCluster();
        return new FederateInterpretableConverter(
                cluster, cluster.traitSetOf(InterpretableConvention.INSTANCE), bestPlan).bind(new CustomizedFilterableExecuteDataContext(schemaName, optimizer.getContext()));
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
