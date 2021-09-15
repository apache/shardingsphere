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

package org.apache.shardingsphere.infra.optimize;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContext;
import org.apache.shardingsphere.infra.optimize.core.convert.SqlNodeConvertEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

@RequiredArgsConstructor
public final class ShardingSphereOptimizer {
    
    @Getter
    private final OptimizeContext context;
    
    /**
     * Optimize.
     * @param sql sql
     * @return rel node
     * @throws SQLParsingException sql parsing exception
     */
    public RelNode optimize(final String sql) throws SQLParsingException {
        try {
            // TODO : Remove the following statement after SqlNodeConvertEngine becomes available.
            // SqlNode sqlNode = SqlParser.create(sql, context.getParserConfig()).parseQuery();
            ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(context.getDatabaseType()));
            SqlNode sqlNode = SqlNodeConvertEngine.convert(sqlParserEngine.parse(sql, true));
            SqlNode validNode = context.getValidator().validate(sqlNode);
            RelNode logicPlan = context.getRelConverter().convertQuery(validNode, false, true).rel;
            return optimize(logicPlan);
        } catch (final UnsupportedOperationException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private RelNode optimize(final RelNode logicPlan) {
        RelOptPlanner planner = context.getRelConverter().getCluster().getPlanner();
        planner.setRoot(planner.changeTraits(logicPlan, context.getRelConverter().getCluster().traitSet().replace(EnumerableConvention.INSTANCE)));
        return planner.findBestExp();
    }
}
