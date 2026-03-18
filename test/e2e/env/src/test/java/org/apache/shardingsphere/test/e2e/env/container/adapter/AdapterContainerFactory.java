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

package org.apache.shardingsphere.test.e2e.env.container.adapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereJdbcEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyDockerContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioCommonPath;

/**
 * Adapter container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdapterContainerFactory {
    
    /**
     * Create new instance of adapter container.
     *
     * @param adapter adapter
     * @param databaseType database type
     * @param scenario scenario
     * @param containerConfig adaptor container configuration
     * @param storageContainer storage container
     * @param type environment type
     * @return created instance
     * @throws RuntimeException runtime exception
     */
    public static AdapterContainer newInstance(final Adapter adapter, final DatabaseType databaseType, final String scenario,
                                               final AdaptorContainerConfiguration containerConfig, final StorageContainer storageContainer, final Type type) {
        switch (adapter) {
            case PROXY:
                return Type.NATIVE == type ? new ShardingSphereProxyEmbeddedContainer(databaseType, containerConfig) : new ShardingSphereProxyDockerContainer(databaseType, containerConfig);
            case JDBC:
                return new ShardingSphereJdbcEmbeddedContainer(storageContainer, new ScenarioCommonPath(scenario).getRuleConfigurationFile(databaseType));
            default:
                throw new RuntimeException(String.format("Unknown adapter `%s`.", adapter));
        }
    }
}
