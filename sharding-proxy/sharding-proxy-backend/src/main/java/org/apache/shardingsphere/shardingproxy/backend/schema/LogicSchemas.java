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
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logic schemas.
 */
@Getter
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
     * @param localSchemaNames local schema names
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @throws SQLException SQL exception
     */
    public void init(final Collection<String> localSchemaNames, 
                     final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        databaseType = DatabaseTypes.getActualDatabaseType(
                JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType());
        initSchemas(localSchemaNames, schemaDataSources, schemaRules);
    }
    
    private void initSchemas(final Collection<String> localSchemaNames, final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources, 
                             final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        if (schemaRules.isEmpty()) {
            String schema = schemaDataSources.keySet().iterator().next();
            logicSchemas.put(schema, LogicSchemaFactory.newInstance(schema, schemaDataSources, null));
        }
        for (Entry<String, Collection<RuleConfiguration>> entry : schemaRules.entrySet()) {
            if (localSchemaNames.isEmpty() || localSchemaNames.contains(entry.getKey())) {
                logicSchemas.put(entry.getKey(), LogicSchemaFactory.newInstance(entry.getKey(), schemaDataSources, entry.getValue()));
            }
        }
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return logicSchemas.containsKey(schema);
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
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) throws SQLException {
        logicSchemas.put(schemaAddedEvent.getShardingSchemaName(), LogicSchemaFactory.newInstance(schemaAddedEvent.getShardingSchemaName(), 
                Collections.singletonMap(schemaAddedEvent.getShardingSchemaName(), DataSourceConverter.getDataSourceParameterMap(schemaAddedEvent.getDataSourceConfigurations())), 
                schemaAddedEvent.getRuleConfigurations()));
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
