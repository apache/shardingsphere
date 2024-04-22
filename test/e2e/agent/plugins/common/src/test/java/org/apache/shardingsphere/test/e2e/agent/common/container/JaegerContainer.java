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

package org.apache.shardingsphere.test.e2e.agent.common.container;

import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Jaeger container.
 */
public final class JaegerContainer extends DockerITContainer {
    
    public JaegerContainer(final String image) {
        super("jaeger", image);
    }
    
    @Override
    protected void configure() {
        withExposedPorts(4317, 16686);
        getContainerEnvironments().forEach(this::addEnv);
        setWaitStrategy(new HttpWaitStrategy().forPort(16686));
    }
    
    private Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(1, 1F);
        result.put("COLLECTOR_OTLP_ENABLED", Boolean.TRUE.toString());
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "jaeger";
    }
    
    public String getHttpUrl() {
        return String.format("http://%s:%s", getHost(), getMappedPort(16686));
    }
}
