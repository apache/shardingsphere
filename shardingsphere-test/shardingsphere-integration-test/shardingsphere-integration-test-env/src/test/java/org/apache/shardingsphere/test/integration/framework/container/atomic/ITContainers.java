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

package org.apache.shardingsphere.test.integration.framework.container.atomic;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * IT containers.
 */
@RequiredArgsConstructor
public final class ITContainers implements Startable {
    
    private final String scenario;
    
    private final Network network = Network.newNetwork();
    
    private final Collection<EmbeddedITContainer> embeddedContainers = new LinkedList<>();
    
    private final Collection<DockerITContainer> dockerContainers = new LinkedList<>();
    
    private volatile boolean started;
    
    /**
     * Register container.
     * 
     * @param container container to be registered
     * @param networkAlias network alias
     * @param <T> type of ShardingSphere container
     * @return registered container
     */
    public <T extends ITContainer> T registerContainer(final T container, final String networkAlias) {
        if (container instanceof EmbeddedITContainer) {
            embeddedContainers.add((EmbeddedITContainer) container);
        } else {
            DockerITContainer dockerContainer = (DockerITContainer) container;
            dockerContainer.setNetwork(network);
            dockerContainer.setNetworkAliases(Collections.singletonList(networkAlias));
            String loggerName = String.join(":", scenario, dockerContainer.getName());
            dockerContainer.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(loggerName), false));
            dockerContainers.add(dockerContainer);
        }
        return container;
    }
    
    @Override
    public void start() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    embeddedContainers.forEach(EmbeddedITContainer::start);
                    dockerContainers.stream().filter(each -> !each.isCreated()).forEach(DockerITContainer::start);
                    waitUntilReady();
                    started = true;
                }
            }
        }
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
                        try {
                            TimeUnit.MILLISECONDS.sleep(500L);
                        } catch (final InterruptedException ignored) {
                        }
                    }
                });
    }
    
    @Override
    public void stop() {
        embeddedContainers.forEach(Startable::close);
        dockerContainers.forEach(Startable::close);
    }
}
