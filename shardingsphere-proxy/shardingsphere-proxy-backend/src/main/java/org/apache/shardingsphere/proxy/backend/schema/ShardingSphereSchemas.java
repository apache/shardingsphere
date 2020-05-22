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

package org.apache.shardingsphere.proxy.backend.schema;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.util.DataSourceConverter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere schemas.
 */
@Getter
public final class ShardingSphereSchemas {
    
    private static final ShardingSphereSchemas INSTANCE = new ShardingSphereSchemas();
    
    private final Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>();
    
    private DatabaseType databaseType;
    
    private ShardingSphereSchemas() {
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get instance of ShardingSphere schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ShardingSphereSchemas getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy context.
     *
     * @param localSchemaNames schema names
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @throws SQLException SQL exception
     */
    public void init(final Collection<String> localSchemaNames,
                     final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        databaseType = DatabaseTypes.getActualDatabaseType(
                JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType());
        initSchemas(localSchemaNames, schemaDataSources, schemaRules);
    }
    
    private void initSchemas(final Collection<String> localSchemaNames, final Map<String, Map<String, DataSourceParameter>> schemaDataSources, 
                             final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        if (schemaRules.isEmpty()) {
            String schema = schemaDataSources.keySet().iterator().next();
            schemas.put(schema, new ShardingSphereSchema(schema, schemaDataSources.get(schema), Collections.emptyList()));
        }
        for (Entry<String, Collection<RuleConfiguration>> entry : schemaRules.entrySet()) {
            if (localSchemaNames.isEmpty() || localSchemaNames.contains(entry.getKey())) {
                schemas.put(entry.getKey(), new ShardingSphereSchema(entry.getKey(), schemaDataSources.get(entry.getKey()), entry.getValue()));
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
        return schemas.containsKey(schema);
    }
    
    /**
     * Get ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : schemas.get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(schemas.keySet());
    }
    
    /**
     * Renew to add new schema.
     *
     * @param schemaAddedEvent schema add changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) throws SQLException {
        schemas.put(schemaAddedEvent.getShardingSchemaName(), new ShardingSphereSchema(schemaAddedEvent.getShardingSchemaName(), 
                DataSourceConverter.getDataSourceParameterMap(schemaAddedEvent.getDataSourceConfigurations()), schemaAddedEvent.getRuleConfigurations()));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param schemaDeletedEvent schema delete changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent schemaDeletedEvent) {
        schemas.remove(schemaDeletedEvent.getShardingSchemaName());
    }
}
