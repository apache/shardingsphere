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

package org.apache.shardingsphere.infra.context.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.PersistService;
import org.apache.shardingsphere.infra.state.StateContext;

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
@Getter
public final class MetaDataContexts {
    
    private final PersistService persistService;
    
    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ExecutorEngine executorEngine;
    
    private final OptimizeContextFactory optimizeContextFactory;
    
    private final ConfigurationProperties props;
    
    private final StateContext stateContext;
    
    public MetaDataContexts(final PersistService persistService) {
        this(persistService, new LinkedHashMap<>(), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()),
                null, new ConfigurationProperties(new Properties()), new OptimizeContextFactory(new HashMap<>()));
    }
    
    public MetaDataContexts(final PersistService persistService, final Map<String, ShardingSphereMetaData> metaDataMap, final ShardingSphereRuleMetaData globalRuleMetaData,
                            final ExecutorEngine executorEngine, final ConfigurationProperties props, final OptimizeContextFactory optimizeContextFactory) {
        this.persistService = persistService;
        this.metaDataMap = new LinkedHashMap<>(metaDataMap);
        this.globalRuleMetaData = globalRuleMetaData;
        this.executorEngine = executorEngine;
        this.optimizeContextFactory = optimizeContextFactory;
        this.props = props;
        stateContext = new StateContext();
    }
    
    /**
     * Get persist service.
     *
     * @return persist service
     */
    public Optional<PersistService> getPersistService() {
        return Optional.ofNullable(persistService);
    }
    
    /**
     * Get all schema names.
     *
     * @return all schema names
     */
    public Collection<String> getAllSchemaNames() {
        return metaDataMap.keySet();
    }
    
    /**
     * Get mata data.
     *
     * @param schemaName schema name
     * @return mata data
     */
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        return metaDataMap.get(schemaName);
    }
    
    /**
     * Get lock.
     *
     * @return lock
     */
    public Optional<ShardingSphereLock> getLock() {
        return Optional.empty();
    }
}
