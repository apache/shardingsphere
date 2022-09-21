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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
    
    private volatile ShardingSphereTransactionManagerEngine resource;
    
    public TransactionRule(final TransactionRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        log.debug("Create transaction rule");
        configuration = ruleConfig;
        defaultType = TransactionType.valueOf(ruleConfig.getDefaultType().toUpperCase());
        providerType = ruleConfig.getProviderType();
        props = ruleConfig.getProps();
        this.databases = databases;
        resource = createTransactionManagerEngine(databases);
    }
    
    private synchronized ShardingSphereTransactionManagerEngine createTransactionManagerEngine(final Map<String, ShardingSphereDatabase> databases) {
        if (databases.isEmpty()) {
            return new ShardingSphereTransactionManagerEngine();
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(databases.size());
        Set<DatabaseType> databaseTypes = new HashSet<>();
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            ShardingSphereDatabase database = entry.getValue();
            database.getResource().getDataSources().forEach((key, value) -> {
                dataSourceMap.put(database.getName() + "." + key, value);
            });
            if (null != entry.getValue().getResource().getDatabaseType()) {
                databaseTypes.add(entry.getValue().getResource().getDatabaseType());
            }
        }
        Preconditions.checkState(databaseTypes.size() < 2, "Multiple types of databases are not supported");
        if (dataSourceMap.isEmpty()) {
            return new ShardingSphereTransactionManagerEngine();
        }
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        result.init(databaseTypes.iterator().next(), dataSourceMap, providerType);
        return result;
    }
    
    @Override
    public synchronized void addResource(final ShardingSphereDatabase database) {
        // TODO process null when for information_schema
        if (null == database) {
            return;
        }
        log.debug("Transaction rule add resource: {}", database.getName());
        rebuildEngine();
    }
    
    @Override
    public synchronized void closeStaleResource(final String databaseName) {
        if (!databases.containsKey(databaseName.toLowerCase())) {
            return;
        }
        log.debug("Transaction rule close resource: {}", databaseName);
        rebuildEngine();
    }
    
    @Override
    public synchronized void closeStaleResource() {
        log.debug("Transaction rule close all resources");
        closeEngine();
    }
    
    private void rebuildEngine() {
        ShardingSphereTransactionManagerEngine previousEngine = resource;
        if (null != previousEngine) {
            closeEngine(previousEngine);
        }
        resource = createTransactionManagerEngine(databases);
    }
    
    private void closeEngine() {
        ShardingSphereTransactionManagerEngine engine = resource;
        if (null != engine) {
            closeEngine(engine);
            resource = new ShardingSphereTransactionManagerEngine();
        }
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
