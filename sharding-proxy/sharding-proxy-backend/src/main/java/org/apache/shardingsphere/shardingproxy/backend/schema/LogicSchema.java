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

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Logic schema.
 *
 * @author panjuan
 */
@Getter
public abstract class LogicSchema {
    
    private final String name;
    
    private final SQLParseEngine parseEngine;
    
    private JDBCBackendDataSource backendDataSource;
    
    public LogicSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources) {
        this.name = name;
        parseEngine = SQLParseEngineFactory.getSQLParseEngine(DatabaseTypes.getTrunkDatabaseTypeName(LogicSchemas.getInstance().getDatabaseType()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    protected final Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap() {
        Map<String, DatabaseAccessConfiguration> result = new HashMap<>(backendDataSource.getDataSourceParameters().size(), 1);
        for (Entry<String, YamlDataSourceParameter> entry : backendDataSource.getDataSourceParameters().entrySet()) {
            YamlDataSourceParameter dataSource = entry.getValue();
            DatabaseAccessConfiguration dataSourceInfo = new DatabaseAccessConfiguration(dataSource.getUrl(), null, null);
            result.put(entry.getKey(), dataSourceInfo);
        }
        return result;
    }
    
    /**
     * Get sharding meta data.
     * 
     * @return sharding meta data.
     */
    public abstract ShardingSphereMetaData getMetaData();
    
    /**
     * Get Sharding rule.
     * 
     * @return sharding rule
     */
    // TODO : It is used in many places, but we can consider how to optimize it because of being irrational for logic schema.
    public abstract ShardingRule getShardingRule();
    
    /**
     * Get data source parameters.
     * 
     * @return data source parameters
     */
    public Map<String, YamlDataSourceParameter> getDataSources() {
        return backendDataSource.getDataSourceParameters();
    }
    
    /**
     * Renew data source configuration.
     *
     * @param dataSourceChangedEvent data source changed event.
     * @throws Exception exception
     */
    @Subscribe
    public final synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) throws Exception {
        if (!name.equals(dataSourceChangedEvent.getShardingSchemaName())) {
            return;
        }
        backendDataSource.renew(DataSourceConverter.getDataSourceParameterMap(dataSourceChangedEvent.getDataSourceConfigurations()));
    }
    
    /**
     * Refresh table meta data.
     * 
     * @param sqlStatementContext SQL statement context
     * @throws SQLException SQL exception
     */
    public void refreshTableMetaData(final SQLStatementContext sqlStatementContext) throws SQLException {
    }
}
