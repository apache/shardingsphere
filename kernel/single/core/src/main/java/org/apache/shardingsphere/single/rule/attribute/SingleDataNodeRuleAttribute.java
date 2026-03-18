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

package org.apache.shardingsphere.single.rule.attribute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Single data node rule attribute.
 */
@RequiredArgsConstructor
public final class SingleDataNodeRuleAttribute implements DataNodeRuleAttribute {
    
    private final Map<String, Collection<DataNode>> tableDataNodes;
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableDataNodes;
    }
    
    @Override
    public Collection<DataNode> getDataNodesByTableName(final String tableName) {
        return tableDataNodes.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public boolean isReplicaBasedDistribution() {
        return false;
    }
}
