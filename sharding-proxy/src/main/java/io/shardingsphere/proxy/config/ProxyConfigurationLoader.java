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

package io.shardingsphere.proxy.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * proxy configuration loader.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyConfigurationLoader {
    
    private static final ProxyConfigurationLoader INSTANCE = new ProxyConfigurationLoader();
    
    private static final String CONFIG_PATH = "/conf/";
    
    private static final String SERVER_CONFIG_FILE = "server.yaml";
    
    private static final Pattern RULE_CONFIG_FILE_PATTERN = Pattern.compile("config-.+\\.yaml");
    
    private YamlProxyServerConfiguration serverConfig;
    
    private Collection<YamlProxyShardingRuleConfiguration> ruleConfigs = new LinkedList<>();
    
    private Collection<String> schemaNames = new LinkedHashSet<>();
    
    /**
     * Get instance of proxy configuration loader.
     *
     * @return instance of proxy configuration loader.
     */
    public static ProxyConfigurationLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * load all proxy configuration.
     *
     * @throws IOException IO exception
     */
    public void loadConfiguration() throws IOException {
        serverConfig = loadServerConfiguration(new File(ProxyConfigurationLoader.class.getResource(CONFIG_PATH + SERVER_CONFIG_FILE).getFile()));
        File configPath = new File(ProxyConfigurationLoader.class.getResource(CONFIG_PATH).getFile());
        File[] ruleConfigFiles = findRuleConfigurationFiles(configPath);
        Preconditions.checkState(ruleConfigFiles.length > 0, "Can not find any sharding rule configuration file in path `%s`.", configPath.getPath());
        for (File each : ruleConfigFiles) {
            YamlProxyShardingRuleConfiguration ruleConfig = loadRuleConfiguration(each);
            Preconditions.checkState(schemaNames.add(ruleConfig.getSchemaName()), "Schema name `%s` must unique at all rule configurations.", ruleConfig.getSchemaName());
            ruleConfigs.add(ruleConfig);
        }
    }
    
    private YamlProxyServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyServerConfiguration result = new Yaml(new Constructor(YamlProxyServerConfiguration.class)).loadAs(inputStreamReader, YamlProxyServerConfiguration.class);
            Preconditions.checkNotNull(result, "Server configuration file `%s` is invalid.", yamlFile.getName());
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getProxyAuthority().getUsername()) || null != result.getOrchestration(), "Authority configuration is invalid.");
            return result;
        }
    }
    
    private YamlProxyShardingRuleConfiguration loadRuleConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyShardingRuleConfiguration result = new Yaml(new Constructor(YamlProxyShardingRuleConfiguration.class)).loadAs(inputStreamReader, YamlProxyShardingRuleConfiguration.class);
            Preconditions.checkNotNull(result, "Configuration file `%s` is invalid.", yamlFile.getName());
            Preconditions.checkNotNull(result.getSchemaName(), "Property `schemaName` in file `%s` is required.", yamlFile.getName());
            Preconditions.checkState(!result.getDataSources().isEmpty(), "Data sources configuration in file `%s` is required.", yamlFile.getName());
            Preconditions.checkState(null != result.getShardingRule() || null != result.getMasterSlaveRule() || null != serverConfig.getOrchestration(),
                    "Configuration invalid in file `%s`, local and orchestration configuration are required at least one.", yamlFile.getName());
            return result;
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
