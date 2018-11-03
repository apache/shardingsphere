/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.runtime;

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.orchestration.internal.event.config.DataSourceChangedEvent;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.shardingproxy.util.DataSourceConverter;
import lombok.Getter;

import java.util.Map;

/**
 * Logic schema.
 *
 * @author panjuan
 */
@Getter
public class LogicSchema {
    
    private final String name;
    
    private final Map<String, DataSourceParameter> dataSources;
    
    private JDBCBackendDataSource backendDataSource;
    
    private final ShardingMetaData metaData;
    
    public LogicSchema(final String name, final Map<String, DataSourceParameter> dataSources, final ShardingMetaData shardingMetaData) {
        this.name = name;
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        this.dataSources = dataSources;
        backendDataSource = new JDBCBackendDataSource(dataSources);
        metaData = shardingMetaData;
    }
    
    /**
     * Renew data source configuration.
     *
     * @param dataSourceEvent data source event.
     */
    @Subscribe
    public void renew(final DataSourceChangedEvent dataSourceEvent) {
        if (!name.equals(dataSourceEvent.getSchemaName())) {
            return;
        }
        backendDataSource.close();
        dataSources.clear();
        dataSources.putAll(DataSourceConverter.getDataSourceParameterMap(dataSourceEvent.getDataSourceConfigurations()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
    }
}
