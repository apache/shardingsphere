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

package org.apache.shardingsphere.governance.core.registry.config.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceAddedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDroppedSQLNotificationEvent;
import org.apache.shardingsphere.infra.config.persist.service.impl.DataSourcePersistService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;

import java.util.Map;

/**
 * Data source registry subscriber.
 */
public final class DataSourceRegistrySubscriber {
    
    private final DataSourcePersistService persistService;
    
    public DataSourceRegistrySubscriber(final RegistryCenterRepository repository) {
        persistService = new DataSourcePersistService(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Update data source configurations for add.
     *
     * @param event data source added event
     */
    @Subscribe
    public void update(final DataSourceAddedSQLNotificationEvent event) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = persistService.load(event.getSchemaName());
        dataSourceConfigs.putAll(event.getDataSourceConfigurations());
        persistService.persist(event.getSchemaName(), dataSourceConfigs);
    }
    
    /**
     * Update data source configurations for drop.
     *
     * @param event data source dropped event
     */
    @Subscribe
    public void update(final DataSourceDroppedSQLNotificationEvent event) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = persistService.load(event.getSchemaName());
        for (String each : event.getDataSourceNames()) {
            dataSourceConfigs.remove(each);
        }
        persistService.persist(event.getSchemaName(), dataSourceConfigs);
    }
}
