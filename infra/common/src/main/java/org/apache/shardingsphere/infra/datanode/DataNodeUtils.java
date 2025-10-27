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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataNodeUtils {
    
    /**
     * Get data node groups.
     *
     * @param dataNodes data nodes
     * @return data node groups, key is data source name, values are data nodes belong to this data source
     */
    public static Map<String, List<DataNode>> getDataNodeGroups(final Collection<DataNode> dataNodes) {
        Map<String, List<DataNode>> result = new LinkedHashMap<>(dataNodes.size(), 1F);
        for (DataNode each : dataNodes) {
            result.computeIfAbsent(each.getDataSourceName(), unused -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    /**
     * Build data node.
     *
     * @param dataNode data node
     * @param dataSources dataSource map
     * @return data node collection
     */
    public static Collection<DataNode> buildDataNode(final DataNode dataNode, final Map<String, Collection<String>> dataSources) {
        if (!dataSources.containsKey(dataNode.getDataSourceName())) {
            return Collections.singletonList(dataNode);
        }
        Collection<DataNode> result = new LinkedList<>();
        for (String each : dataSources.get(dataNode.getDataSourceName())) {
            result.add(new DataNode(each, (String) null, dataNode.getTableName()));
        }
        return result;
    }
    
    /**
     * Get formatted data nodes.
     *
     * @param amount amount
     * @param logicTable logic table
     * @param dataSources data source names
     * @return formatted data node list
     */
    public static List<String> getFormattedDataNodes(final int amount, final String logicTable, final Collection<String> dataSources) {
        List<String> result = new LinkedList<>();
        Iterator<String> iterator = dataSources.iterator();
        for (int i = 0; i < amount; i++) {
            if (!iterator.hasNext()) {
                iterator = dataSources.iterator();
            }
            result.add(String.format("%s.%s_%s", iterator.next(), logicTable, i));
        }
        return result;
    }
}
