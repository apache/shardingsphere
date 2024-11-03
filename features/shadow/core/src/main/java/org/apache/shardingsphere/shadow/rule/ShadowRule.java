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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.rule.attribute.ShadowDataSourceMapperRuleAttribute;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Databases shadow rule.
 */
public final class ShadowRule implements DatabaseRule {
    
    @Getter
    private final ShadowRuleConfiguration configuration;
    
    private final Map<String, ShadowDataSourceRule> shadowDataSourceRules = new LinkedHashMap<>();
    
    private final Map<String, ShadowTableRule> shadowTableRules = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, ShadowAlgorithm> shadowAlgorithms = new LinkedHashMap<>();
    
    private final Collection<String> hintShadowAlgorithmNames = new LinkedList<>();
    
    private final ShadowAlgorithm defaultShadowAlgorithm;
    
    @Getter
    private final RuleAttributes attributes;
    
    public ShadowRule(final ShadowRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        initDataSourceRules(ruleConfig.getDataSources());
        initShadowAlgorithms(ruleConfig.getShadowAlgorithms());
        defaultShadowAlgorithm = shadowAlgorithms.get(ruleConfig.getDefaultShadowAlgorithmName());
        if (defaultShadowAlgorithm instanceof HintShadowAlgorithm<?>) {
            hintShadowAlgorithmNames.add(ruleConfig.getDefaultShadowAlgorithmName());
        }
        initTableRules(ruleConfig.getTables());
        attributes = new RuleAttributes(new ShadowDataSourceMapperRuleAttribute(shadowDataSourceRules));
    }
    
    private void initDataSourceRules(final Collection<ShadowDataSourceConfiguration> dataSources) {
        dataSources.forEach(each -> shadowDataSourceRules.put(each.getName(), new ShadowDataSourceRule(each.getProductionDataSourceName(), each.getShadowDataSourceName())));
    }
    
    private void initShadowAlgorithms(final Map<String, AlgorithmConfiguration> shadowAlgorithmConfigs) {
        shadowAlgorithmConfigs.forEach((key, value) -> {
            ShadowAlgorithm algorithm = TypedSPILoader.getService(ShadowAlgorithm.class, value.getType(), value.getProps());
            if (algorithm instanceof HintShadowAlgorithm<?>) {
                hintShadowAlgorithmNames.add(key);
            }
            shadowAlgorithms.put(key, algorithm);
        });
    }
    
    private void initTableRules(final Map<String, ShadowTableConfiguration> tables) {
        tables.forEach((key, value) -> shadowTableRules.put(key, new ShadowTableRule(key, value.getDataSourceNames(), value.getShadowAlgorithmNames(), shadowAlgorithms)));
    }
    
    /**
     * Get default shadow algorithm.
     *
     * @return shadow algorithm
     */
    @HighFrequencyInvocation
    public Optional<ShadowAlgorithm> getDefaultShadowAlgorithm() {
        return Optional.ofNullable(defaultShadowAlgorithm);
    }
    
    /**
     * Filter shadow tables.
     *
     * @param tableNames to be filtered table names
     * @return filtered shadow tables
     */
    @HighFrequencyInvocation
    public Collection<String> filterShadowTables(final Collection<String> tableNames) {
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
    @HighFrequencyInvocation
    public Collection<String> getAllShadowTableNames() {
        return shadowTableRules.keySet();
    }
    
    /**
     * Get related hint shadow algorithms.
     *
     * @return related hint shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getAllHintShadowAlgorithms() {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (String each : hintShadowAlgorithmNames) {
            result.add((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get hint shadow algorithms by table name.
     *
     * @param tableName table name
     * @return hint shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getHintShadowAlgorithms(final String tableName) {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        Collection<String> hintShadowAlgorithmNames = shadowTableRules.get(tableName).getHintShadowAlgorithmNames();
        for (String each : hintShadowAlgorithmNames) {
            result.add((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get shadow algorithms.
     *
     * @param shadowOperationType shadow operation type
     * @param tableName table name
     * @param shadowColumnName shadow column name
     * @return shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<ColumnShadowAlgorithm<Comparable<?>>> getShadowAlgorithms(final ShadowOperationType shadowOperationType, final String tableName, final String shadowColumnName) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (ShadowAlgorithmNameRule each : shadowTableRules.get(tableName).getColumnShadowAlgorithmNames().getOrDefault(shadowOperationType, Collections.emptyList())) {
            if (shadowColumnName.equals(each.getShadowColumnName())) {
                result.add((ColumnShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each.getShadowAlgorithmName()));
            }
        }
        return result;
    }
    
    /**
     * Get shadow column names.
     *
     * @param shadowOperationType shadow operation type
     * @param tableName table name
     * @return got shadow column names
     */
    @HighFrequencyInvocation
    public Collection<String> getShadowColumnNames(final ShadowOperationType shadowOperationType, final String tableName) {
        Collection<String> result = new LinkedList<>();
        for (ShadowAlgorithmNameRule each : shadowTableRules.get(tableName).getColumnShadowAlgorithmNames().getOrDefault(shadowOperationType, Collections.emptyList())) {
            result.add(each.getShadowColumnName());
        }
        return result;
    }
    
    /**
     * Get shadow data source mappings.
     *
     * @param tableName table name
     * @return shadow data source mappings
     */
    @HighFrequencyInvocation
    public Map<String, String> getShadowDataSourceMappings(final String tableName) {
        Map<String, String> result = new LinkedHashMap<>(shadowDataSourceRules.size(), 1F);
        for (String each : shadowTableRules.get(tableName).getShadowDataSources()) {
            ShadowDataSourceRule dataSourceRule = shadowDataSourceRules.get(each);
            result.put(dataSourceRule.getProductionDataSource(), dataSourceRule.getShadowDataSource());
        }
        return result;
    }
    
    /**
     * Get all shadow data source mappings.
     *
     * @return all shadow data source mappings
     */
    @HighFrequencyInvocation
    public Map<String, String> getAllShadowDataSourceMappings() {
        Map<String, String> result = new LinkedHashMap<>(shadowDataSourceRules.size(), 1F);
        for (Entry<String, ShadowDataSourceRule> entry : shadowDataSourceRules.entrySet()) {
            ShadowDataSourceRule dataSourceRule = entry.getValue();
            result.put(dataSourceRule.getProductionDataSource(), dataSourceRule.getShadowDataSource());
        }
        return result;
    }
    
    /**
     * Find production data source name.
     *
     * @param logicDataSourceName logic data source name
     * @return found production data source name
     */
    @HighFrequencyInvocation
    public Optional<String> findProductionDataSourceName(final String logicDataSourceName) {
        ShadowDataSourceRule dataSourceRule = shadowDataSourceRules.get(logicDataSourceName);
        return null == dataSourceRule ? Optional.empty() : Optional.of(dataSourceRule.getProductionDataSource());
    }
}
