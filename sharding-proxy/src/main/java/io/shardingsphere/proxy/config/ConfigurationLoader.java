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

import com.google.common.base.Optional;
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
public final class ConfigurationLoader {
    
    private static final ConfigurationLoader INSTANCE = new ConfigurationLoader();
    
    private static final String CONFIG_PATH = "/conf/";
    
    private static final String SERVER_CONFIG_FILE = "server.yaml";
    
    private static final Pattern RULE_CONFIG_FILE_PATTERN = Pattern.compile("config-.+\\.yaml");
    
    private ServerConfiguration serverConfiguration;
    
    private Collection<RuleConfiguration> ruleConfigurations = new LinkedList<>();
    
    private Collection<String> schemaNames = new LinkedHashSet<>();
    
    /**
     * Get instance of proxy configuration loader.
     *
     * @return instance of proxy configuration loader.
     */
    public static ConfigurationLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * load all proxy configuration.
     *
     * @throws IOException IO exception
     */
    public void loadConfiguration() throws IOException {
        serverConfiguration = loadServerConfiguration(new File(ConfigurationLoader.class.getResource(CONFIG_PATH + SERVER_CONFIG_FILE).getFile()));
        File configPath = new File(ConfigurationLoader.class.getResource(CONFIG_PATH).getFile());
        for (File each : findRuleConfigurationFiles(configPath)) {
            Optional<RuleConfiguration> ruleConfig = loadRuleConfiguration(each);
            if (ruleConfig.isPresent()) {
                Preconditions.checkState(schemaNames.add(ruleConfig.get().getSchemaName()), "Schema name `%s` must unique at all rule configurations.", ruleConfig.get().getSchemaName());
                ruleConfigurations.add(ruleConfig.get());
            }
        }
        Preconditions.checkState(!ruleConfigurations.isEmpty(), "Can not find any sharding rule configuration file in path `%s`.", configPath.getPath());
    }
    
    private ServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            ServerConfiguration result = new Yaml(new Constructor(ServerConfiguration.class)).loadAs(inputStreamReader, ServerConfiguration.class);
            Preconditions.checkNotNull(result, "Server configuration file `%s` is invalid.", yamlFile.getName());
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getProxyAuthority().getUsername()) || null != result.getOrchestration(), "Authority configuration is invalid.");
            return result;
        }
    }
    
    private Optional<RuleConfiguration> loadRuleConfiguration(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            RuleConfiguration result = new Yaml(new Constructor(RuleConfiguration.class)).loadAs(inputStreamReader, RuleConfiguration.class);
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
