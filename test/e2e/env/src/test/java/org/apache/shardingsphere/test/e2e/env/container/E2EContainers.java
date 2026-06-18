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

package org.apache.shardingsphere.test.e2e.env.container;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.env.container.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.NativeStorageContainer;
import org.awaitility.Awaitility;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * E2E containers.
 */
@RequiredArgsConstructor
@Slf4j
public final class E2EContainers implements Startable {
    
    private final String scenario;
    
    @Getter
    private final Network network = Network.newNetwork();
    
    private final Collection<EmbeddedE2EContainer> embeddedContainers = new LinkedList<>();
    
    private final Collection<DockerE2EContainer> dockerContainers = new LinkedList<>();
    
    private volatile boolean started;
    
    /**
     * Register container.
     *
     * @param container container to be registered
     * @param <T> type of container
     * @return registered container
     */
    public <T extends E2EContainer> T registerContainer(final T container) {
        if (container instanceof ComboE2EContainer) {
            ((ComboE2EContainer) container).getContainers().forEach(this::registerContainer);
        } else if (container instanceof EmbeddedE2EContainer) {
            embeddedContainers.add((EmbeddedE2EContainer) container);
        } else if (container instanceof NativeStorageContainer) {
            String networkAlias = getNetworkAlias(container);
            ((NativeStorageContainer) container).setNetworkAliases(Collections.singletonList(networkAlias));
        } else {
            DockerE2EContainer dockerContainer = (DockerE2EContainer) container;
            dockerContainer.setNetwork(network);
            String networkAlias = getNetworkAlias(container);
            dockerContainer.setNetworkAliases(Collections.singletonList(networkAlias));
            String loggerName = Lists.newArrayList(scenario, dockerContainer.getName()).stream().filter(Objects::nonNull).collect(Collectors.joining(":"));
            dockerContainer.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(loggerName), false));
            dockerContainers.add(dockerContainer);
        }
        return container;
    }
    
    private <T extends E2EContainer> String getNetworkAlias(final T container) {
        return container instanceof GovernanceContainer || Strings.isNullOrEmpty(scenario)
                ? String.join(".", container.getAbbreviation(), "host")
                : String.join(".", container.getAbbreviation(), scenario, "host");
    }
    
    @Override
    public void start() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    embeddedContainers.forEach(EmbeddedE2EContainer::start);
                    dockerContainers.stream().filter(each -> !each.isCreated()).forEach(this::start);
                    waitUntilReady();
                    started = true;
                }
            }
        }
    }
    
    private void start(final DockerE2EContainer container) {
        log.info("Starting container {}...", container.getName());
        container.start();
    }
    
    private void waitUntilReady() {
        dockerContainers.stream()
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
                        Awaitility.await().pollDelay(500L, TimeUnit.MILLISECONDS).until(() -> true);
                    }
                });
    }
    
    @Override
    public void stop() {
        embeddedContainers.forEach(Startable::close);
        dockerContainers.forEach(Startable::close);
        network.close();
    }
}
