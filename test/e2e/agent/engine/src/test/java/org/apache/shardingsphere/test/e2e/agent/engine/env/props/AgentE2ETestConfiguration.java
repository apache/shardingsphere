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

package org.apache.shardingsphere.test.e2e.agent.engine.env.props;

import com.google.common.base.Strings;
import lombok.Getter;

import java.util.Properties;

/**
 * Agent E2E test configuration.
 */
@Getter
public final class AgentE2ETestConfiguration {
    
    private static final AgentE2ETestConfiguration INSTANCE = new AgentE2ETestConfiguration();
    
    private final String adapter;
    
    private final String pluginType;
    
    private final String pluginImageName;
    
    private final boolean logEnabled;
    
    private final int defaultExposePort;
    
    private final long collectDataWaitSeconds;
    
    private AgentE2ETestConfiguration() {
        Properties envProps = AgentE2EEnvironmentProperties.loadProperties("env/engine-env.properties");
        adapter = envProps.getProperty("it.env.adapter");
        pluginType = envProps.getProperty("it.env.plugin.type");
        pluginImageName = envProps.getProperty("it.env.plugin.image");
        logEnabled = Boolean.parseBoolean(envProps.getProperty("it.env.log.enabled", Boolean.FALSE.toString()));
        defaultExposePort = Integer.parseInt(envProps.getProperty("it.env.plugin.default.expose.port", "0"));
        collectDataWaitSeconds = Long.parseLong(envProps.getProperty("it.env.collect.data.wait.seconds", "0"));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static AgentE2ETestConfiguration getInstance() {
        return INSTANCE;
    }
    
    /**
     * Judge whether contains test parameter.
     *
     * @return contains or not
     */
    public boolean containsTestParameter() {
        return !Strings.isNullOrEmpty(adapter) && !Strings.isNullOrEmpty(pluginType);
    }
}
