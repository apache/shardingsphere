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

package org.apache.shardingsphere.proxy.config.yaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlConfiguration;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;

import java.util.Properties;

/**
 * Server configuration for YAML.
 */
@Getter
@Setter
public final class YamlProxyServerConfiguration implements YamlConfiguration {
    
    private YamlAuthenticationConfiguration authentication;
    
    private YamlOrchestrationConfiguration orchestration;
    
    private YamlClusterConfiguration cluster;
    
    private YamlMetricsConfiguration metrics;
    
    private Properties props = new Properties();
}
