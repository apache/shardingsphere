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
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.spi.database.DataSourceInfo;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        parseEngine = SQLParseEngineFactory.getSQLParseEngine(DatabaseTypes.getDatabaseTypeName(LogicSchemas.getInstance().getDatabaseType()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get dataSource map.
     * @param dataSourceMap dataSource map
     * @return get dataSource map
     * @throws SQLException SQLException
     */
    public Map<String, DataSourceInfo> getDataSourceInfoMap(final Map<String, YamlDataSourceParameter> dataSourceMap) throws SQLException {
        Map<String, DataSourceInfo> result = new HashMap<String, DataSourceInfo>(dataSourceMap.size(), 1);
        for (Entry<String, YamlDataSourceParameter> entry : dataSourceMap.entrySet()) {
            YamlDataSourceParameter dataSource = entry.getValue();
            DataSourceInfo sourceInfo = new DataSourceInfo();
            sourceInfo.setUrl(dataSource.getUrl());
            result.put(entry.getKey(), sourceInfo);
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
    
    protected final Map<String, String> getDataSourceURLs(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, YamlDataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUrl());
        }
        return result;
    }
    
    protected final TableMetaDataInitializer getTableMetaDataInitializer(final DataSourceMetas dataSourceMetas) {
        ShardingProperties shardingProperties = ShardingProxyContext.getInstance().getShardingProperties();
        return new TableMetaDataInitializer(
                dataSourceMetas, BackendExecutorContext.getInstance().getExecuteEngine(), new ProxyTableMetaDataConnectionManager(getBackendDataSource()),
                shardingProperties.<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
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
