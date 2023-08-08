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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableReferenceRulesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Show sharding table reference rules executor.
 */
public final class ShowShardingTableReferenceRuleExecutor implements RQLExecutor<ShowShardingTableReferenceRulesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShardingTableReferenceRulesStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (final ShardingTableReferenceRuleConfiguration referenceRule : ((ShardingRuleConfiguration) rule.get().getConfiguration()).getBindingTableGroups()) {
            if (null == sqlStatement.getRuleName() || referenceRule.getName().equalsIgnoreCase(sqlStatement.getRuleName())) {
                result.add(new LocalDataQueryResultRow(referenceRule.getName(), referenceRule.getReference()));
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "sharding_table_reference");
    }
    
    @Override
    public Class<ShowShardingTableReferenceRulesStatement> getType() {
        return ShowShardingTableReferenceRulesStatement.class;
    }
}
