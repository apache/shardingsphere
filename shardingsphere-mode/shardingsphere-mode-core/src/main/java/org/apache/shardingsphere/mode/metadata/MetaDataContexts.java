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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Meta data contexts.
 */
@RequiredArgsConstructor
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final MetaDataPersistService persistService;
    
    private final Map<String, ShardingSphereDatabaseMetaData> databaseMetaDataMap;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final OptimizerContext optimizerContext;
    
    private final ConfigurationProperties props;
    
    public MetaDataContexts(final MetaDataPersistService persistService) {
        this(persistService, new LinkedHashMap<>(), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()),
                OptimizerContextFactory.create(new HashMap<>(), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList())), new ConfigurationProperties(new Properties()));
    }
    
    /**
     * Get persist service.
     *
     * @return persist service
     */
    public Optional<MetaDataPersistService> getPersistService() {
        return Optional.ofNullable(persistService);
    }
    
    /**
     * Get all database names.
     *
     * @return all database names
     */
    public Collection<String> getAllDatabaseNames() {
        return databaseMetaDataMap.keySet();
    }
    
    /**
     * Get database meta data.
     *
     * @param databaseName database name
     * @return database meta data
     */
    public ShardingSphereDatabaseMetaData getDatabaseMetaData(final String databaseName) {
        return databaseMetaDataMap.get(databaseName);
    }
    
    @Override
    public void close() throws Exception {
        if (null != persistService) {
            persistService.getRepository().close();
        }
    }
}
