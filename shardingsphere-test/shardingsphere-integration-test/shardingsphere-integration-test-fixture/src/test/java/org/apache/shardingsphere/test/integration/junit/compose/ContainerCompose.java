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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.annotation.ContainerInitializer;
import org.apache.shardingsphere.test.integration.junit.annotation.OnContainer;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITInject;
import org.apache.shardingsphere.test.integration.junit.container.H2Container;
import org.apache.shardingsphere.test.integration.junit.container.MySQLContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseBeanContext;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseDescription;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Container compose.
 */
@RequiredArgsConstructor
@Slf4j
public final class ContainerCompose implements Closeable {
    
    private final Network network = Network.newNetwork();
    
    private final String clusterName;
    
    private final TestClass testClass;
    
    private final TestCaseDescription description;
    
    private final TestCaseBeanContext beanContext;
    
    private ImmutableList<ShardingSphereContainer> containers;
    
    @Setter
    private Object instance;
    
    /**
     * Create container and then autowired to test-case.
     */
    public void createContainers() {
        ImmutableList.Builder<ShardingSphereContainer> builder = new ImmutableList.Builder<>();
        testClass.getAnnotatedFields(OnContainer.class).stream()
                .map(this::createContainer)
                .filter(Objects::nonNull)
                .peek(this::inject)
                .forEach(builder::add);
        containers = builder.build();
    }
    
    @SneakyThrows
    private ShardingSphereContainer createContainer(final FrameworkField field) {
        OnContainer metadata = field.getAnnotation(OnContainer.class);
        try {
            ShardingSphereContainer container = createContainer(metadata);
            if (Objects.isNull(container)) {
                log.warn("container {} is not activated.", metadata.name());
                return null;
            }
            container.setDockerName(metadata.name());
            String hostName = metadata.hostName();
            if (Strings.isNullOrEmpty(hostName)) {
                hostName = metadata.name();
            }
            container.withNetworkAliases(hostName);
            container.setNetwork(network);
            container.withLogConsumer(ContainerLogs.newConsumer(clusterName + "_" + metadata.name()));
            field.getField().setAccessible(true);
            field.getField().set(instance, container);
            beanContext.registerBeanByName(metadata.name(), container);
            log.info("container {} is activated.", metadata.name());
            return container;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Failed to instantiate container {}.", metadata.name(), ex);
        }
        return null;
    }
    
    @SneakyThrows
    private ShardingSphereContainer createContainer(final OnContainer metadata) {
        switch (metadata.type()) {
            case ADAPTER:
                return createAdapterContainer();
            case STORAGE:
                return createStorageContainer();
            case COORDINATOR:
                throw new UnsupportedOperationException("");
            default:
                return null;
        }
    }
    
    private ShardingSphereAdapterContainer createAdapterContainer() {
        switch (description.getAdapter()) {
            case "proxy":
                return new ShardingSphereProxyContainer();
            case "jdbc":
                return new ShardingSphereJDBCContainer();
            default:
                throw new RuntimeException("Adapter[" + description.getAdapter() + "] is unknown.");
        }
    }
    
    private ShardingSphereStorageContainer createStorageContainer() {
        switch (description.getStorageType()) {
            case MySQL:
                return new MySQLContainer();
            case H2:
                return new H2Container();
            default:
                throw new RuntimeException("Unknown storage type " + description.getStorageType());
        }
    }
    
    private void inject(final ShardingSphereContainer container) {
        List<Field> fields = Lists.newArrayList();
        for (Class<?> klass = container.getClass(); Objects.nonNull(klass); klass = klass.getSuperclass()) {
            fields.addAll(Arrays.asList(klass.getDeclaredFields()));
        }
        fields.stream()
                .filter(e -> e.isAnnotationPresent(ShardingSphereITInject.class))
                .forEach(e -> {
                    Class<?> type = e.getType();
                    e.setAccessible(true);
                    try {
                        if (type.isPrimitive() || String.class == type) {
                            e.set(container, beanContext.getBeanByName(e.getName()));
                        } else {
                            e.set(container, beanContext.getBean(type));
                        }
                    } catch (final IllegalAccessException illegalAccessException) {
                        log.error("Failed to auto inject {}.{}.", container.getContainerName(), e.getName());
                    }
                });
    }
    
    /**
     * Create the initializer and execute.
     */
    @SneakyThrows
    public void createInitializerAndExecute() {
        testClass.getAnnotatedMethods(ContainerInitializer.class).forEach(method -> {
            try {
                if (method.isStatic()) {
                    method.getMethod().setAccessible(true);
                    method.invokeExplosively(null);
                } else {
                    method.getMethod().setAccessible(true);
                    method.invokeExplosively(instance);
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable throwable) {
                // CHECKSTYLE:ON
                throwable.printStackTrace();
            }
        });
    }
    
    /**
     * Startup.
     */
    public void start() {
        containers.stream().filter(c -> !c.isCreated()).forEach(GenericContainer::start);
    }
    
    /**
     * Wait until all containers ready.
     */
    public void waitUntilReady() {
        containers.stream()
                .filter(c -> {
                    try {
                        return !c.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        return false;
                    }
                })
                .forEach(c -> {
                    while (!(c.isRunning() && c.isHealthy())) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (final InterruptedException ignored) {
                        
                        }
                    }
                });
        log.info("Any container is startup.");
    }
    
    @Override
    public void close() {
        containers.forEach(Startable::close);
    }
}
