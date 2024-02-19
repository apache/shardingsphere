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

import org.apache.shardingsphere.distsql.handler.executor.rql.rule.CountResultRowBuilder;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;

/**
 * Sharding count result row builder.
 */
public final class ShardingCountResultRowBuilder implements CountResultRowBuilder<ShardingRule> {
    
    @Override
    public Collection<LocalDataQueryResultRow> generateRows(final ShardingRule rule, final String databaseName) {
        return Arrays.asList(new LocalDataQueryResultRow("sharding_table", databaseName, rule.getShardingTables().size()),
                new LocalDataQueryResultRow("sharding_table_reference", databaseName, rule.getConfiguration().getBindingTableGroups().size()));
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public String getType() {
        return "SHARDING";
    }
}
