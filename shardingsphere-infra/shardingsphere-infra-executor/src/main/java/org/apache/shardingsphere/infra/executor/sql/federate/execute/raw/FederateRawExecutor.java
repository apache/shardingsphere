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

package org.apache.shardingsphere.infra.executor.sql.federate.execute.raw;

import lombok.RequiredArgsConstructor;
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
import org.apache.shardingsphere.infra.executor.sql.federate.execute.FederateExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Federate raw executor.
 */
@RequiredArgsConstructor
public final class FederateRawExecutor implements FederateExecutor {
    
    private final ShardingSphereOptimizer optimizer;
    
    public FederateRawExecutor(final OptimizeContext context) {
        optimizer = new ShardingSphereOptimizer(context);
    }
    
    @Override
    public List<QueryResult> executeQuery(final ExecutionContext executionContext, final JDBCExecutorCallback<? extends ExecuteResult> callback, 
                                          final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        // TODO
        return Collections.emptyList();
    }
    
    @Override
    public void close() {
        // TODO
    }
    
    @Override
    public ResultSet getResultSet() {
        return null;
    }
    
    private Enumerable<Object[]> execute(final String sql) {
        // TODO
        return execute(optimizer.optimize(sql));
    }
    
    private Enumerable<Object[]> execute(final RelNode bestPlan) {
        RelOptCluster cluster = optimizer.getContext().getRelConverter().getCluster();
        return new FederateInterpretableConverter(cluster, cluster.traitSetOf(InterpretableConvention.INSTANCE), bestPlan).bind(new FederateExecuteDataContext(optimizer.getContext()));
    }
    
    public static final class FederateInterpretableConverter extends InterpretableConverter {
        
        public FederateInterpretableConverter(final RelOptCluster cluster, final RelTraitSet traits, final RelNode input) {
            super(cluster, traits, input);
        }
    }
}
