/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.state.datasource;

import com.google.common.base.Optional;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeEvent;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeListener;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeType;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * Data source service.
 * 
 * @author caohao
 */
@Getter
public final class DataSourceService {
    
    private final String dataSourceNodePath;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ConfigurationService configurationService;
    
    public DataSourceService(final OrchestrationConfiguration config) {
        dataSourceNodePath = new DataSourceStateNode(config.getName()).getFullPath();
        regCenter = config.getRegistryCenter();
        configurationService = new ConfigurationService(config);
    }
    
    /**
     * Persist master-salve datasources node and add listener.
     *
     * @param masterSlaveDataSource master-slave datasource
     */
    public void persistDataSourcesNodeOnline(final MasterSlaveDataSource masterSlaveDataSource) {
        regCenter.persist(dataSourceNodePath, "");
        regCenter.addCacheData(dataSourceNodePath);
        addDataSourcesNodeListener(masterSlaveDataSource);
    }
    
    private void addDataSourcesNodeListener(final MasterSlaveDataSource masterSlaveDataSource) {
        regCenter.addRegistryChangeListener(dataSourceNodePath, new RegistryChangeListener() {
            @Override
            public void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception {
                Optional<RegistryChangeEvent.Payload> payload = registryChangeEvent.getPayload();
                if (payload.isPresent()) {
                    MasterSlaveRule masterSlaveRule = configurationService.getAvailableMasterSlaveRule();
                    if (RegistryChangeType.UPDATED == registryChangeEvent.getType()) {
                        String datasourceKey = payload.get().getKey();
                        String dataSourceName = datasourceKey.substring(datasourceKey.lastIndexOf("/") + 1);
                        masterSlaveRule.getSlaveDataSourceMap().remove(dataSourceName);
                    }
                    masterSlaveDataSource.renew(masterSlaveRule);
                }
            }
        });
    }
}
