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

package org.apache.shardingsphere.test.integration.env.container.atomic;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Docker IT container.
 */
@Getter
@Slf4j
public abstract class DockerITContainer extends GenericContainer<DockerITContainer> implements ITContainer {
    
    private final String name;
    
    public DockerITContainer(final String name, final String dockerImageName) {
        super(new RemoteDockerImage(DockerImageName.parse(dockerImageName)));
        this.name = name;
    }
    
    @Override
    public void start() {
        startDependencies();
        super.start();
        postStart();
    }
    
    private void startDependencies() {
        Collection<DockerITContainer> dependencies = getDependencies().stream().filter(each -> each instanceof DockerITContainer).map(each -> (DockerITContainer) each).collect(Collectors.toList());
        dependencies.stream().filter(each -> !each.isCreated()).forEach(GenericContainer::start);
        dependencies.stream()
                .filter(each -> {
                    try {
                        return !each.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        log.info("Failed to check container {} healthy.", each.getName(), ex);
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
