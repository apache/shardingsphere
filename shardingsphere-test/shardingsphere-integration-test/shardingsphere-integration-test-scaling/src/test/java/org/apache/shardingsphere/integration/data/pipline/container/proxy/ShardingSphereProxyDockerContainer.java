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

package org.apache.shardingsphere.integration.data.pipline.container.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * ShardingSphere proxy container.
 */
@Slf4j
public final class ShardingSphereProxyDockerContainer extends DockerITContainer {
    
    private final DatabaseType databaseType;
    
    public ShardingSphereProxyDockerContainer(final DatabaseType databaseType) {
        super("Scaling-Proxy", "apache/shardingsphere-proxy-test");
        this.databaseType = databaseType;
    }
    
    @Override
    protected void configure() {
        mapConfigurationFiles();
        withExposedPorts(3307);
        setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(3307), ""), "root", "root")));
    }
    
    private void mapConfigurationFiles() {
        withClasspathResourceMapping("/env/conf/", "/opt/shardingsphere-proxy/conf", BindMode.READ_ONLY);
    }
    
    @Slf4j
    @RequiredArgsConstructor
    private static class JDBCConnectionWaitStrategy extends AbstractWaitStrategy {
        
        private final Callable<Connection> connectionSupplier;
        
        @Override
        protected void waitUntilReady() {
            Unreliables.retryUntilSuccess((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
                getRateLimiter().doWhenReady(() -> {
                    try (Connection unused = connectionSupplier.call()) {
                        log.info("Container ready");
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        throw new RuntimeException("Not Ready yet.", ex);
                    }
                });
                return true;
            });
        }
    }
}
