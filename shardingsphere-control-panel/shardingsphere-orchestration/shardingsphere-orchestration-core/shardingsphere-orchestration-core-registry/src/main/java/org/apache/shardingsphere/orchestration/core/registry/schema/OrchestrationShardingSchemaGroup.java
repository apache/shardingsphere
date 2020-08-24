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

package org.apache.shardingsphere.orchestration.core.registry.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Orchestration sharding schema group.
 */
public final class OrchestrationShardingSchemaGroup {
    
    private final Map<String, Collection<String>> schemaGroup = new HashMap<>();
    
    /**
     * Add orchestration sharding schema.
     * 
     * @param orchestrationShardingSchema orchestration sharding schema
     */
    public void add(final OrchestrationSchema orchestrationShardingSchema) {
        String schemaName = orchestrationShardingSchema.getSchemaName();
        if (!schemaGroup.containsKey(schemaName)) {
            schemaGroup.put(schemaName, new LinkedList<>());
        }
        schemaGroup.get(schemaName).add(orchestrationShardingSchema.getDataSourceName());
    }
    
    /**
     * Put orchestration sharding schema.
     * 
     * @param shardingSchemaName sharding schema name
     * @param dataSourceNames data source names
     */
    public void put(final String shardingSchemaName, final Collection<String> dataSourceNames) {
        schemaGroup.put(shardingSchemaName, dataSourceNames);
    }
    
    /**
     * Get data source names.
     * 
     * @param shardingSchemaName sharding schema name
     * @return data source names
     */
    public Collection<String> getDataSourceNames(final String shardingSchemaName) {
        return schemaGroup.getOrDefault(shardingSchemaName, Collections.emptyList());
    }
}
