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

package org.apache.shardingsphere.test.e2e.env.container.atomic.adapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereJdbcEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereMultiProxyClusterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl.ShardingSphereProxyStandaloneContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.cluster.ClusterEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;

/**
 * Adapter container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdapterContainerFactory {
    
    /**
     * Create new instance of adapter container.
     *
     * @param mode adapter mode
     * @param adapter adapter type
     * @param databaseType database type
     * @param scenario scenario
     * @param containerConfig adaptor container configuration
     * @param storageContainer storage container
     * @param envType environment type
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static AdapterContainer newInstance(final AdapterMode mode, final AdapterType adapter, final DatabaseType databaseType, final String scenario,
                                               final AdaptorContainerConfiguration containerConfig, final StorageContainer storageContainer, final String envType) {
        switch (adapter) {
            case PROXY:
                if (ClusterEnvironment.Type.NATIVE.name().equalsIgnoreCase(envType)) {
                    return new ShardingSphereProxyEmbeddedContainer(databaseType, containerConfig);
                }
                return AdapterMode.CLUSTER == mode
                        ? new ShardingSphereProxyClusterContainer(databaseType, containerConfig)
                        : new ShardingSphereProxyStandaloneContainer(databaseType, containerConfig);
            case PROXY_RANDOM:
                return new ShardingSphereMultiProxyClusterContainer(databaseType, containerConfig);
            case JDBC:
                return new ShardingSphereJdbcEmbeddedContainer(storageContainer, new ScenarioCommonPath(scenario).getRuleConfigurationFile(databaseType));
            default:
                throw new RuntimeException(String.format("Unknown adapter `%s`.", adapter));
        }
    }
}
