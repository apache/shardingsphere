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

package org.apache.shardingsphere.test.e2e.container.compose;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.container.compose.mode.ClusterContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.mode.StandaloneContainerComposer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;

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
     * @param testParam test parameter
     * @return composed container
     */
    public ContainerComposer getContainerComposer(final E2ETestParameter testParam) {
        String key = testParam.getKey();
        if (containerComposers.containsKey(key)) {
            return containerComposers.get(key);
        }
        synchronized (containerComposers) {
            if (!containerComposers.containsKey(key)) {
                containerComposers.put(key, createContainerComposer(testParam));
            }
            return containerComposers.get(key);
        }
    }
    
    private ContainerComposer createContainerComposer(final E2ETestParameter testParam) {
        return isClusterMode(testParam) ? new ClusterContainerComposer(testParam) : new StandaloneContainerComposer(testParam);
    }
    
    private boolean isClusterMode(final E2ETestParameter testParam) {
        // TODO cluster mode often throw exception sometimes, issue is #15517
        return AdapterMode.CLUSTER.getValue().equalsIgnoreCase(testParam.getMode()) && AdapterType.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter());
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
    
    @SneakyThrows
    private void closeTargetDataSource(final DataSource targetDataSource) {
        Preconditions.checkState(targetDataSource instanceof AutoCloseable, "target data source is not implement AutoCloseable");
        ((AutoCloseable) targetDataSource).close();
    }
    
    @SneakyThrows
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
