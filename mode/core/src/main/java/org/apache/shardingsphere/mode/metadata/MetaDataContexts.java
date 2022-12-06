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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilderFactory;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.metadata.persist.MetadataPersistService;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Metadata contexts.
 */
@Getter
public final class MetadataContexts implements AutoCloseable {
    
    private final MetadataPersistService persistService;
    
    private final ShardingSphereMetaData metadata;
    
    private final ShardingSphereData shardingSphereData;
    
    public MetadataContexts(final MetadataPersistService persistService, final ShardingSphereMetaData metadata) {
        this.persistService = persistService;
        this.metadata = metadata;
        this.shardingSphereData = initShardingSphereData(persistService, metadata);
    }
    
    private ShardingSphereData initShardingSphereData(final MetadataPersistService persistService, final ShardingSphereMetaData metadata) {
        if (metadata.getDatabases().isEmpty()) {
            return new ShardingSphereData();
        }
        ShardingSphereData result = Optional.ofNullable(metadata.getDatabases().values().iterator().next().getProtocolType())
                .flatMap(protocolType -> ShardingSphereDataBuilderFactory.getInstance(protocolType).map(builder -> builder.build(metadata))).orElseGet(ShardingSphereData::new);
        Optional<ShardingSphereData> loadedShardingSphereData = Optional.ofNullable(persistService.getShardingSphereDataPersistService())
                .flatMap(shardingSphereDataPersistService -> shardingSphereDataPersistService.load(metadata));
        loadedShardingSphereData.ifPresent(sphereData -> useLoadedToReplaceInit(result, sphereData));
        return result;
    }
    
    private void useLoadedToReplaceInit(final ShardingSphereData initShardingSphereData, final ShardingSphereData loadedShardingSphereData) {
        for (Entry<String, ShardingSphereDatabaseData> entry : initShardingSphereData.getDatabaseData().entrySet()) {
            if (loadedShardingSphereData.getDatabaseData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitByDatabaseData(entry.getValue(), loadedShardingSphereData.getDatabaseData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitByDatabaseData(final ShardingSphereDatabaseData initDatabaseData, final ShardingSphereDatabaseData loadedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : initDatabaseData.getSchemaData().entrySet()) {
            if (loadedDatabaseData.getSchemaData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitBySchemaData(entry.getValue(), loadedDatabaseData.getSchemaData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitBySchemaData(final ShardingSphereSchemaData initSchemaData, final ShardingSphereSchemaData loadedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : initSchemaData.getTableData().entrySet()) {
            if (loadedSchemaData.getTableData().containsKey(entry.getKey())) {
                entry.setValue(loadedSchemaData.getTableData().get(entry.getKey()));
            }
        }
    }
    
    @Override
    public void close() {
        persistService.getRepository().close();
        metadata.getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource);
        metadata.getDatabases().values().forEach(each -> each.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource));
    }
}
