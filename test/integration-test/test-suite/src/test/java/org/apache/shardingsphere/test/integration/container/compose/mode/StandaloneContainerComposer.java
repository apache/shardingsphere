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

package org.apache.shardingsphere.test.integration.container.compose.mode;

import org.apache.shardingsphere.test.integration.container.compose.ContainerComposer;
import org.apache.shardingsphere.test.integration.container.config.ProxyStandaloneContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.ITContainers;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Standalone composed container.
 */
public final class StandaloneContainerComposer implements ContainerComposer {
    
    private final ITContainers containers;
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public StandaloneContainerComposer(final ParameterizedArray parameterizedArray) {
        String scenario = parameterizedArray.getScenario();
        containers = new ITContainers(scenario);
        // TODO add more version of databases
        storageContainer = containers.registerContainer(StorageContainerFactory.newInstance(parameterizedArray.getDatabaseType(), "", scenario,
                StorageContainerConfigurationFactory.newInstance(parameterizedArray.getDatabaseType())));
        adapterContainer = containers.registerContainer(AdapterContainerFactory.newInstance(parameterizedArray.getMode(), parameterizedArray.getAdapter(),
                parameterizedArray.getDatabaseType(), storageContainer, scenario, ProxyStandaloneContainerConfigurationFactory.newInstance(scenario, parameterizedArray.getDatabaseType())));
        if (adapterContainer instanceof DockerITContainer) {
            ((DockerITContainer) adapterContainer).dependsOn(storageContainer);
        }
    }
    
    @Override
    public void start() {
        containers.start();
    }
    
    @Override
    public DataSource getTargetDataSource() {
        return adapterContainer.getTargetDataSource(null);
    }
    
    @Override
    public Map<String, DataSource> getActualDataSourceMap() {
        return storageContainer.getActualDataSourceMap();
    }
    
    @Override
    public Map<String, DataSource> getExpectedDataSourceMap() {
        return storageContainer.getExpectedDataSourceMap();
    }
    
    @Override
    public void stop() {
        containers.stop();
    }
}
