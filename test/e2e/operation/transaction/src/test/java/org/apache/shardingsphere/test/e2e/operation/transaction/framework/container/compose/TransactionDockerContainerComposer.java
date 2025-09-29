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

package org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.compose;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereJdbcEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.impl.ShardingSphereProxyEmbeddedContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.governance.option.GovernanceContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.NativeStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.config.TransactionProxyContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.param.TransactionTestParameter;

import java.net.URL;
import java.util.Objects;

/**
 * transaction composed container.
 */
@Getter
@Slf4j
public final class TransactionDockerContainerComposer extends TransactionBaseContainerComposer {
    
    private final AdapterContainer proxyContainer;
    
    private final ShardingSphereJdbcEmbeddedContainer jdbcContainer;
    
    private final StorageContainer storageContainer;
    
    public TransactionDockerContainerComposer(final TransactionTestParameter testParam) {
        super(testParam.getScenario());
        DatabaseType databaseType = testParam.getDatabaseType();
        GovernanceContainer governanceContainer = getContainers().registerContainer(new GovernanceContainer(TypedSPILoader.getService(GovernanceContainerOption.class, "ZooKeeper")));
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        if (Type.DOCKER == type) {
            storageContainer = getContainers().registerContainer(
                    new DockerStorageContainer(testParam.getDatabaseContainerImage(), DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType), testParam.getScenario()));
        } else {
            storageContainer = getContainers().registerContainer(new NativeStorageContainer(databaseType, testParam.getScenario()));
        }
        if (Adapter.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter())) {
            jdbcContainer = null;
            AdaptorContainerConfiguration containerConfig = TransactionProxyContainerConfigurationFactory.newInstance(testParam.getScenario(), databaseType, testParam.getPortBindings());
            proxyContainer = AdapterContainerFactory.newInstance(Adapter.PROXY, databaseType, testParam.getScenario(), containerConfig, storageContainer, type);
            if (proxyContainer instanceof DockerE2EContainer) {
                ((DockerE2EContainer) proxyContainer).dependsOn(governanceContainer, storageContainer);
            }
            if (proxyContainer instanceof ShardingSphereProxyEmbeddedContainer) {
                ((ShardingSphereProxyEmbeddedContainer) proxyContainer).dependsOn(governanceContainer, storageContainer);
            }
            getContainers().registerContainer(proxyContainer);
        } else {
            proxyContainer = null;
            ShardingSphereJdbcEmbeddedContainer jdbcContainer = new ShardingSphereJdbcEmbeddedContainer(
                    storageContainer, Objects.requireNonNull(getShardingSphereConfigResource(testParam)).getFile());
            this.jdbcContainer = getContainers().registerContainer(jdbcContainer);
        }
    }
    
    private URL getShardingSphereConfigResource(final TransactionTestParameter testParam) {
        URL result = Thread.currentThread().getContextClassLoader().getResource(getScenarioResource(testParam));
        if (null != result) {
            return result;
        }
        result = Thread.currentThread().getContextClassLoader().getResource(getDefaultResource(testParam));
        log.info("Transaction IT tests use the configuration file: {}", result);
        return result;
    }
    
    private String getDefaultResource(final TransactionTestParameter testParam) {
        return String.format("env/%s/%s/database-sharding-%s%s.yaml", testParam.getAdapter().toLowerCase(),
                testParam.getDatabaseType().getType().toLowerCase(), testParam.getTransactionTypes().get(0).toString().toLowerCase(),
                getTransactionProvider(testParam.getProviders().get(0)));
    }
    
    private String getScenarioResource(final TransactionTestParameter testParam) {
        return String.format("env/scenario/%s/%s/conf/%s/database-%s-%s%s.yaml", testParam.getScenario(), testParam.getAdapter().toLowerCase(),
                testParam.getDatabaseType().getType().toLowerCase(), testParam.getScenario(), testParam.getTransactionTypes().get(0).toString().toLowerCase(),
                getTransactionProvider(testParam.getProviders().get(0)));
    }
    
    private String getTransactionProvider(final String providerType) {
        return Strings.isNullOrEmpty(providerType) ? "" : "-" + providerType.toLowerCase();
    }
    
    @Override
    public void stop() {
        super.stop();
        if (null != proxyContainer) {
            proxyContainer.stop();
        }
        if (null != jdbcContainer) {
            jdbcContainer.stop();
        }
    }
}
