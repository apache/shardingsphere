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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.AggregationDistinctTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.DistinctProjectionPrefixTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.IndexTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.OffsetTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.OrderByTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ProjectionsTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.RowCountTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingInsertValuesTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.TableTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyAssignmentTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyForUseDefaultInsertColumnsTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyInsertColumnTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyInsertValuesTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for sharding.
 */
@RequiredArgsConstructor
public final class ShardingTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final ShardingRule shardingRule;
    
    private final RouteContext routeContext;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = buildSQLTokenGenerators();
        for (SQLTokenGenerator each : result) {
            if (each instanceof ShardingRuleAware) {
                ((ShardingRuleAware) each).setShardingRule(shardingRule);
            }
            if (each instanceof RouteContextAware) {
                ((RouteContextAware) each).setRouteContext(routeContext);
            }
        }
        return result;
    }
    
    private Collection<SQLTokenGenerator> buildSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        addSQLTokenGenerator(result, new TableTokenGenerator());
        addSQLTokenGenerator(result, new DistinctProjectionPrefixTokenGenerator());
        addSQLTokenGenerator(result, new ProjectionsTokenGenerator());
        addSQLTokenGenerator(result, new OrderByTokenGenerator());
        addSQLTokenGenerator(result, new AggregationDistinctTokenGenerator());
        addSQLTokenGenerator(result, new IndexTokenGenerator());
        addSQLTokenGenerator(result, new OffsetTokenGenerator());
        addSQLTokenGenerator(result, new RowCountTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyInsertColumnTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyForUseDefaultInsertColumnsTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyAssignmentTokenGenerator());
        addSQLTokenGenerator(result, new ShardingInsertValuesTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyInsertValuesTokenGenerator());
        return result;
    }
    
    private void addSQLTokenGenerator(final Collection<SQLTokenGenerator> sqlTokenGenerators, final SQLTokenGenerator toBeAddedSQLTokenGenerator) {
        if (toBeAddedSQLTokenGenerator instanceof IgnoreForSingleRoute && routeContext.isSingleRouting()) {
            return;
        }
        sqlTokenGenerators.add(toBeAddedSQLTokenGenerator);
    }
}
