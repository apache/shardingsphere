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
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data nodes.
 */
@RequiredArgsConstructor
public final class DataNodes {
    
    private final Collection<ShardingSphereRule> rules;
    
    /**
     * Get data nodes.
     *
     * @param tableName table name
     * @return data nodes
     */
    @HighFrequencyInvocation
    public Collection<DataNode> getDataNodes(final String tableName) {
        Collection<DataNode> result = getDataNodesByTableName(tableName);
        if (result.isEmpty()) {
            return result;
        }
        for (ShardingSphereRule each : getOrderedRules()) {
            Optional<DataSourceMapperRuleAttribute> dataSourceMapperRuleAttribute = each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class);
            if (dataSourceMapperRuleAttribute.isPresent()) {
                result = buildDataNodes(result, dataSourceMapperRuleAttribute.get());
            }
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodesByTableName(final String tableName) {
        for (ShardingSphereRule each : rules) {
            Collection<DataNode> dataNodes = getDataNodesByTableName(each, tableName);
            if (!dataNodes.isEmpty()) {
                return Collections.unmodifiableCollection(dataNodes);
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<DataNode> getDataNodesByTableName(final ShardingSphereRule rule, final String tableName) {
        return rule.getAttributes().findAttribute(DataNodeRuleAttribute.class).map(optional -> optional.getDataNodesByTableName(tableName)).orElse(Collections.emptyList());
    }
    
    private Collection<ShardingSphereRule> getOrderedRules() {
        List<ShardingSphereRule> result = new ArrayList<>(rules);
        result.sort(Comparator.comparingInt(ShardingSphereRule::getOrder));
        return result;
    }
    
    private Collection<DataNode> buildDataNodes(final Collection<DataNode> dataNodes, final DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute) {
        Collection<DataNode> result = new LinkedList<>();
        Map<String, Collection<String>> dataSourceMapper = dataSourceMapperRuleAttribute.getDataSourceMapper();
        for (DataNode each : dataNodes) {
            result.addAll(DataNodeUtils.buildDataNode(each, dataSourceMapper));
        }
        return result;
    }
}
