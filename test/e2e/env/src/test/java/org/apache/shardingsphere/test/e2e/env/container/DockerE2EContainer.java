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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Docker E2E container.
 */
@Getter
@Setter
@Slf4j
public abstract class DockerE2EContainer extends GenericContainer<DockerE2EContainer> implements E2EContainer {
    
    private String name;
    
    protected DockerE2EContainer(final String name, final String containerImage) {
        super(new RemoteDockerImage(DockerImageName.parse(containerImage)));
        this.name = name;
    }
    
    protected final void mapResources(final Map<String, String> resources) {
        resources.forEach((key, value) -> withClasspathResourceMapping(key, value, BindMode.READ_ONLY));
    }
    
    @Override
    public final void start() {
        startDependencies();
        super.start();
        postStart();
    }
    
    private void startDependencies() {
        Collection<DockerE2EContainer> dependencies = getDependencies().stream().filter(DockerE2EContainer.class::isInstance).map(DockerE2EContainer.class::cast).collect(Collectors.toList());
        dependencies.stream().filter(each -> !each.isCreated()).forEach(GenericContainer::start);
        dependencies.stream()
                .filter(each -> {
                    try {
                        return !each.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        log.info("Failed to check container {} healthy.", each.getName());
                        return false;
                    }
                })
                .forEach(each -> {
                    DockerHealthcheckWaitStrategy waitStrategy = new DockerHealthcheckWaitStrategy();
                    log.info("Waiting for container {} healthy.", each.getDockerImageName());
                    waitStrategy.withStartupTimeout(Duration.of(90, ChronoUnit.SECONDS));
                    waitStrategy.waitUntilReady(each);
                    log.info("Container {} is startup.", each.getDockerImageName());
                });
    }
    
    protected void postStart() {
    }
}
