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

package org.apache.shardingsphere.sharding.rewrite.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator.GeneratedKeyAssignmentTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator.GeneratedKeyForUseDefaultInsertColumnsTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator.GeneratedKeyInsertColumnTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator.GeneratedKeyInsertValuesTokenGenerator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingAggregationDistinctTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingConstraintTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingCursorTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingDistinctProjectionPrefixTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingFetchDirectionTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingIndexTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingInsertValuesTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingOffsetTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingOrderByTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingProjectionsTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRemoveTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingRowCountTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingTableTokenGenerator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for sharding.
 */
@RequiredArgsConstructor
public final class ShardingTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final ShardingRule rule;
    
    private final RouteContext routeContext;
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        addSQLTokenGenerator(result, new ShardingTableTokenGenerator(rule));
        addSQLTokenGenerator(result, new ShardingProjectionsTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyAssignmentTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyInsertValuesTokenGenerator());
        addSQLTokenGenerator(result, new ShardingDistinctProjectionPrefixTokenGenerator());
        addSQLTokenGenerator(result, new ShardingOrderByTokenGenerator());
        addSQLTokenGenerator(result, new ShardingAggregationDistinctTokenGenerator());
        addSQLTokenGenerator(result, new ShardingIndexTokenGenerator(rule));
        addSQLTokenGenerator(result, new ShardingConstraintTokenGenerator(rule));
        addSQLTokenGenerator(result, new ShardingOffsetTokenGenerator());
        addSQLTokenGenerator(result, new ShardingRowCountTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyInsertColumnTokenGenerator());
        addSQLTokenGenerator(result, new GeneratedKeyForUseDefaultInsertColumnsTokenGenerator());
        addSQLTokenGenerator(result, new ShardingInsertValuesTokenGenerator());
        addSQLTokenGenerator(result, new ShardingRemoveTokenGenerator());
        addSQLTokenGenerator(result, new ShardingCursorTokenGenerator(rule));
        addSQLTokenGenerator(result, new ShardingFetchDirectionTokenGenerator());
        return result;
    }
    
    private void addSQLTokenGenerator(final Collection<SQLTokenGenerator> sqlTokenGenerators, final SQLTokenGenerator toBeAddedSQLTokenGenerator) {
        if (toBeAddedSQLTokenGenerator instanceof IgnoreForSingleRoute && routeContext.isSingleRouting()) {
            return;
        }
        if (toBeAddedSQLTokenGenerator instanceof RouteContextAware) {
            ((RouteContextAware) toBeAddedSQLTokenGenerator).setRouteContext(routeContext);
        }
        if (toBeAddedSQLTokenGenerator.isGenerateSQLToken(sqlStatementContext)) {
            sqlTokenGenerators.add(toBeAddedSQLTokenGenerator);
        }
    }
}
