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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data node utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataNodeUtil {
    
    /**
     * Get data node groups.
     *
     * @param dataNodes data nodes
     * @return data node groups, key is data source name, values are data nodes belong to this data source
     */
    public static Map<String, List<DataNode>> getDataNodeGroups(final Collection<DataNode> dataNodes) {
        Map<String, List<DataNode>> result = new LinkedHashMap<>(dataNodes.size(), 1);
        for (DataNode each : dataNodes) {
            String dataSourceName = each.getDataSourceName();
            if (!result.containsKey(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
}
