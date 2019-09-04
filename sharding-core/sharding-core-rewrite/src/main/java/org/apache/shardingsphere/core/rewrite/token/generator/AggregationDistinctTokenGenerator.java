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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.AggregationDistinctToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Aggregation distinct token generator.
 *
 * @author panjuan
 */
public final class AggregationDistinctTokenGenerator implements CollectionSQLTokenGenerator<ShardingRule>, IgnoreForSingleRoute {
    
    @Override
    public Collection<AggregationDistinctToken> generateSQLTokens(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        if (!(optimizedStatement instanceof ShardingSelectOptimizedStatement)) {
            return Collections.emptyList();
        }
        Collection<AggregationDistinctToken> result = new LinkedList<>();
        for (AggregationDistinctSelectItem each : ((ShardingSelectOptimizedStatement) optimizedStatement).getSelectItems().getAggregationDistinctSelectItems()) {
            result.add(createAggregationDistinctToken(each));
        }
        return result;
    }
    
    private AggregationDistinctToken createAggregationDistinctToken(final AggregationDistinctSelectItem item) {
        Preconditions.checkArgument(item.getAlias().isPresent());
        String derivedAlias = DerivedColumn.isDerivedColumnName(item.getAlias().get()) ? item.getAlias().get() : null;
        return new AggregationDistinctToken(item.getStartIndex(), item.getStopIndex(), item.getDistinctInnerExpression(), derivedAlias);
    }
}
