/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.config;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.shardingproxy.config.yaml.YamlProxyRuleConfiguration;
import io.shardingsphere.shardingproxy.config.yaml.YamlProxyServerConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Sharding configuration loader.
 *
 * @author chenqingyang
 */
public final class ShardingConfigurationLoader {
    
    private static final String CONFIG_PATH = "/conf/";
    
    private static final String SERVER_CONFIG_FILE = "server.yaml";
    
    private static final Pattern RULE_CONFIG_FILE_PATTERN = Pattern.compile("config-.+\\.yaml");
    
    /**
     * Load proxy configuration.
     *
     * @return proxy configuration
     * @throws IOException IO exception
     */
    public ShardingConfiguration load() throws IOException {
        Collection<String> schemaNames = new HashSet<>();
        YamlProxyServerConfiguration serverConfig = loadServerConfiguration(new File(ShardingConfigurationLoader.class.getResource(CONFIG_PATH + SERVER_CONFIG_FILE).getFile()));
        File configPath = new File(ShardingConfigurationLoader.class.getResource(CONFIG_PATH).getFile());
        Collection<YamlProxyRuleConfiguration> ruleConfigurations = new LinkedList<>();
        for (File each : findRuleConfigurationFiles(configPath)) {
            Optional<YamlProxyRuleConfiguration> ruleConfig = loadRuleConfiguration(each, serverConfig);
            if (ruleConfig.isPresent()) {
                Preconditions.checkState(schemaNames.add(ruleConfig.get().getSchemaName()), "Schema name `%s` must unique at all rule configurations.", ruleConfig.get().getSchemaName());
                ruleConfigurations.add(ruleConfig.get());
            }
        }
        Preconditions.checkState(!ruleConfigurations.isEmpty() || null != serverConfig.getOrchestration(), "Can not find any sharding rule configuration file in path `%s`.", configPath.getPath());
        Map<String, YamlProxyRuleConfiguration> ruleConfigurationMap = new HashMap<>(ruleConfigurations.size(), 1);
        for (YamlProxyRuleConfiguration each : ruleConfigurations) {
            ruleConfigurationMap.put(each.getSchemaName(), each);
        }
        return new ShardingConfiguration(serverConfig, ruleConfigurationMap);
    }
    
    private YamlProxyServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyServerConfiguration result = new Yaml(new Constructor(YamlProxyServerConfiguration.class)).loadAs(inputStreamReader, YamlProxyServerConfiguration.class);
            Preconditions.checkNotNull(result, "Server configuration file `%s` is invalid.", yamlFile.getName());
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getAuthentication().getUsername()) || null != result.getOrchestration(), "Authority configuration is invalid.");
            return result;
        }
    }
    
    private Optional<YamlProxyRuleConfiguration> loadRuleConfiguration(final File yamlFile, final YamlProxyServerConfiguration serverConfiguration) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyRuleConfiguration result = new Yaml(new Constructor(YamlProxyRuleConfiguration.class)).loadAs(inputStreamReader, YamlProxyRuleConfiguration.class);
            if (null == result) {
                return Optional.absent();
            }
            Preconditions.checkNotNull(result.getSchemaName(), "Property `schemaName` in file `%s` is required.", yamlFile.getName());
            Preconditions.checkState(!result.getDataSources().isEmpty(), "Data sources configuration in file `%s` is required.", yamlFile.getName());
            Preconditions.checkState(null != result.getShardingRule() || null != result.getMasterSlaveRule() || null != serverConfiguration.getOrchestration(),
                    "Configuration invalid in file `%s`, local and orchestration configuration are required at least one.", yamlFile.getName());
            return Optional.of(result);
        }
    }
    
    private File[] findRuleConfigurationFiles(final File path) {
        return path.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(final File pathname) {
                return RULE_CONFIG_FILE_PATTERN.matcher(pathname.getName()).matches();
            }
        });
    }
}
