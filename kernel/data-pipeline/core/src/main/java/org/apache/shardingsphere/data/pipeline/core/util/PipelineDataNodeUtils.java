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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pipeline data node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineDataNodeUtils {
    
    /**
     * Build table and data nodes map.
     *
     * @param database database
     * @param tableNames table names
     * @return table and data nodes map
     * @throws PipelineInvalidParameterException thrown invalid parameter exception when can't get data nodes.
     */
    public static Map<String, List<DataNode>> buildTableAndDataNodesMap(final ShardingSphereDatabase database, final Collection<String> tableNames) {
        ShardingSpherePreconditions.checkNotEmpty(tableNames, () -> new PipelineInvalidParameterException("Table names are empty."));
        Map<String, List<DataNode>> result = new HashMap<>(tableNames.size(), 1F);
        Collection<DataNodeRuleAttribute> attributes = database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class);
        // TODO support virtual data source name
        for (String each : tableNames) {
            Collection<DataNode> dataNodes = findDataNodes(each, attributes);
            ShardingSpherePreconditions.checkNotEmpty(dataNodes, () -> new PipelineInvalidParameterException(String.format("Not find actual data nodes of `%s`", each)));
            result.put(each, new ArrayList<>(dataNodes));
        }
        return result;
    }
    
    private static Collection<DataNode> findDataNodes(final String tableName, final Collection<DataNodeRuleAttribute> attributes) {
        for (DataNodeRuleAttribute each : attributes) {
            Collection<DataNode> dataNodes = each.getDataNodesByTableName(tableName);
            if (!dataNodes.isEmpty()) {
                return each.isReplicaBasedDistribution() ? Collections.singleton(dataNodes.iterator().next()) : dataNodes;
            }
        }
        return Collections.emptyList();
    }
}
