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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.rule.attribute.SingleDataNodeRuleAttribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * In used single storage unit retriever.
 */
public final class InUsedSingleStorageUnitRetriever implements InUsedStorageUnitRetriever<SingleRule> {
    
    @Override
    public Collection<String> getInUsedResources(final ShowRulesUsedStorageUnitStatement sqlStatement, final SingleRule rule) {
        SingleDataNodeRuleAttribute attribute = rule.getAttributes().getAttribute(SingleDataNodeRuleAttribute.class);
        Map<String, Collection<DataNode>> dataNodes = attribute.getAllDataNodes();
        Collection<String> result = new HashSet<>(dataNodes.size(), 1F);
        for (Collection<DataNode> each : dataNodes.values()) {
            String storageUnitName = each.iterator().next().getDataSourceName();
            if (storageUnitName.equalsIgnoreCase(sqlStatement.getStorageUnitName())) {
                result.add(each.iterator().next().getTableName());
            }
        }
        return result;
    }
    
    @Override
    public Class<SingleRule> getType() {
        return SingleRule.class;
    }
}
