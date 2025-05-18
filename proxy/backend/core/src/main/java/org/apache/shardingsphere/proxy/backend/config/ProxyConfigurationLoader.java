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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.config.checker.YamlProxyConfigurationChecker;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Proxy configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyConfigurationLoader {
    
    private static final String GLOBAL_CONFIG_FILE = "global.yaml";
    
    private static final Pattern DATABASE_CONFIG_FILE_PATTERN = Pattern.compile("database-.+\\.yaml");
    
    // TODO remove COMPATIBLE_GLOBAL_CONFIG_FILE in next major version
    /**
     * to be removed.
     *
     * @deprecated to be removed
     */
    @Deprecated
    private static final String COMPATIBLE_GLOBAL_CONFIG_FILE = "server.yaml";
    
    // TODO remove COMPATIBLE_DATABASE_CONFIG_FILE_PATTERN in next major version
    /**
     * to be removed.
     *
     * @deprecated to be removed
     */
    @Deprecated
    private static final Pattern COMPATIBLE_DATABASE_CONFIG_FILE_PATTERN = Pattern.compile("config-.+\\.yaml");
    
    /**
     * Load configuration of ShardingSphere-Proxy.
     *
     * @param path configuration path of ShardingSphere-Proxy
     * @return configuration of ShardingSphere-Proxy
     * @throws IOException IO exception
     */
    public static YamlProxyConfiguration load(final String path) throws IOException {
        YamlProxyServerConfiguration serverConfig = loadServerConfiguration(getGlobalConfigFile(path));
        File configPath = getResourceFile(path);
        Collection<YamlProxyDatabaseConfiguration> databaseConfigs = loadDatabaseConfigurations(configPath);
        YamlProxyConfigurationChecker.checkDataSources(serverConfig.getDataSources(), databaseConfigs);
        return new YamlProxyConfiguration(serverConfig, databaseConfigs.stream().collect(Collectors.toMap(
                YamlProxyDatabaseConfiguration::getDatabaseName, each -> each, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
    }
    
    private static File getGlobalConfigFile(final String path) {
        File result = getResourceFile(String.join("/", path, GLOBAL_CONFIG_FILE));
        return result.exists() ? result : getResourceFile(String.join("/", path, COMPATIBLE_GLOBAL_CONFIG_FILE));
    }
    
    @SneakyThrows(URISyntaxException.class)
    private static File getResourceFile(final String path) {
        URL url = ProxyConfigurationLoader.class.getResource(path);
        return null == url ? new File(path) : new File(url.toURI().getPath());
    }
    
    private static YamlProxyServerConfiguration loadServerConfiguration(final File yamlFile) throws IOException {
        YamlProxyServerConfiguration result = YamlEngine.unmarshal(yamlFile, YamlProxyServerConfiguration.class);
        return rebuildGlobalRuleConfiguration(result);
    }
    
    private static YamlProxyServerConfiguration rebuildGlobalRuleConfiguration(final YamlProxyServerConfiguration serverConfig) {
        serverConfig.getRules().removeIf(YamlGlobalRuleConfiguration.class::isInstance);
        if (null != serverConfig.getAuthority()) {
            serverConfig.getRules().add(serverConfig.getAuthority());
        }
        if (null != serverConfig.getTransaction()) {
            serverConfig.getRules().add(serverConfig.getTransaction());
        }
        if (null != serverConfig.getGlobalClock()) {
            serverConfig.getRules().add(serverConfig.getGlobalClock());
        }
        if (null != serverConfig.getSqlParser()) {
            serverConfig.getRules().add(serverConfig.getSqlParser());
        }
        if (null != serverConfig.getSqlTranslator()) {
            serverConfig.getRules().add(serverConfig.getSqlTranslator());
        }
        if (null != serverConfig.getSqlFederation()) {
            serverConfig.getRules().add(serverConfig.getSqlFederation());
        }
        return serverConfig;
    }
    
    private static Collection<YamlProxyDatabaseConfiguration> loadDatabaseConfigurations(final File configPath) throws IOException {
        File[] ruleConfigFiles = findRuleConfigurationFiles(configPath);
        Collection<String> loadedDatabaseNames = new HashSet<>(ruleConfigFiles.length);
        Collection<YamlProxyDatabaseConfiguration> result = new LinkedList<>();
        for (File each : ruleConfigFiles) {
            loadDatabaseConfiguration(each).ifPresent(optional -> {
                Preconditions.checkState(loadedDatabaseNames.add(optional.getDatabaseName()), "Database name `%s` must unique at all database configurations.", optional.getDatabaseName());
                result.add(optional);
            });
        }
        return result;
    }
    
    private static Optional<YamlProxyDatabaseConfiguration> loadDatabaseConfiguration(final File yamlFile) throws IOException {
        YamlProxyDatabaseConfiguration result = YamlEngine.unmarshal(yamlFile, YamlProxyDatabaseConfiguration.class);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        Preconditions.checkNotNull(result.getDatabaseName(), "Property `databaseName` in file `%s` is required.", yamlFile.getName());
        checkDuplicateRule(result.getRules(), yamlFile);
        return Optional.of(result);
    }
    
    private static void checkDuplicateRule(final Collection<YamlRuleConfiguration> ruleConfigs, final File yamlFile) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        Map<Class<? extends RuleConfiguration>, Long> ruleConfigTypeCountMap = ruleConfigs.stream()
                .collect(Collectors.groupingBy(YamlRuleConfiguration::getRuleConfigurationType, Collectors.counting()));
        Optional<Entry<Class<? extends RuleConfiguration>, Long>> duplicateRuleConfig = ruleConfigTypeCountMap.entrySet().stream().filter(each -> each.getValue() > 1L).findFirst();
        if (duplicateRuleConfig.isPresent()) {
            throw new IllegalStateException(String.format("Duplicate rule tag `!%s` in file `%s`", getDuplicateRuleTagName(duplicateRuleConfig.get().getKey()), yamlFile.getName()));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static Object getDuplicateRuleTagName(final Class<? extends RuleConfiguration> ruleConfigClass) {
        Optional<YamlRuleConfigurationSwapper> result = ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)
                .stream().filter(each -> ruleConfigClass.equals(each.getTypeClass())).findFirst();
        return result.orElseThrow(() -> new IllegalStateException("Not find rule tag name of class " + ruleConfigClass));
    }
    
    private static File[] findRuleConfigurationFiles(final File path) {
        return path.listFiles(each -> DATABASE_CONFIG_FILE_PATTERN.matcher(each.getName()).matches() || COMPATIBLE_DATABASE_CONFIG_FILE_PATTERN.matcher(each.getName()).matches());
    }
}
