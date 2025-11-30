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

package org.apache.shardingsphere.broadcast.metadata.reviser.schema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.revise.SchemaTableMetaDataAggregator;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema.SchemaTableAggregationReviser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Broadcast schema table aggregation reviser.
 */
@Slf4j
@RequiredArgsConstructor
public final class BroadcastSchemaTableAggregationReviser implements SchemaTableAggregationReviser<BroadcastRule> {
    
    private final boolean checkTableMetaDataEnabled;
    
    private final Map<String, Collection<TableMetaData>> tableMetaDataMap = new LinkedHashMap<>();
    
    @Override
    public void add(final TableMetaData metaData) {
        tableMetaDataMap.computeIfAbsent(metaData.getName(), key -> new LinkedList<>()).add(metaData);
    }
    
    @Override
    public Collection<TableMetaData> aggregate(final BroadcastRule rule) {
        return new SchemaTableMetaDataAggregator().aggregate(checkTableMetaDataEnabled, tableMetaDataMap);
    }
}
