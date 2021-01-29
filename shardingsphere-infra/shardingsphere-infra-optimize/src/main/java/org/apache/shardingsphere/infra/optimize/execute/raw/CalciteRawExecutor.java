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

package org.apache.shardingsphere.infra.optimize.execute.raw;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.interpreter.InterpretableConvention;
import org.apache.calcite.interpreter.InterpretableConverter;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.optimize.context.CalciteContext;
import org.apache.shardingsphere.infra.optimize.execute.CalciteExecutor;
import org.apache.shardingsphere.infra.optimize.execute.raw.context.CalciteDataContext;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

/**
 * Calcite raw executor.
 */
@RequiredArgsConstructor
public final class CalciteRawExecutor implements CalciteExecutor {
    
    private final CalciteContext context;
    
    @Override
    public List<QueryResult> executeQuery(final String sql, final List<Object> parameters) {
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
    
    private Enumerable<Object[]> execute(final String sql) throws SqlParseException {
        // TODO The below will be replaced by SqlNodeConverter.
        SqlNode sqlNode = SqlParser.create(sql, context.getParserConfig()).parseQuery();
        SqlNode validNode = context.getValidator().validate(sqlNode);
        RelNode logicPlan = context.getRelConverter().convertQuery(validNode, false, true).rel;
        RelNode bestPlan = optimize(logicPlan);
        return execute(bestPlan);
    }
    
    private Enumerable<Object[]> execute(final RelNode bestPlan) {
        RelOptCluster cluster = context.getRelConverter().getCluster();
        return new CalciteInterpretableConverter(cluster, cluster.traitSetOf(InterpretableConvention.INSTANCE), bestPlan).bind(new CalciteDataContext(context));
    }
    
    private RelNode optimize(final RelNode logicPlan) {
        RelOptPlanner planner = context.getRelConverter().getCluster().getPlanner();
        planner.setRoot(planner.changeTraits(logicPlan, context.getRelConverter().getCluster().traitSet().replace(EnumerableConvention.INSTANCE)));
        return planner.findBestExp();
    }
    
    public static final class CalciteInterpretableConverter extends InterpretableConverter {
        
        public CalciteInterpretableConverter(final RelOptCluster cluster, final RelTraitSet traits, final RelNode input) {
            super(cluster, traits, input);
        }
    }
}
