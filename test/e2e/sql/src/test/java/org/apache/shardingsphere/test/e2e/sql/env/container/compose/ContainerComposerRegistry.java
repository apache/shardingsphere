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

package org.apache.shardingsphere.test.e2e.sql.env.container.compose;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.mode.ClusterContainerComposer;
import org.apache.shardingsphere.test.e2e.sql.env.container.compose.mode.StandaloneContainerComposer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Composed container registry.
 */
public final class ContainerComposerRegistry implements AutoCloseable {
    
    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }
    
    private final Map<String, ContainerComposer> containerComposers = new HashMap<>(7, 1F);
    
    /**
     * Get container composer.
     *
     * @param key key
     * @param scenario scenario
     * @param databaseType databaseType
     * @param mode mode
     * @param adapter adapter
     * @return composed container
     */
    public ContainerComposer getContainerComposer(final String key, final String scenario, final DatabaseType databaseType, final Mode mode, final Adapter adapter) {
        if (containerComposers.containsKey(key)) {
            return containerComposers.get(key);
        }
        synchronized (containerComposers) {
            if (!containerComposers.containsKey(key)) {
                containerComposers.put(key, createContainerComposer(isClusterMode(mode, adapter), scenario, databaseType, adapter));
            }
            return containerComposers.get(key);
        }
    }
    
    private boolean isClusterMode(final Mode mode, final Adapter adapter) {
        return Mode.CLUSTER == mode && Adapter.PROXY == adapter;
    }
    
    private ContainerComposer createContainerComposer(final boolean clusterMode, final String scenario, final DatabaseType databaseType, final Adapter adapter) {
        return clusterMode ? new ClusterContainerComposer(scenario, databaseType, adapter) : new StandaloneContainerComposer(scenario, databaseType, adapter);
    }
    
    @Override
    public void close() {
        for (ContainerComposer each : containerComposers.values()) {
            closeTargetDataSource(each.getTargetDataSource());
            closeActualDataSourceMap(each.getActualDataSourceMap());
            closeContainer(each);
        }
        containerComposers.clear();
    }
    
    @SneakyThrows(Exception.class)
    private void closeTargetDataSource(final DataSource targetDataSource) {
        Preconditions.checkState(targetDataSource instanceof AutoCloseable, "target data source is not implement AutoCloseable");
        ((AutoCloseable) targetDataSource).close();
    }
    
    @SneakyThrows(Exception.class)
    private void closeActualDataSourceMap(final Map<String, DataSource> actualDataSourceMap) {
        for (DataSource each : actualDataSourceMap.values()) {
            Preconditions.checkState(each instanceof AutoCloseable, "actual data source is not implement AutoCloseable");
            ((AutoCloseable) each).close();
        }
    }
    
    private void closeContainer(final ContainerComposer containerComposer) {
        containerComposer.close();
    }
}
