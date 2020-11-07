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

package org.apache.shardingsphere.infra.rule.type;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere rule contains data node.
 */
public interface DataNodeContainedRule extends ShardingSphereRule {
    
    /**
     * Get all data nodes.
     *
     * @return all data nodes map, key is logic table name, values are data node collection belong to the key
     */
    Map<String, Collection<DataNode>> getAllDataNodes();
    
    /**
     * Get all actual tables.
     * 
     * @return all actual tables
     */
    Collection<String> getAllActualTables();
    
    /**
     * Find first actual table name.
     *
     * @param logicTable logic table name
     * @return the first actual table name
     */
    Optional<String> findFirstActualTable(String logicTable);
    
    /**
     * Is need accumulate.
     * 
     * @param tables table names
     * @return need accumulate
     */
    boolean isNeedAccumulate(Collection<String> tables);
    
    /**
     * Find logic table name via actual table name.
     *
     * @param actualTable actual table name
     * @return logic table name
     */
    Optional<String> findLogicTableByActualTable(String actualTable);
}
