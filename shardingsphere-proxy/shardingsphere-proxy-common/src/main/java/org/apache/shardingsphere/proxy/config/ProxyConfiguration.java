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

package org.apache.shardingsphere.proxy.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Proxy configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ProxyConfiguration {
    
    private final Map<String, Map<String, DataSourceParameter>> schemaDataSources;
    
    private final Map<String, Collection<RuleConfiguration>> schemaRules;
    
    private final Authentication authentication;
    
    private final ClusterConfiguration cluster;
    
    private final MetricsConfiguration metrics;
    
    private final Properties props;
}
