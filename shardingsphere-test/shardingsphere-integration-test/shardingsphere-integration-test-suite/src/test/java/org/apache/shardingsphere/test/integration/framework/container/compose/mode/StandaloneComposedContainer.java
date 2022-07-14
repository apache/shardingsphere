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

package org.apache.shardingsphere.test.integration.framework.container.compose.mode;

import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.ITContainers;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.util.NetworkAliasUtil;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Standalone composed container.
 */
public final class StandaloneComposedContainer implements ComposedContainer {
    
    private final ITContainers containers;
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public StandaloneComposedContainer(final ParameterizedArray parameterizedArray) {
        String scenario = parameterizedArray.getScenario();
        containers = new ITContainers(scenario);
        storageContainer = containers.registerContainer(StorageContainerFactory.newInstance(parameterizedArray.getDatabaseType(), scenario),
                NetworkAliasUtil.getNetworkAliasWithScenario(parameterizedArray.getDatabaseType().getType(), scenario));
        adapterContainer = containers.registerContainer(AdapterContainerFactory.newInstance(parameterizedArray.getMode(), parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType(),
                storageContainer, scenario),
                NetworkAliasUtil.getNetworkAliasWithScenario(parameterizedArray.getAdapter(), scenario));
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
