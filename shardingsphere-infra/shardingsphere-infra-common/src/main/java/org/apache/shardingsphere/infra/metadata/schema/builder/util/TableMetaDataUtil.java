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

package org.apache.shardingsphere.infra.metadata.schema.builder.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Table meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableMetaDataUtil {
    
    /**
     * Get data source actual table groups.
     *
     * @param tableNames table name collection
     * @param materials materials
     * @return datasource and table collection map
     */
    public static Map<String, Collection<String>> getDataSourceActualTableGroups(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (String each : tableNames) {
            Optional<DataNode> optional = dataNodes.getDataNodes(each).stream().findFirst();
            String dataSourceName = optional.map(DataNode::getDataSourceName).orElse(materials.getDataSourceMap().keySet().iterator().next());
            String tableName = optional.map(DataNode::getTableName).orElse(each);
            Collection<String> tables = result.getOrDefault(dataSourceName, new LinkedList<>());
            tables.add(tableName);
            result.putIfAbsent(dataSourceName, tables);
        }
        return result;
    }
}
