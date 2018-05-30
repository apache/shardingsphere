/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.asserts;

import io.shardingsphere.core.rule.DataNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Collection of data nodes.
 *
 * @author zhangliang
 */
public final class DataNodes {
    
    private final Map<String, Collection<String>> dataNodeMap = new LinkedHashMap<>();
    
    /**
     * Add data nodes.
     *
     * @param dataNodes data nodes
     */
    public void add(final Collection<DataNode> dataNodes) {
        for (DataNode each : dataNodes) {
            add(each);
        }
    }
    
    /**
     * Add data node.
     * 
     * @param dataNode data node
     */
    public void add(final DataNode dataNode) {
        if (!dataNodeMap.containsKey(dataNode.getDataSourceName())) {
            dataNodeMap.put(dataNode.getDataSourceName(), new LinkedList<String>());
        }
        dataNodeMap.get(dataNode.getDataSourceName()).add(dataNode.getTableName());
    }
    
    /**
     * Get data source names.
     *
     * @return data source names
     */
    public Collection<String> getDataSourceNames() {
        return dataNodeMap.keySet();
    }
    
    /**
     * Get table names.
     * 
     * @param dataSourceName data source name
     * @return table names
     */
    public Collection<String> getTableNames(final String dataSourceName) {
        return dataNodeMap.get(dataSourceName);
    }
}
