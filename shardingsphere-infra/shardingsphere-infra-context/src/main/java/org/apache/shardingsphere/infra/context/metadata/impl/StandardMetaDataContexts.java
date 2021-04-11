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

package org.apache.shardingsphere.infra.context.metadata.impl;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.optimize.context.CalciteContextFactory;
import org.apache.shardingsphere.infra.state.StateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Standard meta data contexts.
 */
@Getter
public final class StandardMetaDataContexts implements MetaDataContexts {
    
    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final ShardingSphereRuleMetaData globalRuleMetaData;
    
    private final ExecutorEngine executorEngine;
    
    private final CalciteContextFactory calciteContextFactory;
    
    private final ShardingSphereUsers users;
    
    private final ConfigurationProperties props;
    
    private final StateContext stateContext;
    
    public StandardMetaDataContexts() {
        this(new LinkedHashMap<>(), 
                new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()), null, new ShardingSphereUsers(new HashSet<>()), new ConfigurationProperties(new Properties()));
    }
    
    public StandardMetaDataContexts(final Map<String, ShardingSphereMetaData> metaDataMap, final ShardingSphereRuleMetaData globalRuleMetaData, 
                                    final ExecutorEngine executorEngine, final ShardingSphereUsers users, final ConfigurationProperties props) {
        this.metaDataMap = new LinkedHashMap<>(metaDataMap);
        this.globalRuleMetaData = globalRuleMetaData;
        this.executorEngine = executorEngine;
        calciteContextFactory = new CalciteContextFactory(metaDataMap);
        this.users = users;
        this.props = props;
        stateContext = new StateContext();
    }
    
    @Override
    public Collection<String> getAllSchemaNames() {
        return metaDataMap.keySet();
    }
    
    @Override
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        return metaDataMap.get(schemaName);
    }
    
    @Override
    public ShardingSphereMetaData getDefaultMetaData() {
        return getMetaData(DefaultSchema.LOGIC_NAME);
    }
    
    @Override
    public Optional<ShardingSphereLock> getLock() {
        return Optional.empty();
    }
    
    @Override
    public void close() {
        executorEngine.close();
    }
}
