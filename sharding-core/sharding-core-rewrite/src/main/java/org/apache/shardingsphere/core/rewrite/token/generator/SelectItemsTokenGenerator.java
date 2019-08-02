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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectItemsToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Select items token generator.
 *
 * @author zhangliang
 */
public final class SelectItemsTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule>, IgnoreForSingleRoute {
    
    @Override
    public Optional<SelectItemsToken> generateSQLToken(
            final OptimizedStatement optimizedStatement, final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        if (!(optimizedStatement instanceof ShardingSelectOptimizedStatement)) {
            return Optional.absent();
        }
        Collection<String> derivedItemTexts = getDerivedItemTexts((ShardingSelectOptimizedStatement) optimizedStatement);
        return derivedItemTexts.isEmpty() ? Optional.<SelectItemsToken>absent()
                : Optional.of(new SelectItemsToken(((ShardingSelectOptimizedStatement) optimizedStatement).getSelectItems().getStopIndex() + 1 + " ".length(), derivedItemTexts));
    }
    
    private Collection<String> getDerivedItemTexts(final ShardingSelectOptimizedStatement optimizedStatement) {
        Collection<String> result = new LinkedList<>();
        for (SelectItem each : optimizedStatement.getSelectItems().getItems()) {
            if (each instanceof AggregationSelectItem && !((AggregationSelectItem) each).getDerivedAggregationItems().isEmpty()) {
                result.addAll(Lists.transform(((AggregationSelectItem) each).getDerivedAggregationItems(), new Function<AggregationSelectItem, String>() {
                    
                    @Override
                    public String apply(final AggregationSelectItem input) {
                        return getDerivedItemText(input);
                    }
                }));
            } else if (each instanceof DerivedSelectItem) {
                result.add(getDerivedItemText(each));
            }
        }
        return result;
    }
    
    private String getDerivedItemText(final SelectItem selectItem) {
        Preconditions.checkState(selectItem.getAlias().isPresent());
        if (selectItem instanceof AggregationDistinctSelectItem) {
            return ((AggregationDistinctSelectItem) selectItem).getDistinctInnerExpression() + " AS " + selectItem.getAlias().get() + " ";
        }
        return selectItem.getExpression() + " AS " + selectItem.getAlias().get() + " ";
    }
}
