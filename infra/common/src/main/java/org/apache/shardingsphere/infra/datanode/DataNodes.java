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
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

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
        dataNodeBuilders = OrderedSPILoader.getServices(DataNodeBuilder.class, rules);
    }
    
    /**
     * Get data nodes.
     * 
     * @param tableName table name
     * @return data nodes
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<DataNode> getDataNodes(final String tableName) {
        Collection<DataNode> result = new LinkedList<>(
                rules.stream().map(each -> getDataNodes(each, tableName)).filter(dataNodes -> !dataNodes.isEmpty()).findFirst().orElse(Collections.emptyList()));
        for (Entry<ShardingSphereRule, DataNodeBuilder> entry : dataNodeBuilders.entrySet()) {
            result = entry.getValue().build(result, entry.getKey());
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodes(final ShardingSphereRule rule, final String tableName) {
        return rule.getAttributes().findAttribute(DataNodeRuleAttribute.class).map(optional -> optional.getDataNodesByTableName(tableName)).orElse(Collections.emptyList());
    }
}
