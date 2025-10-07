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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.docker;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyDockerContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.PipelineBaseContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.config.proxy.PipelineProxyContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.ProxyDatabaseTypeUtils;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Pipeline Composed container.
 */
public final class PipelineDockerContainerComposer extends PipelineBaseContainerComposer {
    
    private final DatabaseType databaseType;
    
    private ShardingSphereProxyDockerContainer proxyContainer;
    
    @Getter
    private final List<DockerStorageContainer> storageContainers = new LinkedList<>();
    
    public PipelineDockerContainerComposer(final DatabaseType databaseType, final String databaseContainerImage, final int storageContainerCount) {
        this.databaseType = databaseType;
        ShardingSpherePreconditions.checkState(storageContainerCount >= 1, () -> new InvalidParameterException("storageContainerCount must >= 1"));
        GovernanceContainer governanceContainer = getContainers().registerContainer(new GovernanceContainer(TypedSPILoader.getService(GovernanceContainerOption.class, "ZooKeeper")));
        for (int i = 0; i < storageContainerCount; i++) {
            DockerStorageContainer storageContainer = getContainers().registerContainer(
                    new DockerStorageContainer(databaseContainerImage, DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType), null));
            storageContainer.setNetworkAliases(Collections.singletonList(String.join(".", databaseType.getType().toLowerCase() + "_" + i, "host")));
            storageContainers.add(storageContainer);
        }
        AdaptorContainerConfiguration containerConfig = PipelineProxyContainerConfigurationFactory.newInstance(databaseType);
        DatabaseType proxyDatabaseType = ProxyDatabaseTypeUtils.getProxyDatabaseType(databaseType);
        if (Type.NATIVE == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            ShardingSphereProxyEmbeddedContainer proxyContainer = new ShardingSphereProxyEmbeddedContainer(proxyDatabaseType, containerConfig);
            for (DockerStorageContainer each : storageContainers) {
                proxyContainer.dependsOn(governanceContainer, each);
            }
            getContainers().registerContainer(proxyContainer);
        } else {
            ShardingSphereProxyDockerContainer proxyContainer = new ShardingSphereProxyDockerContainer(proxyDatabaseType, containerConfig);
            for (DockerStorageContainer each : storageContainers) {
                proxyContainer.dependsOn(governanceContainer, each);
            }
            this.proxyContainer = getContainers().registerContainer(proxyContainer);
        }
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        String host;
        int port;
        if (Type.NATIVE == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            host = "127.0.0.1";
            port = 3307;
        } else {
            host = proxyContainer.getHost();
            port = proxyContainer.getFirstMappedPort();
        }
        return DatabaseTypedSPILoader.getService(StorageContainerOption.class, ProxyDatabaseTypeUtils.getProxyDatabaseType(databaseType)).getConnectOption().getURL(host, port, databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return proxyContainer.getMappedPort(33071);
    }
    
    @Override
    public void cleanUpDatabase(final String databaseName) {
    }
}
