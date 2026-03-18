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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.rule.attribute.ShadowDataSourceMapperRuleAttribute;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Databases shadow rule.
 */
public final class ShadowRule implements DatabaseRule {
    
    @Getter
    private final ShadowRuleConfiguration configuration;
    
    private final Map<String, ShadowAlgorithm> shadowAlgorithms;
    
    private final ShadowAlgorithm defaultShadowAlgorithm;
    
    private final Map<String, ShadowDataSourceRule> dataSourceRules;
    
    private final Map<String, ShadowTableRule> tableRules;
    
    @Getter
    private final RuleAttributes attributes;
    
    public ShadowRule(final ShadowRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        shadowAlgorithms = createShadowAlgorithms(ruleConfig.getShadowAlgorithms());
        defaultShadowAlgorithm = shadowAlgorithms.get(ruleConfig.getDefaultShadowAlgorithmName());
        dataSourceRules = createDataSourceRules(ruleConfig.getDataSources());
        tableRules = createTableRules(ruleConfig.getTables());
        attributes = new RuleAttributes(new ShadowDataSourceMapperRuleAttribute(dataSourceRules));
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms(final Map<String, AlgorithmConfiguration> shadowAlgorithmConfigs) {
        return shadowAlgorithmConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> TypedSPILoader.getService(ShadowAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private Map<String, ShadowDataSourceRule> createDataSourceRules(final Collection<ShadowDataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.stream().collect(Collectors.toMap(ShadowDataSourceConfiguration::getName,
                each -> new ShadowDataSourceRule(each.getProductionDataSourceName(), each.getShadowDataSourceName()), (oldValue, currentValue) -> currentValue, CaseInsensitiveMap::new));
    }
    
    private Map<String, ShadowTableRule> createTableRules(final Map<String, ShadowTableConfiguration> tableConfigs) {
        return tableConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
                entry -> new ShadowTableRule(entry.getKey(), entry.getValue().getDataSourceNames(), entry.getValue().getShadowAlgorithmNames(), shadowAlgorithms),
                (oldValue, currentValue) -> currentValue, CaseInsensitiveMap::new));
    }
    
    /**
     * Whether contains shadow algorithm.
     *
     * @param algorithmName algorithm name
     * @return contains shadow algorithm or not
     */
    public boolean containsShadowAlgorithm(final String algorithmName) {
        return shadowAlgorithms.containsKey(algorithmName);
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
            if (tableRules.containsKey(each)) {
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
        return tableRules.keySet();
    }
    
    /**
     * Get all hint shadow algorithms.
     *
     * @return all hint shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getAllHintShadowAlgorithms() {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (Entry<String, ShadowAlgorithm> entry : shadowAlgorithms.entrySet()) {
            if (entry.getValue() instanceof HintShadowAlgorithm) {
                result.add((HintShadowAlgorithm<Comparable<?>>) entry.getValue());
            }
        }
        return result;
    }
    
    /**
     * Get hint shadow algorithms.
     *
     * @param tableName table name
     * @return hint shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<HintShadowAlgorithm<Comparable<?>>> getHintShadowAlgorithms(final String tableName) {
        Collection<HintShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (String each : tableRules.get(tableName).getHintShadowAlgorithmNames()) {
            result.add((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each));
        }
        return result;
    }
    
    /**
     * Get column shadow algorithms.
     *
     * @param operationType shadow operation type
     * @param tableName table name
     * @param shadowColumnName shadow column name
     * @return column shadow algorithms
     */
    @HighFrequencyInvocation
    @SuppressWarnings("unchecked")
    public Collection<ColumnShadowAlgorithm<Comparable<?>>> getColumnShadowAlgorithms(final ShadowOperationType operationType, final String tableName, final String shadowColumnName) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> result = new LinkedList<>();
        for (ShadowAlgorithmNameRule each : tableRules.get(tableName).getColumnShadowAlgorithmNames().getOrDefault(operationType, Collections.emptyList())) {
            if (shadowColumnName.equals(each.getShadowColumnName())) {
                result.add((ColumnShadowAlgorithm<Comparable<?>>) shadowAlgorithms.get(each.getShadowAlgorithmName()));
            }
        }
        return result;
    }
    
    /**
     * Get shadow column names.
     *
     * @param operationType shadow operation type
     * @param tableName table name
     * @return got shadow column names
     */
    @HighFrequencyInvocation
    public Collection<String> getShadowColumnNames(final ShadowOperationType operationType, final String tableName) {
        Collection<String> result = new LinkedList<>();
        for (ShadowAlgorithmNameRule each : tableRules.get(tableName).getColumnShadowAlgorithmNames().getOrDefault(operationType, Collections.emptyList())) {
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
        Map<String, String> result = new LinkedHashMap<>(dataSourceRules.size(), 1F);
        for (String each : tableRules.get(tableName).getLogicDataSourceNames()) {
            ShadowDataSourceRule dataSourceRule = dataSourceRules.get(each);
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
        Map<String, String> result = new LinkedHashMap<>(dataSourceRules.size(), 1F);
        for (Entry<String, ShadowDataSourceRule> entry : dataSourceRules.entrySet()) {
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
        ShadowDataSourceRule dataSourceRule = dataSourceRules.get(logicDataSourceName);
        return null == dataSourceRule ? Optional.empty() : Optional.of(dataSourceRule.getProductionDataSource());
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
}
