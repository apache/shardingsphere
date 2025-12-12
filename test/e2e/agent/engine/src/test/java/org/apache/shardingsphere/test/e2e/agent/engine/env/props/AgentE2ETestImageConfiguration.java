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

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Properties;

/**
 * Agent E2E test image configuration.
 */
@Getter
public final class AgentE2ETestImageConfiguration {
    
    private static final AgentE2ETestImageConfiguration INSTANCE = new AgentE2ETestImageConfiguration();
    
    private final String mysqlImage;
    
    private final String proxyImage;
    
    private final String jdbcProjectImage;
    
    private AgentE2ETestImageConfiguration() {
        Properties imageProps = EnvironmentPropertiesLoader.loadProperties("env/image.properties");
        mysqlImage = imageProps.getProperty("mysql.image", "mysql:8.0");
        proxyImage = imageProps.getProperty("proxy.image", "apache/shardingsphere-proxy-agent-test:latest");
        jdbcProjectImage = imageProps.getProperty("jdbc.project.image", "apache/shardingsphere-jdbc-agent-test:latest");
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static AgentE2ETestImageConfiguration getInstance() {
        return INSTANCE;
    }
}
