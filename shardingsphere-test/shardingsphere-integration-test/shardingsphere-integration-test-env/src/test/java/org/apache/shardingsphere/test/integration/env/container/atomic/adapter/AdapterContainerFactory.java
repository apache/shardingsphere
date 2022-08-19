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

package org.apache.shardingsphere.test.integration.env.container.atomic.adapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereProxyStandaloneContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainer;

/**
 * Adapter container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdapterContainerFactory {
    
    /**
     * Create new instance of adapter container.
     *
     * @param mode mode
     * @param adapter adapter
     * @param databaseType database type
     * @param storageContainer storage container
     * @param scenario scenario
     * @param containerConfig adaptor container configuration
     * @return created instance
     */
    public static AdapterContainer newInstance(final String mode, final String adapter, final DatabaseType databaseType,
                                               final StorageContainer storageContainer, final String scenario, final AdaptorContainerConfiguration containerConfig) {
        switch (adapter) {
            case "proxy":
                return "Cluster".equalsIgnoreCase(mode)
                        ? new ShardingSphereProxyClusterContainer(databaseType, containerConfig)
                        : new ShardingSphereProxyStandaloneContainer(databaseType, containerConfig);
            case "jdbc":
                return new ShardingSphereJDBCContainer(storageContainer, scenario);
            default:
                throw new RuntimeException(String.format("Adapter [%s] is unknown.", adapter));
        }
    }
}
