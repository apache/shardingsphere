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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;
import java.util.HashSet;

/**
 * In used sharding storage unit retriever.
 */
public final class InUsedShardingStorageUnitRetriever implements InUsedStorageUnitRetriever<ShardingRule> {
    
    @Override
    public Collection<String> getInUsedResources(final ShowRulesUsedStorageUnitStatement sqlStatement, final ShardingRule rule) {
        Collection<String> result = new HashSet<>(rule.getShardingTables().size(), 1F);
        for (ShardingTable each : rule.getShardingTables().values()) {
            if (new CaseInsensitiveSet<>(each.getActualDataSourceNames()).contains(sqlStatement.getStorageUnitName())) {
                result.add(each.getLogicTable());
            }
        }
        return result;
    }
    
    @Override
    public Class<ShardingRule> getType() {
        return ShardingRule.class;
    }
}
