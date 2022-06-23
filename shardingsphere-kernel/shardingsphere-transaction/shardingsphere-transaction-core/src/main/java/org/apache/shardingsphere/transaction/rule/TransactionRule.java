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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Transaction rule.
 */
@Getter
@Slf4j
public final class TransactionRule implements GlobalRule, InstanceAwareRule, ResourceHeldRule<ShardingSphereTransactionManagerEngine> {
    
    private final TransactionRuleConfiguration configuration;
    
    private final TransactionType defaultType;
    
    private final String providerType;
    
    private final Properties props;
    
    private final Map<String, ShardingSphereDatabase> databases;
    
    private volatile Map<String, ShardingSphereTransactionManagerEngine> resources;
    
    public TransactionRule(final TransactionRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        defaultType = TransactionType.valueOf(ruleConfig.getDefaultType().toUpperCase());
        providerType = ruleConfig.getProviderType();
        props = ruleConfig.getProps();
        this.databases = databases;
    }
    
    @Override
    public synchronized void setInstanceContext(final InstanceContext instanceContext) {
        resources = createTransactionManagerEngines(databases, instanceContext);
    }
    
    private Map<String, ShardingSphereTransactionManagerEngine> createTransactionManagerEngines(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        Map<String, ShardingSphereTransactionManagerEngine> result = new HashMap<>(databases.keySet().size(), 1);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            result.put(entry.getKey(), createTransactionManagerEngine(entry.getValue()));
        }
        return result;
    }
    
    private ShardingSphereTransactionManagerEngine createTransactionManagerEngine(final ShardingSphereDatabase database) {
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        ShardingSphereResource resource = database.getResource();
        result.init(resource.getDatabaseType(), resource.getDataSources(), providerType);
        return result;
    }
    
    @Override
    public synchronized void addResource(final ShardingSphereDatabase database) {
        // TODO process null when for information_schema
        if (null == database) {
            return;
        }
        ShardingSphereTransactionManagerEngine previousEngine = resources.put(database.getName(), createTransactionManagerEngine(database));
        if (null != previousEngine) {
            closeEngine(previousEngine);
        }
    }
    
    @Override
    public synchronized void closeStaleResource(final String databaseName) {
        ShardingSphereTransactionManagerEngine engine = resources.remove(databaseName);
        if (null != engine) {
            closeEngine(engine);
        }
    }
    
    @Override
    public void closeStaleResources() {
        resources.values().forEach(this::closeEngine);
    }
    
    private void closeEngine(final ShardingSphereTransactionManagerEngine engine) {
        try {
            engine.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Close transaction engine failed", ex);
        }
    }
    
    @Override
    public String getType() {
        return TransactionRule.class.getSimpleName();
    }
}
