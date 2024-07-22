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

package org.apache.shardingsphere.test.e2e.agent.metrics.container;

import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

/**
 * Prometheus container.
 */
public final class PrometheusContainer extends DockerITContainer {
    
    public PrometheusContainer(final String image) {
        super("prometheus", image);
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping("/env/prometheus/prometheus.yml", "/etc/prometheus/prometheus.yml", BindMode.READ_ONLY);
        setWaitStrategy(new HttpWaitStrategy().forPort(AgentE2ETestConfiguration.getInstance().getDefaultExposePort()).forPath("/-/ready"));
        withExposedPorts(AgentE2ETestConfiguration.getInstance().getDefaultExposePort());
    }
    
    @Override
    public String getAbbreviation() {
        return "prometheus";
    }
}
