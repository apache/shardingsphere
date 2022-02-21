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

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Optional;

/**
 * ShardingSphere rule which contains mutable data node.
 */
public interface MutableDataNodeRule extends ShardingSphereRule {
    
    /**
     * Add data node.
     *
     * @param tableName table name
     * @param dataSourceName data source name
     */
    void put(String tableName, String dataSourceName);
    
    /**
     * Remove data node.
     *
     * @param tableName table name
     */
    void remove(String tableName);
    
    /**
     * Find single data node by table name.
     *
     * @param tableName table name
     * @return single table data node
     */
    Optional<DataNode> findSingleTableDataNode(String tableName);
}
