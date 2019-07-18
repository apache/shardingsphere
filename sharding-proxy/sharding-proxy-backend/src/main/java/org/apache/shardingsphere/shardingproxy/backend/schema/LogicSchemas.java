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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.SchemaDeletedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logic schemas.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
@Slf4j
public final class LogicSchemas {
    
    private static final LogicSchemas INSTANCE = new LogicSchemas();
    
    private final Map<String, LogicSchema> logicSchemas = new ConcurrentHashMap<>();
    
    private DatabaseType databaseType;
    
    private LogicSchemas() {
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get instance of logic schemas.
     *
     * @return instance of logic schemas.
     */
    public static LogicSchemas getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy context.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     */
    public void init(final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules) {
        init(schemaRules.keySet(), schemaDataSources, schemaRules, false);
    }
    
    /**
     * Initialize proxy context.
     *
     * @param localSchemaNames local schema names
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param isUsingRegistry is using registry or not
     */
    public void init(final Collection<String> localSchemaNames, final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources,
                     final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        databaseType = DatabaseTypes.getActualDatabaseType(
                JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType());
        initSchemas(localSchemaNames, schemaDataSources, schemaRules, isUsingRegistry);
    }
    
    private void initSchemas(final Collection<String> localSchemaNames, 
                             final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) {
        if (schemaRules.isEmpty()) {
            logicSchemas.put(schemaDataSources.keySet().iterator().next(), createLogicSchema(schemaDataSources.keySet().iterator().next(), schemaDataSources, null, isUsingRegistry));
        }
        for (Entry<String, RuleConfiguration> entry : schemaRules.entrySet()) {
            if (localSchemaNames.isEmpty() || localSchemaNames.contains(entry.getKey())) {
                logicSchemas.put(entry.getKey(), createLogicSchema(entry.getKey(), schemaDataSources, entry.getValue(), isUsingRegistry));
            }
        }
    }
    
    private LogicSchema createLogicSchema(
            final String schemaName, final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final RuleConfiguration ruleConfiguration, final boolean isUsingRegistry) {
        LogicSchema result;
        try {
            if (ruleConfiguration instanceof ShardingRuleConfiguration) {
                result = new ShardingSchema(schemaName, schemaDataSources.get(schemaName), (ShardingRuleConfiguration) ruleConfiguration, isUsingRegistry);
            } else if (ruleConfiguration instanceof MasterSlaveRuleConfiguration) {
                result = new MasterSlaveSchema(schemaName, schemaDataSources.get(schemaName), (MasterSlaveRuleConfiguration) ruleConfiguration, isUsingRegistry);
            } else if (ruleConfiguration instanceof EncryptRuleConfiguration) {
                result = new EncryptSchema(schemaName, schemaDataSources.get(schemaName), (EncryptRuleConfiguration) ruleConfiguration);
            } else {
                result = new TransparentSchema(schemaName, schemaDataSources.get(schemaName));
            }
        } catch (final Exception ex) {
            log.error("Exception occur when create schema {}.\nThe exception detail is {}.", schemaName, ex.getMessage());
            throw ex;
        }
        return result;
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return logicSchemas.keySet().contains(schema);
    }
    
    /**
     * Get logic schema.
     *
     * @param schemaName schema name
     * @return sharding schema
     */
    public LogicSchema getLogicSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : logicSchemas.get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(logicSchemas.keySet());
    }
    
    /**
     * Renew to add new schema.
     *
     * @param schemaAddedEvent schema add changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) {
        logicSchemas.put(schemaAddedEvent.getShardingSchemaName(), createLogicSchema(schemaAddedEvent.getShardingSchemaName(), 
                Collections.singletonMap(schemaAddedEvent.getShardingSchemaName(), DataSourceConverter.getDataSourceParameterMap(schemaAddedEvent.getDataSourceConfigurations())), 
                schemaAddedEvent.getRuleConfiguration(), true));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param schemaDeletedEvent schema delete changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent schemaDeletedEvent) {
        logicSchemas.remove(schemaDeletedEvent.getShardingSchemaName());
    }
}
