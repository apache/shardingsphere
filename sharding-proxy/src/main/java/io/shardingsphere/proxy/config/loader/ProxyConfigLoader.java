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

package io.shardingsphere.proxy.config.loader;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.proxy.config.YamlProxyServerConfiguration;
import io.shardingsphere.proxy.config.YamlProxyShardingRuleConfiguration;
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
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * proxy config loader.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyConfigLoader {
    
    private static final ProxyConfigLoader INSTANCE = new ProxyConfigLoader();
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    private static final Pattern PATTERN_CONFIG_FILE_NAME = Pattern.compile("config-[\\w\\-]+.yaml");
    
    private static final String DEFAULT_SERVER_CONFIG_FILE = "server.yaml";
    
    private YamlProxyServerConfiguration yamlServerConfiguration;
    
    private Collection<YamlProxyShardingRuleConfiguration> yamlProxyShardingRuleConfigurations = new LinkedList<>();
    
    private Collection<String> schemaNames = new LinkedList<>();
    
    /**
     * Get instance of proxy config loader.
     *
     * @return instance of proxy config loader.
     */
    public static ProxyConfigLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * load all proxy config.
     *
     * @throws IOException IO exception
     */
    public void loadConfiguration() throws IOException {
        yamlServerConfiguration = loadServerConfiguration(new File(ProxyConfigLoader.class.getResource(DEFAULT_CONFIG_PATH + DEFAULT_SERVER_CONFIG_FILE).getFile()));
        File configDir = new File(ProxyConfigLoader.class.getResource(DEFAULT_CONFIG_PATH).getFile());
        File[] shardingRuleConfiFiles = findShardingRuleConfiFiles(configDir);
        Preconditions.checkState(shardingRuleConfiFiles.length > 0, "No sharding Configuration file found");
        for (File shardingRuleConfigFile : shardingRuleConfiFiles) {
            YamlProxyShardingRuleConfiguration yamlProxyShardingRuleConfiguration = loadShardingRuleConfiguration(shardingRuleConfigFile);
            yamlProxyShardingRuleConfigurations.add(yamlProxyShardingRuleConfiguration);
            schemaNames.add(yamlProxyShardingRuleConfiguration.getSchemaName());
        }
    }
    
    private YamlProxyShardingRuleConfiguration loadShardingRuleConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyShardingRuleConfiguration result = new Yaml(new Constructor(YamlProxyShardingRuleConfiguration.class)).loadAs(inputStreamReader, YamlProxyShardingRuleConfiguration.class);
            Preconditions.checkNotNull(result, String.format("Configuration file `%s` is invalid.", yamlFile.getName()));
            Preconditions.checkNotNull(result.getSchemaName(), String.format("schemaName configuration in file `%s` can not be null.", yamlFile.getName()));
            Preconditions.checkState(!schemaNames.contains(result.getSchemaName()), String.format("schemaName `%s` has already exist.", result.getSchemaName()));
            Preconditions.checkState(!result.getDataSources().isEmpty(), String.format("Data sources configuration in file `%s` can not be empty.", yamlFile.getName()));
            Preconditions.checkState(null != result.getShardingRule() || null != result.getMasterSlaveRule() || null != yamlServerConfiguration.getOrchestration(),
                    String.format("Configuration invalid in file `%s`, sharding rule, local and orchestration configuration can not be both null.", yamlFile.getName()));
            return result;
        }
    }
    
    private YamlProxyServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        try (
                
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            YamlProxyServerConfiguration result = new Yaml(new Constructor(YamlProxyServerConfiguration.class)).loadAs(inputStreamReader, YamlProxyServerConfiguration.class);
            Preconditions.checkNotNull(result, String.format("Server configuration file `%s` is invalid.", yamlFile.getName()));
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getProxyAuthority().getUsername()) || null != result.getOrchestration(), "Authority configuration is invalid.");
            return result;
            
        }
    }
    
    private File[] findShardingRuleConfiFiles(final File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                String name = pathname.getName();
                if (PATTERN_CONFIG_FILE_NAME.matcher(name).matches()) {
                    return true;
                }
                return false;
            }
        });
    }
    
}
