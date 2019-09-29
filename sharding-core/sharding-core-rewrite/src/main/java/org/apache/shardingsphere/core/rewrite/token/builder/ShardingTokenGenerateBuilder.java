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

package org.apache.shardingsphere.core.rewrite.token.builder;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.ShardingConditionsAware;
import org.apache.shardingsphere.core.rewrite.token.generator.ShardingRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.AggregationDistinctTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.IndexTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.impl.TableTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertGeneratedKeyFromMetadataTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertGeneratedKeyNameTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertSetGeneratedKeyColumnTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.InsertValuesTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.OffsetTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.OrderByTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.ProjectionsTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.RowCountTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.impl.SelectItemPrefixTokenGenerator;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator builder for sharding.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingTokenGenerateBuilder implements SQLTokenGeneratorBuilder {
    
    private final ShardingRule shardingRule;
    
    private final ShardingConditions shardingConditions;
    
    @Override
    public Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = buildSQLTokenGenerators();
        for (SQLTokenGenerator each : result) {
            if (each instanceof ShardingRuleAware) {
                ((ShardingRuleAware) each).setShardingRule(shardingRule);
            }
            if (each instanceof ShardingConditionsAware) {
                ((ShardingConditionsAware) each).setShardingConditions(shardingConditions);
            }
        }
        return result;
    }
    
    private Collection<SQLTokenGenerator> buildSQLTokenGenerators() {
        Collection<SQLTokenGenerator> result = new LinkedList<>();
        result.add(new TableTokenGenerator());
        result.add(new SelectItemPrefixTokenGenerator());
        result.add(new ProjectionsTokenGenerator());
        result.add(new OrderByTokenGenerator());
        result.add(new AggregationDistinctTokenGenerator());
        result.add(new IndexTokenGenerator());
        result.add(new OffsetTokenGenerator());
        result.add(new RowCountTokenGenerator());
        result.add(new InsertGeneratedKeyNameTokenGenerator());
        result.add(new InsertGeneratedKeyFromMetadataTokenGenerator());
        result.add(new InsertSetGeneratedKeyColumnTokenGenerator());
        result.add(new InsertValuesTokenGenerator());
        return result;
    }
}
