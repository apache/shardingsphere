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

package org.apache.shardingsphere.test.integration.framework.compose;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.impl.PostgreSQLContainer;
import org.apache.shardingsphere.test.integration.framework.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Composed container.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class ComposedContainer extends ExternalResource implements Closeable {
    
    @Getter(AccessLevel.NONE)
    private final String name;
    
    private final ParameterizedArray parameterizedArray;
    
    private final Network network = Network.newNetwork();
    
    private final Collection<ShardingSphereContainer> containers = new LinkedList<>();
    
    private volatile boolean started;
    
    private volatile boolean executed;
    
    protected ShardingSphereStorageContainer createStorageContainer() {
        Supplier<ShardingSphereStorageContainer> supplier = () -> {
            switch (parameterizedArray.getDatabaseType().getName()) {
                case "MySQL":
                    return new MySQLContainer(parameterizedArray);
                case "H2":
                    return new H2Container(parameterizedArray);
                case "PostgreSQL" :
                    return new PostgreSQLContainer(parameterizedArray);
                default:
                    throw new RuntimeException("Unknown storage type " + parameterizedArray.getDatabaseType());
            }
        };
        return createContainer(supplier, parameterizedArray.getDatabaseType().getName().toLowerCase() + "." + parameterizedArray.getScenario() + ".host");
    }
    
    protected ShardingSphereAdapterContainer createAdapterContainer() {
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
        return createContainer(supplier, "adapter");
    }
    
    protected final <T extends ShardingSphereContainer> T createContainer(final Supplier<T> supplier, final String hostname) {
        T result = supplier.get();
        containers.add(result);
        result.setNetwork(network);
        result.setNetworkAliases(Collections.singletonList(hostname));
        result.withLogConsumer(ContainerLogs.newConsumer(String.join("-", name, result.getName())));
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
    
    /**
     * Get adapter container.
     *
     * @return ShardingSphere adapter container
     */
    public abstract ShardingSphereAdapterContainer getAdapterContainer();
    
    /**
     * Get storage container.
     *
     * @return ShardingSphere storage container
     */
    public abstract ShardingSphereStorageContainer getStorageContainer();

    /**
     * Get all target data sources.
     *
     * @return datasource map
     */
    public Map<String, DataSource> getDataSourceMap() {
        return Collections.singletonMap("adapterForWriter", getAdapterContainer().getDataSource(null));
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
    
    /**
     * Execution initializer one time after container started.
     *
     * @param consumer initializer
     */
    public final void executeOnStarted(final Consumer<ComposedContainer> consumer) {
        if (!executed) {
            synchronized (this) {
                if (!executed) {
                    consumer.accept(this);
                    executed = true;
                }
            }
        }
    }
    
    @Override
    public void close() {
        containers.forEach(Startable::close);
    }
}
