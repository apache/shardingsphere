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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Databases shadow rule.
 */
@Getter
public final class ShadowRule implements SchemaRule, DataSourceContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(ShadowAlgorithm.class);
    }
    
    private ShadowAlgorithm defaultShadowAlgorithm;
    
    private final Collection<String> hintShadowAlgorithmNames = new LinkedList<>();
    
    private final Map<String, ShadowDataSourceRule> shadowDataSourceMappings = new LinkedHashMap<>();
    
    private final Map<String, ShadowAlgorithm> shadowAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, ShadowTableRule> shadowTableRules = new LinkedHashMap<>();
    
    public ShadowRule(final ShadowRuleConfiguration shadowRuleConfig) {
        initShadowDataSourceMappings(shadowRuleConfig.getDataSources());
        initShadowAlgorithmConfigurations(shadowRuleConfig.getShadowAlgorithms());
        initDefaultShadowAlgorithm(shadowRuleConfig.getDefaultShadowAlgorithmName());
        initShadowTableRules(shadowRuleConfig.getTables());
    }
    
    public ShadowRule(final AlgorithmProvidedShadowRuleConfiguration shadowRuleConfig) {
        initShadowDataSourceMappings(shadowRuleConfig.getDataSources());
        initShadowAlgorithms(shadowRuleConfig.getShadowAlgorithms());
        initDefaultShadowAlgorithm(shadowRuleConfig.getDefaultShadowAlgorithmName());
        initShadowTableRules(shadowRuleConfig.getTables());
    }
    
    private void initShadowDataSourceMappings(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        dataSources.forEach((key, value) -> shadowDataSourceMappings.put(key, new ShadowDataSourceRule(value.getSourceDataSourceName(), value.getShadowDataSourceName())));
    }
    
    private void initShadowAlgorithmConfigurations(final Map<String, ShardingSphereAlgorithmConfiguration> shadowAlgorithmConfigurations) {
        shadowAlgorithmConfigurations.forEach((key, value) -> {
            ShadowAlgorithm algorithm = ShardingSphereAlgorithmFactory.createAlgorithm(value, ShadowAlgorithm.class);
            if (algorithm instanceof HintShadowAlgorithm<?>) {
                hintShadowAlgorithmNames.add(key);
            }
            shadowAlgorithms.put(key, algorithm);
        });
    }
    
    private void initShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        shadowAlgorithms.forEach((key, value) -> {
            if (value instanceof HintShadowAlgorithm<?>) {
                hintShadowAlgorithmNames.add(key);
            }
            this.shadowAlgorithms.put(key, value);
        });
    }
    
    private void initDefaultShadowAlgorithm(final String defaultShadowAlgorithmName) {
        ShadowAlgorithm shadowAlgorithm = shadowAlgorithms.get(defaultShadowAlgorithmName);
        if (shadowAlgorithm instanceof HintShadowAlgorithm<?>) {
            hintShadowAlgorithmNames.add(defaultShadowAlgorithmName);
        }
        defaultShadowAlgorithm = shadowAlgorithm;
    }
    
    private void initShadowTableRules(final Map<String, ShadowTableConfiguration> tables) {
        tables.forEach((key, value) -> shadowTableRules.put(key, new ShadowTableRule(key, value.getDataSourceNames(), value.getShadowAlgorithmNames(), shadowAlgorithms)));
    }
    
    /**
     * Get default shadow algorithm.
     *
     * @return shadow algorithm
     */
    public Optional<ShadowAlgorithm> getDefaultShadowAlgorithm() {
        return null == defaultShadowAlgorithm ? Optional.empty() : Optional.of(defaultShadowAlgorithm);
    }
    
    /**
     * Get related shadow tables.
     *
     * @param tableNames table names
     * @return related shadow tables
     */
    public Collection<String> getRelatedShadowTables(final Collection<String> tableNames) {
        Collection<String> result = new LinkedList<>();
        for (String each : tableNames) {
            if (shadowTableRules.containsKey(each)) {
                result.add(each);
            }
        }
        return result;
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
     * Get related hint shadow algorithms.
     *
     * @return related hint shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getAllHintShadowAlgorithms() {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (String each : hintShadowAlgorithmNames) {
            result.add((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get related hint shadow algorithms by table name.
     *
     * @param tableName table name
     * @return hint shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getRelatedHintShadowAlgorithms(final String tableName) {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        Collection<String> hintShadowAlgorithmNames = shadowTableRules.get(tableName).getHintShadowAlgorithmNames();
        for (String each : hintShadowAlgorithmNames) {
            result.add((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get related column shadow algorithms by table name.
     *
     * @param tableName table name
     * @param shadowOperationType shadow operation type
     * @return column shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Collection<ColumnShadowAlgorithm<Comparable<?>>> getRelatedColumnShadowAlgorithms(final String tableName, final ShadowOperationType shadowOperationType) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        Map<ShadowOperationType, Collection<String>> columnShadowAlgorithmNames = shadowTableRules.get(tableName).getColumnShadowAlgorithmNames();
        Collection<String> names = columnShadowAlgorithmNames.get(shadowOperationType);
        if (Objects.isNull(names)) {
            return result;
        }
        for (String each : names) {
            result.add((ColumnShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get shadow data source mappings.
     *
     * @param tableName table name
     * @return shadow data source rules
     */
    public Map<String, String> getRelatedShadowDataSourceMappings(final String tableName) {
        Map<String, String> result = new LinkedHashMap<>();
        Collection<String> shadowDataSources = shadowTableRules.get(tableName).getShadowDataSources();
        for (String each : shadowDataSources) {
            ShadowDataSourceRule shadowDataSourceRule = shadowDataSourceMappings.get(each);
            result.put(shadowDataSourceRule.getSourceDataSource(), shadowDataSourceRule.getShadowDataSource());
        }
        return result;
    }
    
    /**
     * Get all shadow data source mappings.
     *
     * @return all shadow data source mappings
     */
    public Map<String, String> getAllShadowDataSourceMappings() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Entry<String, ShadowDataSourceRule> entry : shadowDataSourceMappings.entrySet()) {
            ShadowDataSourceRule rule = entry.getValue();
            result.put(rule.getSourceDataSource(), rule.getShadowDataSource());
        }
        return result;
    }
    
    /**
     * Get source data source name.
     *
     * @param actualDataSourceName actual data source name
     * @return source data source name
     */
    public Optional<String> getSourceDataSourceName(final String actualDataSourceName) {
        ShadowDataSourceRule shadowDataSourceRule = shadowDataSourceMappings.get(actualDataSourceName);
        return shadowDataSourceRule == null ? Optional.empty() : Optional.of(shadowDataSourceRule.getSourceDataSource());
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        shadowDataSourceMappings.forEach((key, value) -> result.put(key, createShadowDataSources(value)));
        return result;
    }
    
    private Collection<String> createShadowDataSources(final ShadowDataSourceRule shadowDataSourceRule) {
        Collection<String> result = new LinkedList<>();
        result.add(shadowDataSourceRule.getSourceDataSource());
        result.add(shadowDataSourceRule.getShadowDataSource());
        return result;
    }
    
    @Override
    public String getType() {
        return ShadowRule.class.getSimpleName();
    }
}
