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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.List;

/**
 * Sharding condition engine.
 */
@RequiredArgsConstructor
public final class ShardingConditionEngine {
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ShardingSphereDatabase database;
    
    private final ShardingRule shardingRule;
    
    /**
     * Create sharding conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @return sharding conditions
     */
    public List<ShardingCondition> createShardingConditions(final SQLStatementContext sqlStatementContext, final List<Object> params) {
        TimestampServiceRule timestampServiceRule = globalRuleMetaData.getSingleRule(TimestampServiceRule.class);
        return sqlStatementContext instanceof InsertStatementContext
                ? new InsertClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions((InsertStatementContext) sqlStatementContext, params)
                : new WhereClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions(sqlStatementContext, params);
    }
}
