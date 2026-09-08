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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pipeline data node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineDataNodeUtils {
    
    /**
     * Build table and data nodes map.
     *
     * @param database database
     * @param schemaTableNames resolved schema table names, empty schema name means schema unavailable
     * @return table and data nodes map
     * @throws PipelineInvalidParameterException thrown invalid parameter exception when can't get data nodes.
     */
    public static Map<String, List<DataNode>> buildTableAndDataNodesMap(final ShardingSphereDatabase database, final Map<String, Set<String>> schemaTableNames) {
        int tableCount = schemaTableNames.values().stream().mapToInt(Collection::size).sum();
        ShardingSpherePreconditions.checkState(0 < tableCount, () -> new PipelineInvalidParameterException("Table names are empty."));
        Map<String, List<DataNode>> result = new HashMap<>(tableCount, 1F);
        Collection<DataNodeRuleAttribute> attributes = database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class);
        // TODO support virtual data source name
        for (Entry<String, Set<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                ShardingSpherePreconditions.checkState(!result.containsKey(each),
                        () -> new PipelineInvalidParameterException(String.format("More than one schema table has the same table name `%s`.", each)));
                Collection<DataNode> dataNodes = findDataNodes(entry.getKey(), each, attributes);
                ShardingSpherePreconditions.checkNotEmpty(dataNodes, () -> new PipelineInvalidParameterException(String.format("Not find actual data nodes of `%s`", each)));
                result.put(each, new ArrayList<>(dataNodes));
            }
        }
        return result;
    }
    
    private static Collection<DataNode> findDataNodes(final String schemaName, final String tableName, final Collection<DataNodeRuleAttribute> attributes) {
        for (DataNodeRuleAttribute each : attributes) {
            Collection<DataNode> dataNodes = each.getDataNodesByTableName(tableName);
            if (!dataNodes.isEmpty()) {
                Collection<DataNode> matchedDataNodes = filterDataNodesBySchema(schemaName, each, dataNodes);
                return matchedDataNodes.isEmpty() || !each.isReplicaBasedDistribution() ? matchedDataNodes : Collections.singleton(matchedDataNodes.iterator().next());
            }
        }
        return Collections.emptyList();
    }
    
    private static Collection<DataNode> filterDataNodesBySchema(final String schemaName, final DataNodeRuleAttribute ruleAttribute, final Collection<DataNode> dataNodes) {
        if (schemaName.isEmpty() || !ruleAttribute.isDataNodeTableNameLoadedFromStorage()) {
            return dataNodes;
        }
        return dataNodes.stream().filter(each -> schemaName.equals(each.getSchemaName())).collect(Collectors.toList());
    }
}
