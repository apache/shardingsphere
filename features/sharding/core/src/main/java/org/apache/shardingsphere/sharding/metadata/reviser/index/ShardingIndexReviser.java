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

package org.apache.shardingsphere.sharding.metadata.reviser.index;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding index reviser.
 */
@RequiredArgsConstructor
public final class ShardingIndexReviser implements IndexReviser<ShardingRule> {
    
    private final ShardingTable shardingTable;
    
    @Override
    public Optional<IndexMetaData> revise(final String tableName, final IndexMetaData originalMetaData, final Collection<TableMetaData> originalTableMetaDataList,
                                          final ShardingRule rule) {
        return revise(tableName, originalMetaData, originalTableMetaDataList, Collections.emptyList(), rule);
    }
    
    @Override
    public Optional<IndexMetaData> revise(final String tableName, final IndexMetaData originalMetaData, final Collection<TableMetaData> originalTableMetaDataList,
                                          final Collection<TableMetaData> schemaMetaDataRevisionCandidateTableMetaDataList, final ShardingRule rule) {
        if (shardingTable.getActualDataNodes().isEmpty()) {
            return Optional.empty();
        }
        String actualTableName = tableName;
        String logicIndexName = IndexMetaDataUtils.findGeneratedLogicIndexName(
                originalMetaData.getName(), actualTableName,
                findCandidateLogicIndexNames(originalMetaData, originalTableMetaDataList, schemaMetaDataRevisionCandidateTableMetaDataList)).orElse(originalMetaData.getName());
        IndexMetaData result = new IndexMetaData(
                logicIndexName, originalMetaData.getColumns());
        result.setUnique(originalMetaData.isUnique());
        return Optional.of(result);
    }
    
    private Collection<String> findCandidateLogicIndexNames(final IndexMetaData originalMetaData, final Collection<TableMetaData> originalTableMetaDataList,
                                                            final Collection<TableMetaData> schemaMetaDataRevisionCandidateTableMetaDataList) {
        Collection<String> result = new LinkedHashSet<>();
        result.add(getGeneratedAnonymousIndexName(originalMetaData));
        for (TableMetaData eachTable : originalTableMetaDataList) {
            if (isActualTable(eachTable.getName())) {
                result.addAll(findCandidateLogicIndexNames(eachTable));
            }
        }
        for (TableMetaData each : schemaMetaDataRevisionCandidateTableMetaDataList) {
            if (isLogicTable(each.getName())) {
                result.addAll(findCandidateLogicIndexNamesFromRevisionCandidate(each));
            }
        }
        return result;
    }
    
    private Collection<String> findCandidateLogicIndexNames(final TableMetaData tableMetaData) {
        Collection<String> result = new LinkedHashSet<>();
        for (IndexMetaData each : tableMetaData.getIndexes()) {
            IndexMetaDataUtils.findGeneratedLogicIndexName(each.getName(), tableMetaData.getName(), Collections.emptyList())
                    .filter(optional -> !optional.equals(each.getName())).ifPresent(result::add);
            result.add(getGeneratedAnonymousIndexName(each));
        }
        return result;
    }
    
    private Collection<String> findCandidateLogicIndexNamesFromRevisionCandidate(final TableMetaData tableMetaData) {
        Collection<String> result = new LinkedHashSet<>();
        for (IndexMetaData each : tableMetaData.getIndexes()) {
            result.add(each.getName());
            result.add(getGeneratedAnonymousIndexName(each));
        }
        return result;
    }
    
    private boolean isActualTable(final String tableName) {
        return shardingTable.getActualDataNodes().stream().map(DataNode::getTableName).anyMatch(each -> each.equalsIgnoreCase(tableName));
    }
    
    private boolean isLogicTable(final String tableName) {
        return shardingTable.getLogicTable().equalsIgnoreCase(tableName);
    }
    
    private String getGeneratedAnonymousIndexName(final IndexMetaData originalMetaData) {
        return originalMetaData.getColumns().stream().map(each -> each + "_").collect(Collectors.joining("", "", "idx"));
    }
}
