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

package org.apache.shardingsphere.sharding.metadata.reviser.constraint;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint.ConstraintReviser;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Optional;

/**
 * Sharding constraint reviser.
 */
@RequiredArgsConstructor
public final class ShardingConstraintReviser implements ConstraintReviser<ShardingRule> {
    
    private final ShardingTable shardingTable;
    
    @Override
    public Optional<ConstraintMetaData> revise(final String tableName, final ConstraintMetaData originalMetaData, final ShardingRule rule) {
        for (DataNode each : shardingTable.getActualDataNodes()) {
            String referencedTableName = originalMetaData.getReferencedTableName();
            Optional<String> logicIndexName = getLogicIndex(originalMetaData.getName(), each.getTableName());
            if (logicIndexName.isPresent()) {
                return Optional.of(new ConstraintMetaData(
                        logicIndexName.get(), rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findLogicTableByActualTable(referencedTableName).orElse(referencedTableName)));
            }
        }
        return Optional.empty();
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? Optional.of(actualIndexName.replace(indexNameSuffix, "")) : Optional.empty();
    }
}
