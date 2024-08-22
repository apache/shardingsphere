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

package org.apache.shardingsphere.test.e2e.env.container.atomic;

import com.alibaba.dcm.DnsCacheManipulator;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainer;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * IT containers.
 */
@Slf4j
public final class ITContainers implements Startable {
    
    private final String scenario;
    
    @Getter
    private final Network network = Network.newNetwork();
    
    private final Collection<EmbeddedITContainer> embeddedContainers = new LinkedList<>();
    
    private final Collection<DockerITContainer> dockerContainers = new LinkedList<>();
    
    private volatile boolean started;
    
    public ITContainers(final String scenario) {
        this.scenario = scenario;
        DnsCacheManipulator.setDnsCachePolicy(-1);
        System.setProperty("socksNonProxyHosts", "localhost|127.*|[::1]|0.0.0.0|[::0]|*.host");
    }
    
    /**
     * Register container.
     *
     * @param container container to be registered
     * @param <T> type of container
     * @return registered container
     */
    public <T extends ITContainer> T registerContainer(final T container) {
        if (container instanceof ComboITContainer) {
            ((ComboITContainer) container).getContainers().forEach(this::registerContainer);
        } else if (container instanceof EmbeddedITContainer) {
            embeddedContainers.add((EmbeddedITContainer) container);
        } else {
            DockerITContainer dockerContainer = (DockerITContainer) container;
            dockerContainer.setNetwork(network);
            String networkAlias = getNetworkAlias(container);
            dockerContainer.setNetworkAliases(Collections.singletonList(networkAlias));
            String loggerName = String.join(":", scenario, dockerContainer.getName());
            dockerContainer.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(loggerName), false));
            dockerContainers.add(dockerContainer);
        }
        return container;
    }
    
    private <T extends ITContainer> String getNetworkAlias(final T container) {
        return container instanceof GovernanceContainer || Strings.isNullOrEmpty(scenario)
                ? String.join(".", container.getAbbreviation(), "host")
                : String.join(".", container.getAbbreviation(), scenario, "host");
    }
    
    @Override
    public void start() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    embeddedContainers.forEach(EmbeddedITContainer::start);
                    dockerContainers.stream().filter(each -> !each.isCreated()).forEach(this::start);
                    waitUntilReady();
                    started = true;
                }
            }
        }
    }
    
    private void start(final DockerITContainer dockerITContainer) {
        log.info("Starting container {}...", dockerITContainer.getName());
        dockerITContainer.start();
        dockerITContainer.getNetworkAliases().forEach(each -> DnsCacheManipulator.setDnsCache(each,
                dockerITContainer.getContainerInfo().getNetworkSettings().getNetworks().values().stream().map(ContainerNetwork::getIpAddress).collect(Collectors.toList()).toArray(new String[0])));
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
        DnsCacheManipulator.clearDnsCache();
    }
}
