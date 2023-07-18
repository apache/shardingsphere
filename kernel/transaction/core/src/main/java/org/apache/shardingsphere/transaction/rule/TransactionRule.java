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

package org.apache.shardingsphere.transaction.rule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.api.TransactionType;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transaction rule.
 */
@Getter
@Slf4j
public final class TransactionRule implements GlobalRule, ResourceHeldRule<ShardingSphereTransactionManagerEngine> {
    
    private final TransactionRuleConfiguration configuration;
    
    private final TransactionType defaultType;
    
    private final String providerType;
    
    private final Properties props;
    
    private final Map<String, ShardingSphereDatabase> databases;
    
    private final AtomicReference<ShardingSphereTransactionManagerEngine> resource;
    
    public TransactionRule(final TransactionRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        defaultType = TransactionType.valueOf(ruleConfig.getDefaultType().toUpperCase());
        providerType = ruleConfig.getProviderType();
        props = ruleConfig.getProps();
        this.databases = new ConcurrentHashMap<>(databases);
        resource = new AtomicReference<>(createTransactionManagerEngine(this.databases));
    }
    
    private synchronized ShardingSphereTransactionManagerEngine createTransactionManagerEngine(final Map<String, ShardingSphereDatabase> databases) {
        if (databases.isEmpty()) {
            return new ShardingSphereTransactionManagerEngine(defaultType);
        }
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(databases.size(), 1F);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(databases.size(), 1F);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            ShardingSphereDatabase database = entry.getValue();
            database.getResourceMetaData().getDataSources().forEach((key, value) -> dataSourceMap.put(database.getName() + "." + key, value));
            database.getResourceMetaData().getStorageTypes().forEach((key, value) -> databaseTypes.put(database.getName() + "." + key, value));
        }
        if (dataSourceMap.isEmpty()) {
            return new ShardingSphereTransactionManagerEngine(defaultType);
        }
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine(defaultType);
        result.init(databaseTypes, dataSourceMap, providerType);
        return result;
    }
    
    /**
     * Get resource.
     * 
     * @return resource
     */
    public ShardingSphereTransactionManagerEngine getResource() {
        return resource.get();
    }
    
    @Override
    public synchronized void addResource(final ShardingSphereDatabase database) {
        // TODO process null when for information_schema
        if (null == database) {
            return;
        }
        databases.put(database.getName(), database);
        rebuildEngine();
    }
    
    @Override
    public synchronized void closeStaleResource(final String databaseName) {
        if (!databases.containsKey(databaseName.toLowerCase())) {
            return;
        }
        databases.remove(databaseName);
        rebuildEngine();
    }
    
    @Override
    public synchronized void closeStaleResource() {
        databases.clear();
        closeEngine();
    }
    
    private void rebuildEngine() {
        ShardingSphereTransactionManagerEngine previousEngine = resource.get();
        if (null != previousEngine) {
            closeEngine(previousEngine);
        }
        resource.set(createTransactionManagerEngine(databases));
    }
    
    private void closeEngine() {
        ShardingSphereTransactionManagerEngine engine = resource.get();
        if (null != engine) {
            closeEngine(engine);
            resource.set(new ShardingSphereTransactionManagerEngine(defaultType));
        }
    }
    
    private void closeEngine(final ShardingSphereTransactionManagerEngine engine) {
        try {
            engine.close();
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Close transaction engine failed", ex);
        }
    }
    
    @Override
    public String getType() {
        return TransactionRule.class.getSimpleName();
    }
}
