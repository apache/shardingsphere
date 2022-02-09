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

package org.apache.shardingsphere.test.integration.framework.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.logging.ContainerLogs;
import org.testcontainers.containers.Network;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * ShardingSphere containers.
 */
@RequiredArgsConstructor
public final class ShardingSphereContainers implements AutoCloseable {
    
    private final String testSuiteName;
    
    private final Network network = Network.newNetwork();
    
    private final Collection<ShardingSphereContainer> containers = new LinkedList<>();
    
    @Getter
    private volatile boolean started;
    
    /**
     * Register container.
     * 
     * @param container container to be registered
     * @param hostname container hostname
     * @param <T> type of ShardingSphere container
     * @return registered container
     */
    public <T extends ShardingSphereContainer> T registerContainer(final T container, final String hostname) {
        container.setNetwork(network);
        container.setNetworkAliases(Collections.singletonList(hostname));
        container.withLogConsumer(ContainerLogs.newConsumer(String.join("-", testSuiteName, container.getName())));
        containers.add(container);
        return container;
    }
    
    /**
     * Start containers.
     */
    public void start() {
        containers.stream().filter(each -> !each.isCreated()).forEach(ShardingSphereContainer::start);
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
    public void close() {
        containers.forEach(ShardingSphereContainer::close);
    }
}
