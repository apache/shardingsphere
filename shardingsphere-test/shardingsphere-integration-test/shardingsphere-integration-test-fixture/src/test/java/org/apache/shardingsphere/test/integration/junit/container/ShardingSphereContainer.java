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

package org.apache.shardingsphere.test.integration.junit.container;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ShardingSphere container.
 */
@Slf4j
public abstract class ShardingSphereContainer extends GenericContainer<ShardingSphereContainer> {
    
    @Getter
    private final boolean isFakeContainer;
    
    @Getter
    private final ParameterizedArray parameterizedArray;
    
    @Getter
    private final String dockerName;
    
    public ShardingSphereContainer(final String dockerName, final String dockerImageName, final boolean isFakeContainer, final ParameterizedArray parameterizedArray) {
        super(convertToDockerImage(dockerImageName, isFakeContainer));
        this.dockerName = dockerName;
        this.isFakeContainer = isFakeContainer;
        this.parameterizedArray = parameterizedArray;
    }
    
    private static RemoteDockerImage convertToDockerImage(final String dockerImageName, final boolean isFakeContainer) {
        if (isFakeContainer) {
            return new RemoteDockerImage(DockerImageName.parse(dockerImageName)).withImagePullPolicy(dockerName -> false);
        }
        return new RemoteDockerImage(DockerImageName.parse(dockerImageName));
    }
    
    @Override
    public void start() {
        configure();
        startDependencies();
        if (!isFakeContainer) {
            super.start();
        }
        execute();
    }
    
    private void startDependencies() {
        List<ShardingSphereContainer> dependencies = getDependencies().stream()
                .map(e -> (ShardingSphereContainer) e)
                .collect(Collectors.toList());
        dependencies.stream()
                .filter(c -> !c.isCreated())
                .forEach(GenericContainer::start);
        dependencies.stream()
                .filter(c -> {
                    try {
                        return !c.isHealthy();
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        log.info("Failed to check container {} healthy.", c.getDockerName(), ex);
                        return false;
                    }
                })
                .forEach(c -> {
                    DockerHealthcheckWaitStrategy waitStrategy = new DockerHealthcheckWaitStrategy();
                    log.info("Waiting for container {} healthy.", c.getDockerImageName());
                    waitStrategy.withStartupTimeout(Duration.of(90, ChronoUnit.SECONDS));
                    waitStrategy.waitUntilReady(c);
                    log.info("Container {} is startup.", c.getDockerImageName());
                });
    }
    
    protected void execute() {
    }
}
