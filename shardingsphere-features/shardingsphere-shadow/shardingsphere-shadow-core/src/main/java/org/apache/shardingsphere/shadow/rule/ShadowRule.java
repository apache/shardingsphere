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

package org.apache.shardingsphere.shadow.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.checker.ShadowRuleChecker;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Databases shadow rule.
 */
@Getter
public final class ShadowRule implements SchemaRule, DataSourceContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(ShadowAlgorithm.class);
    }
    
    private final Map<String, String> shadowMappings;
    
    private final String column;
    
    private final boolean enable;
    
    private final Map<String, ShadowDataSourceRule> shadowDataSourceMappings = new LinkedHashMap<>();
    
    private final Map<String, ShadowAlgorithm> shadowAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, ShadowTableRule> shadowTableRules = new LinkedHashMap<>();
    
    public ShadowRule(final ShadowRuleConfiguration shadowRuleConfig) {
        enable = shadowRuleConfig.isEnable();
        column = shadowRuleConfig.getColumn();
        shadowMappings = new HashMap<>(shadowRuleConfig.getShadowDataSourceNames().size());
        for (int i = 0; i < shadowRuleConfig.getSourceDataSourceNames().size(); i++) {
            shadowMappings.put(shadowRuleConfig.getSourceDataSourceNames().get(i), shadowRuleConfig.getShadowDataSourceNames().get(i));
        }
    }
    
    public ShadowRule(final AlgorithmProvidedShadowRuleConfiguration shadowRuleConfig) {
        enable = shadowRuleConfig.isEnable();
        column = shadowRuleConfig.getColumn();
        shadowMappings = new HashMap<>(shadowRuleConfig.getShadowDataSourceNames().size());
        for (int i = 0; i < shadowRuleConfig.getSourceDataSourceNames().size(); i++) {
            shadowMappings.put(shadowRuleConfig.getSourceDataSourceNames().get(i), shadowRuleConfig.getShadowDataSourceNames().get(i));
        }
        if (enable) {
            initShadowDataSourceMappings(shadowRuleConfig.getDataSources());
            initShadowAlgorithms(shadowRuleConfig.getShadowAlgorithms());
            initShadowTableRules(shadowRuleConfig.getTables(), shadowRuleConfig.getShadowAlgorithms());
        }
    }
    
    private void initShadowTableRules(final Map<String, ShadowTableConfiguration> tables, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        ShadowRuleChecker.checkShadowTables(tables);
        tables.forEach((key, value) -> {
            Collection<String> tableShadowAlgorithmNames = value.getShadowAlgorithmNames();
            uselessShadowAlgorithmFilter(tableShadowAlgorithmNames, shadowAlgorithms);
            ShadowRuleChecker.checkTableShadowAlgorithms(key, tableShadowAlgorithmNames, shadowAlgorithms);
            shadowTableRules.put(key, new ShadowTableRule(key, getDataSourceName(value), tableShadowAlgorithmNames));
        });
    }
    
    private Collection<String> getDataSourceName(final ShadowTableConfiguration shadowTableConfiguration) {
        Collection<String> result = new LinkedList<>();
        Collection<String> dataSourceNames = shadowTableConfiguration.getDataSourceNames();
        if (1 == shadowDataSourceMappings.size() && dataSourceNames.isEmpty()) {
            result.add(shadowDataSourceMappings.keySet().iterator().next());
            return result;
        }
        result = dataSourceNames.stream().filter(each -> null != shadowDataSourceMappings.get(each)).collect(Collectors.toCollection(LinkedList::new));
        ShadowRuleChecker.checkShadowTableDataSources(result);
        return result;
    }
    
    private void uselessShadowAlgorithmFilter(final Collection<String> tableShadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        tableShadowAlgorithmNames.removeIf(each -> Objects.isNull(shadowAlgorithms.get(each)));
    }
    
    private void initShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        ShadowRuleChecker.checkShadowAlgorithms(shadowAlgorithms);
        this.shadowAlgorithms.putAll(shadowAlgorithms);
    }
    
    private void initShadowDataSourceMappings(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        ShadowRuleChecker.checkDataSources(dataSources);
        dataSources.forEach((key, value) -> shadowDataSourceMappings.put(key, new ShadowDataSourceRule(value.getSourceDataSourceName(), value.getShadowDataSourceName())));
    }
    
    /**
     * Get related shadow tables.
     *
     * @param tableNames table names
     * @return related shadow tables
     */
    public Collection<String> getRelatedShadowTables(final Collection<String> tableNames) {
        return tableNames.stream().filter(shadowTableRules.keySet()::contains).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Get all shadow table names.
     *
     * @return shadow table names
     */
    public Collection<String> getAllShadowTableNames() {
        return shadowTableRules.keySet();
    }
    
    /**
     * Get related shadow algorithms by table name.
     *
     * @param tableName table name
     * @return shadow algorithms
     */
    public Optional<Collection<ShadowAlgorithm>> getRelatedShadowAlgorithms(final String tableName) {
        ShadowTableRule shadowTableRule = shadowTableRules.get(tableName);
        if (Objects.isNull(shadowTableRule)) {
            return Optional.empty();
        }
        Collection<ShadowAlgorithm> result = shadowTableRule.getShadowAlgorithmNames().stream().map(shadowAlgorithms::get).filter(each -> !Objects.isNull(each))
                .collect(Collectors.toCollection(LinkedList::new));
        return result.isEmpty() ? Optional.of(result) : Optional.of(result);
    }
    
    /**
     * Get shadow data source mappings.
     *
     * @param tableName table name
     * @return shadow data source rules
     */
    public Optional<Map<String, String>> getRelatedShadowDataSourceMappings(final String tableName) {
        Map<String, String> result = new LinkedHashMap<>();
        Collection<String> shadowDataSources = shadowTableRules.get(tableName).getShadowDataSources();
        shadowDataSources.forEach(each -> {
            ShadowDataSourceRule shadowDataSourceRule = shadowDataSourceMappings.get(each);
            if (null != shadowDataSourceRule) {
                result.put(shadowDataSourceRule.getSourceDataSource(), shadowDataSourceRule.getShadowDataSource());
            }
        });
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(shadowMappings.size());
        for (Entry<String, String> entry : shadowMappings.entrySet()) {
            result.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        return result;
    }
}
