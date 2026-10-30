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

package org.apache.shardingsphere.proxy.backend.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.validator.constraint.NoConflictingDataSourceNames;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Proxy configuration load result.
 */
@RequiredArgsConstructor
@Getter
@NoConflictingDataSourceNames
public final class ProxyConfigurationLoadResult {
    
    /**
     * Mode configuration, or {@code null} to use the default mode.
     */
    private final ModeConfiguration modeConfiguration;
    
    private final Map<String, DataSourceConfiguration> dataSources;
    
    private final Collection<RuleConfiguration> rules;
    
    private final Properties properties;
    
    private final Map<String, ProxyDatabaseConfiguration> databaseConfigurations;
}
