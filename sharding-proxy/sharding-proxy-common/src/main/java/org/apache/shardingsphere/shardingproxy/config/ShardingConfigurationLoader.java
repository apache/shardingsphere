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

package org.apache.shardingsphere.shardingproxy.config;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sharding configuration loader.
 */
public final class ShardingConfigurationLoader {
    
    private static final String DEFAULT_DATASOURCE_NAME = "dataSource";
    
    private static final String SERVER_CONFIG_FILE = "server.yaml";
    
    private static final Pattern RULE_CONFIG_FILE_PATTERN = Pattern.compile("config-.+\\.yaml");
    
    /**
     * Load configuration of Sharding-Proxy.
     *
     * @param path configuration path of Sharding-Proxy
     * @return configuration of Sharding-Proxy
     * @throws IOException IO exception
     */
    public ShardingConfiguration load(final String path) throws IOException {
        Collection<String> schemaNames = new HashSet<>();
        YamlProxyServerConfiguration serverConfig = loadServerConfiguration(new File(ShardingConfigurationLoader.class.getResource(path + "/" + SERVER_CONFIG_FILE).getFile()));
        File configPath = new File(ShardingConfigurationLoader.class.getResource(path).getFile());
        Collection<YamlProxyRuleConfiguration> ruleConfigurations = new LinkedList<>();
        for (File each : findRuleConfigurationFiles(configPath)) {
            loadRuleConfiguration(each).ifPresent(yamlProxyRuleConfiguration -> {
                Preconditions.checkState(
                        schemaNames.add(yamlProxyRuleConfiguration.getSchemaName()), "Schema name `%s` must unique at all rule configurations.", yamlProxyRuleConfiguration.getSchemaName());
                ruleConfigurations.add(yamlProxyRuleConfiguration);
            });
        }
        Preconditions.checkState(!ruleConfigurations.isEmpty() || null != serverConfig.getOrchestration(), "Can not find any sharding rule configuration file in path `%s`.", configPath.getPath());
        Map<String, YamlProxyRuleConfiguration> ruleConfigurationMap = ruleConfigurations.stream().collect(Collectors.toMap(YamlProxyRuleConfiguration::getSchemaName, each -> each));
        return new ShardingConfiguration(serverConfig, ruleConfigurationMap);
    }
    
    private YamlProxyServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        YamlProxyServerConfiguration result = YamlEngine.unmarshal(yamlFile, YamlProxyServerConfiguration.class);
        Preconditions.checkNotNull(result, "Server configuration file `%s` is invalid.", yamlFile.getName());
        Preconditions.checkState(null != result.getAuthentication() || null != result.getOrchestration(), "Authority configuration is invalid.");
        return result;
    }
    
    private Optional<YamlProxyRuleConfiguration> loadRuleConfiguration(final File yamlFile) throws IOException {
        YamlProxyRuleConfiguration result = YamlEngine.unmarshal(yamlFile, YamlProxyRuleConfiguration.class);
        if (null == result) {
            return Optional.empty();
        }
        Preconditions.checkNotNull(result.getSchemaName(), "Property `schemaName` in file `%s` is required.", yamlFile.getName());
        if (result.getDataSources().isEmpty() && null != result.getDataSource()) {
            result.getDataSources().put(DEFAULT_DATASOURCE_NAME, result.getDataSource());
        }
        Preconditions.checkState(!result.getDataSources().isEmpty(), "Data sources configuration in file `%s` is required.", yamlFile.getName());
        return Optional.of(result);
    }
    
    private File[] findRuleConfigurationFiles(final File path) {
        return path.listFiles(pathname -> RULE_CONFIG_FILE_PATTERN.matcher(pathname.getName()).matches());
    }
}
