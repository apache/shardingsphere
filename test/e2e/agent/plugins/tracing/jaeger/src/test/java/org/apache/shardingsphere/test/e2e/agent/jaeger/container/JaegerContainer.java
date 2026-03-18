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

package org.apache.shardingsphere.test.e2e.agent.jaeger.container;

import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.util.Collections;
import java.util.Map;

/**
 * Jaeger container.
 */
public final class JaegerContainer extends DockerE2EContainer {
    
    public JaegerContainer(final String image) {
        super("jaeger", image);
    }
    
    @Override
    protected void configure() {
        withExposedPorts(4317, AgentE2ETestConfiguration.getInstance().getDefaultExposePort());
        getContainerEnvironments().forEach(this::addEnv);
        setWaitStrategy(new HttpWaitStrategy().forPort(AgentE2ETestConfiguration.getInstance().getDefaultExposePort()));
    }
    
    private Map<String, String> getContainerEnvironments() {
        return Collections.singletonMap("COLLECTOR_OTLP_ENABLED", Boolean.TRUE.toString());
    }
    
    @Override
    public String getAbbreviation() {
        return "jaeger";
    }
}
