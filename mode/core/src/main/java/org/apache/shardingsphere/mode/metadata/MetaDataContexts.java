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
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilderFactory;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.data.ShardingSphereDataPersistService;

import java.util.Optional;

/**
 * Meta data contexts.
 */
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final MetaDataPersistService persistService;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereData shardingSphereData;
    
    public MetaDataContexts(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        this.persistService = persistService;
        this.metaData = metaData;
        this.shardingSphereData = initShardingSphereData(persistService, metaData);
    }
    
    private ShardingSphereData initShardingSphereData(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        Optional<ShardingSphereData> result = Optional.ofNullable(persistService.getShardingSphereDataPersistService()).flatMap(ShardingSphereDataPersistService::load);
        if (result.isPresent()) {
            return result.get();
        }
        if (metaData.getDatabases().isEmpty()) {
            return new ShardingSphereData();
        }
        return Optional.ofNullable(metaData.getDatabases().values().iterator().next().getProtocolType())
                .flatMap(protocolType -> ShardingSphereDataBuilderFactory.getInstance(protocolType).map(builder -> builder.build(metaData))).orElseGet(ShardingSphereData::new);
    }
    
    @Override
    public void close() {
        persistService.getRepository().close();
        metaData.getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource);
        metaData.getDatabases().values().forEach(each -> each.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource));
    }
}
