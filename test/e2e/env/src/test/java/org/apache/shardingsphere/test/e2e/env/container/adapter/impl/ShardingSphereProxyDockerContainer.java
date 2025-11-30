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

package org.apache.shardingsphere.test.e2e.env.container.adapter.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.util.JdbcConnectCheckingWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy docker container.
 */
public final class ShardingSphereProxyDockerContainer extends DockerE2EContainer implements AdapterContainer {
    
    private final AdaptorContainerConfiguration config;
    
    private final StorageContainerConnectOption storageContainerConnectOption;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereProxyDockerContainer(final DatabaseType databaseType, final AdaptorContainerConfiguration config) {
        super(ProxyContainerConstants.PROXY_CONTAINER_NAME_PREFIX, config.getAdapterContainerImage());
        this.config = config;
        storageContainerConnectOption = DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType).getConnectOption();
    }
    
    @Override
    protected void configure() {
        if (!Strings.isNullOrEmpty(config.getContainerCommand())) {
            setCommand(config.getContainerCommand());
        }
        withExposedPorts(3307, 33071, 3308);
        if (!config.getPortBindings().isEmpty()) {
            setPortBindings(config.getPortBindings());
        }
        addEnv("TZ", "UTC");
        mapResources(config.getMountedResources());
        setWaitStrategy(new JdbcConnectCheckingWaitStrategy(() -> DriverManager.getConnection(
                storageContainerConnectOption.getURL(getHost(), getMappedPort(3307), config.getProxyDataSourceName()), ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD)));
        withStartupTimeout(Duration.of(120L, ChronoUnit.SECONDS));
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            targetDataSourceProvider.set(StorageContainerUtils.generateDataSource(
                    storageContainerConnectOption.getURL(getHost(), getMappedPort(3307), config.getProxyDataSourceName()), ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD, 2));
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
}
