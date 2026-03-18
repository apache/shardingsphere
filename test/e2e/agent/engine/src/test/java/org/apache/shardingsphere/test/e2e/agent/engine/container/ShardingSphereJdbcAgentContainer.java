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

package org.apache.shardingsphere.test.e2e.agent.engine.container;

import com.google.common.base.Strings;
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * ShardingSphere jdbc container.
 */
public final class ShardingSphereJdbcAgentContainer extends DockerE2EContainer {
    
    private static final String CONFIG_PATH_IN_CONTAINER = "/opt/shardingsphere-jdbc-app/";
    
    private final String plugin;
    
    private final Consumer<OutputFrame> consumer;
    
    public ShardingSphereJdbcAgentContainer(final String image, final String plugin, final Consumer<OutputFrame> consumer) {
        super("jdbc-agent", image);
        this.consumer = consumer;
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        mapResources(createResourceMappingForProxy());
        Optional.ofNullable(consumer).ifPresent(optional -> withLogConsumer(consumer));
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*JdbcProjectApplication started.*"));
    }
    
    private Map<String, String> createResourceMappingForProxy() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("/env/jdbc/conf/config.yaml", CONFIG_PATH_IN_CONTAINER + "conf/config.yaml");
        if (!Strings.isNullOrEmpty(plugin)) {
            result.put(String.format("/env/agent/conf/%s/agent.yaml", plugin), CONFIG_PATH_IN_CONTAINER + "agent/conf/agent.yaml");
        }
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc-agent";
    }
}
