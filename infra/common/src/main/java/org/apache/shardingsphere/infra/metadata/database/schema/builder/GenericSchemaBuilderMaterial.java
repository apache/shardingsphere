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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere schema builder material.
 */
@RequiredArgsConstructor
@Getter
public final class GenericSchemaBuilderMaterial {
    
    private final DatabaseType protocolType;
    
    private final Map<String, DatabaseType> storageTypes;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    private final String defaultSchemaName;
    
    public GenericSchemaBuilderMaterial(final DatabaseType protocolType, final Map<String, StorageUnit> storageUnits,
                                        final Collection<ShardingSphereRule> rules, final ConfigurationProperties props, final String defaultSchemaName) {
        this(protocolType, storageUnits.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getStorageType(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                storageUnits.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                rules, props, defaultSchemaName);
    }
    
    /**
     * Judge whether same protocol and storage database types.
     * 
     * @return is same or not
     */
    public boolean isSameProtocolAndStorageTypes() {
        return storageTypes.values().stream().allMatch(protocolType::equals);
    }
}
