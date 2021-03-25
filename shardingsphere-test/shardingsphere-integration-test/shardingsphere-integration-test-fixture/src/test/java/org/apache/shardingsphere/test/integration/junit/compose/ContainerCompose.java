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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.annotation.ContainerInitializer;
import org.apache.shardingsphere.test.integration.junit.annotation.OnContainer;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITInject;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.junit.container.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.logging.ContainerLogs;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseBeanContext;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseDescription;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
            ShardingSphereContainer result = createContainer(metadata);
            if (Objects.isNull(result)) {
                log.warn("container {} is not activated.", metadata.name());
                return null;
            }
            result.setDockerName(metadata.name());
            String hostName = metadata.hostName();
            if (Strings.isNullOrEmpty(hostName)) {
                hostName = metadata.name();
            }
            result.withNetworkAliases(hostName);
            result.setNetwork(network);
            result.withLogConsumer(ContainerLogs.newConsumer(clusterName + "_" + metadata.name()));
            field.getField().setAccessible(true);
            field.getField().set(instance, result);
            beanContext.registerBeanByName(metadata.name(), result);
            log.info("container {} is activated.", metadata.name());
            return result;
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
        Collection<Field> fields = new LinkedList<>();
        for (Class<?> klass = container.getClass(); Objects.nonNull(klass); klass = klass.getSuperclass()) {
            fields.addAll(Arrays.asList(klass.getDeclaredFields()));
        }
        fields.stream().filter(each -> each.isAnnotationPresent(ShardingSphereITInject.class)).forEach(each -> setFieldValue(each, container));
    }
    
    private void setFieldValue(final Field field, final ShardingSphereContainer container) {
        Class<?> type = field.getType();
        field.setAccessible(true);
        try {
            if (type.isPrimitive() || String.class == type) {
                field.set(container, beanContext.getBeanByName(field.getName()));
            } else {
                field.set(container, beanContext.getBean(type));
            }
        } catch (final IllegalAccessException ex) {
            log.error("Failed to auto inject {}.{}.", container.getContainerName(), field.getName());
        }
    }
    
    /**
     * Create the initializer and execute.
     */
    @SneakyThrows
    public void createInitializerAndExecute() {
        testClass.getAnnotatedMethods(ContainerInitializer.class).forEach(this::invokeExplosively);
    }
    
    private void invokeExplosively(final FrameworkMethod frameworkMethod) {
        try {
            if (frameworkMethod.isStatic()) {
                frameworkMethod.getMethod().setAccessible(true);
                frameworkMethod.invokeExplosively(null);
            } else {
                frameworkMethod.getMethod().setAccessible(true);
                frameworkMethod.invokeExplosively(instance);
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            throw new RuntimeException(ex);
        }
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
        log.info("Any container is startup.");
    }
    
    @Override
    public void close() {
        containers.forEach(Startable::close);
    }
}
