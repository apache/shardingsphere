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

package org.apache.shardingsphere.test.integration.junit.compose;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.junit.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Container compose.
 */
public final class ContainerCompose extends ExternalResource implements Closeable {
    
    private final Network network = Network.newNetwork();
    
    private final String clusterName;
    
    private final ParameterizedArray parameterizedArray;
    
    private final List<ShardingSphereContainer> containers;
    
    @Getter
    private final ShardingSphereStorageContainer storageContainer;
    
    @Getter
    private final ShardingSphereAdapterContainer adapterContainer;
    
    private volatile boolean started;
    
    public ContainerCompose(final String clusterName, final ParameterizedArray parameterizedArray) {
        this.clusterName = clusterName;
        this.parameterizedArray = parameterizedArray;
        this.storageContainer = createStorageContainer();
        this.adapterContainer = createAdapterContainer();
        adapterContainer.dependsOn(storageContainer);
        this.containers = Arrays.asList(storageContainer, adapterContainer);
    }
    
    private ShardingSphereAdapterContainer createAdapterContainer() {
        Supplier<ShardingSphereAdapterContainer> supplier = () -> {
            switch (parameterizedArray.getAdapter()) {
                case "proxy":
                    return new ShardingSphereProxyContainer(parameterizedArray);
                case "jdbc":
                    return new ShardingSphereJDBCContainer(parameterizedArray);
                default:
                    throw new RuntimeException(String.format("Adapter[%s] is unknown.", parameterizedArray.getAdapter()));
                
            }
        };
        ShardingSphereAdapterContainer result = supplier.get();
        result.setNetwork(network);
        result.withLogConsumer(ContainerLogs.newConsumer(this.clusterName + "-adapter"));
        return result;
    }
    
    private ShardingSphereStorageContainer createStorageContainer() {
        Supplier<ShardingSphereStorageContainer> supplier = () -> {
            switch (parameterizedArray.getDatabaseType().getName()) {
                case "MySQL":
                    return new MySQLContainer(parameterizedArray);
                case "H2":
                    return new H2Container(parameterizedArray);
                default:
                    throw new RuntimeException("Unknown storage type " + parameterizedArray.getDatabaseType());
            }
        };
        ShardingSphereStorageContainer result = supplier.get();
        result.setNetwork(network);
        result.withLogConsumer(ContainerLogs.newConsumer(this.clusterName + "-storage"));
        result.setNetworkAliases(Collections.singletonList("mysql.db.host"));
        return result;
    }
    
    /**
     * Startup.
     */
    public void start() {
        containers.stream().filter(each -> !each.isCreated()).forEach(GenericContainer::start);
    }
    
    /**
     * Wait until all containers ready.
     */
    public void waitUntilReady() {
        containers.stream()
                .filter(each -> {
                    try {
                        return !each.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final RuntimeException ex) {
                        // CHECKSTYLE:ON
                        return false;
                    }
                })
                .forEach(each -> {
                    while (!(each.isRunning() && each.isHealthy())) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (final InterruptedException ignored) {
                        }
                    }
                });
        started = true;
    }
    
    @Override
    protected void before() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    start();
                    waitUntilReady();
                }
            }
        }
    }
    
    @Override
    public void close() {
        containers.forEach(Startable::close);
    }
}
