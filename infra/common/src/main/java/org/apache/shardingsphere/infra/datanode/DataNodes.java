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

package org.apache.shardingsphere.infra.datanode;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Data nodes.
 */
@RequiredArgsConstructor
public final class DataNodes {
    
    private final Collection<ShardingSphereRule> rules;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, DataNodeBuilder> dataNodeBuilders;
    
    public DataNodes(final Collection<ShardingSphereRule> rules) {
        this.rules = rules;
        dataNodeBuilders = DataNodeBuilderFactory.getInstances(rules);
    }
    
    /**
     * Get data nodes.
     * 
     * @param tableName table name
     * @return data nodes
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<DataNode> getDataNodes(final String tableName) {
        Optional<DataNodeContainedRule> dataNodeContainedRule = findDataNodeContainedRule(tableName);
        if (!dataNodeContainedRule.isPresent()) {
            return Collections.emptyList();
        }
        Collection<DataNode> result = new LinkedList<>(dataNodeContainedRule.get().getDataNodesByTableName(tableName));
        for (Entry<ShardingSphereRule, DataNodeBuilder> entry : dataNodeBuilders.entrySet()) {
            result = entry.getValue().build(result, entry.getKey());
        }
        return result;
    }
    
    private Optional<DataNodeContainedRule> findDataNodeContainedRule(final String tableName) {
        return rules.stream().filter(each -> isDataNodeContainedRuleContainsTable(each, tableName)).findFirst().map(optional -> (DataNodeContainedRule) optional);
    }
    
    private boolean isDataNodeContainedRuleContainsTable(final ShardingSphereRule each, final String tableName) {
        return each instanceof DataNodeContainedRule && !((DataNodeContainedRule) each).getDataNodesByTableName(tableName).isEmpty();
    }
}
