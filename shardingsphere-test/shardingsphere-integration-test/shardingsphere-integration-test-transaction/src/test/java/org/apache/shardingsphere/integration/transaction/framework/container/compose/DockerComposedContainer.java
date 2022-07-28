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

package org.apache.shardingsphere.integration.transaction.framework.container.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.factory.DatabaseContainerFactory;
import org.apache.shardingsphere.integration.transaction.framework.container.database.DatabaseContainer;
import org.apache.shardingsphere.integration.transaction.framework.container.jdbc.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.integration.transaction.framework.container.proxy.ShardingSphereProxyDockerContainer;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.governance.impl.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;

import java.util.Objects;

/**
 * Composed container, include governance container and database container.
 */
@Slf4j
@Getter
public final class DockerComposedContainer extends BaseComposedContainer {
    
    private final DatabaseType databaseType;
    
    private final GovernanceContainer governanceContainer;
    
    private final ShardingSphereProxyDockerContainer proxyContainer;
    
    private final ShardingSphereJDBCContainer jdbcContainer;
    
    private final DatabaseContainer databaseContainer;
    
    public DockerComposedContainer(final TransactionParameterized parameterized) {
        this.databaseType = parameterized.getDatabaseType();
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer());
        databaseContainer = getContainers().registerContainer(DatabaseContainerFactory.newInstance(this.databaseType, parameterized.getDockerImageName()));
        if (TransactionTestConstants.PROXY.equalsIgnoreCase(parameterized.getAdapter())) {
            this.jdbcContainer = null;
            ShardingSphereProxyDockerContainer proxyContainer = new ShardingSphereProxyDockerContainer(this.databaseType);
            proxyContainer.dependsOn(governanceContainer, databaseContainer);
            this.proxyContainer = getContainers().registerContainer(proxyContainer);
        } else {
            this.proxyContainer = null;
            ShardingSphereJDBCContainer jdbcContainer = new ShardingSphereJDBCContainer(databaseContainer,
                    Objects.requireNonNull(ShardingSphereJDBCContainer.class.getClassLoader().getResource(getShardingSphereConfigResource(parameterized))).getFile());
            this.jdbcContainer = getContainers().registerContainer(jdbcContainer);
        }
    }
    
    private String getShardingSphereConfigResource(final TransactionParameterized parameterized) {
        String result = String.format("env/%s/%s/config-sharding-%s.yaml", parameterized.getAdapter().toLowerCase(),
                parameterized.getDatabaseType().getType().toLowerCase(), parameterized.getTransactionType().toString().toLowerCase());
        log.info("Transaction IT tests use the configuration file: {}", result);
        return result;
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
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
    
}
