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

package org.apache.shardingsphere.test.integration.container.compose;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.integration.container.compose.mode.ClusterContainerComposer;
import org.apache.shardingsphere.test.integration.container.compose.mode.StandaloneContainerComposer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Composed container registry.
 */
public final class ContainerComposerRegistry implements AutoCloseable {
    
    private final Map<String, ContainerComposer> containerComposers = new HashMap<>();
    
    /**
     * Get composed container.
     *
     * @param parameterizedArray parameterized array
     * @return composed container
     */
    public ContainerComposer getContainerComposer(final ParameterizedArray parameterizedArray) {
        String key = parameterizedArray.getKey();
        if (containerComposers.containsKey(key)) {
            return containerComposers.get(key);
        }
        synchronized (containerComposers) {
            if (!containerComposers.containsKey(key)) {
                containerComposers.put(key, createContainerComposer(parameterizedArray));
            }
            return containerComposers.get(key);
        }
    }
    
    private ContainerComposer createContainerComposer(final ParameterizedArray parameterizedArray) {
        return isClusterMode(parameterizedArray) ? new ClusterContainerComposer(parameterizedArray) : new StandaloneContainerComposer(parameterizedArray);
    }
    
    private boolean isClusterMode(final ParameterizedArray parameterizedArray) {
        // TODO cluster mode often throw exception sometimes, issue is #15517
        return "Cluster".equalsIgnoreCase(parameterizedArray.getMode()) && "proxy".equalsIgnoreCase(parameterizedArray.getAdapter());
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
        if (targetDataSource instanceof AutoCloseable) {
            ((AutoCloseable) targetDataSource).close();
        }
    }
    
    @SneakyThrows
    private void closeActualDataSourceMap(final Map<String, DataSource> actualDataSourceMap) {
        for (DataSource each : actualDataSourceMap.values()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    private void closeContainer(final ContainerComposer containerComposer) {
        containerComposer.close();
    }
}
