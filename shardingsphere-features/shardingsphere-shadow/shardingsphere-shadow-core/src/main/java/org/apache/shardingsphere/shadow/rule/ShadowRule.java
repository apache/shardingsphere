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
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
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
    
    private final boolean enable;
    
    private final Map<String, ShadowDataSourceRule> shadowDataSourceMappings = new LinkedHashMap<>();
    
    private final Map<String, ShadowAlgorithm> shadowAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, ShadowTableRule> shadowTableRules = new LinkedHashMap<>();
    
    public ShadowRule(final ShadowRuleConfiguration shadowRuleConfig) {
        enable = shadowRuleConfig.isEnable();
        if (enable) {
            initShadowDataSourceMappings(shadowRuleConfig.getDataSources());
            initShadowAlgorithmConfigurations(shadowRuleConfig.getShadowAlgorithms());
            initShadowTableRules(shadowRuleConfig.getTables());
        }
    }
    
    public ShadowRule(final AlgorithmProvidedShadowRuleConfiguration shadowRuleConfig) {
        enable = shadowRuleConfig.isEnable();
        if (enable) {
            initShadowDataSourceMappings(shadowRuleConfig.getDataSources());
            initShadowAlgorithms(shadowRuleConfig.getShadowAlgorithms());
            initShadowTableRules(shadowRuleConfig.getTables());
        }
    }
    
    private void initShadowAlgorithmConfigurations(final Map<String, ShardingSphereAlgorithmConfiguration> shadowAlgorithmConfigurations) {
        shadowAlgorithmConfigurations.forEach((key, value) -> shadowAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ShadowAlgorithm.class)));
    }
    
    private void initShadowTableRules(final Map<String, ShadowTableConfiguration> tables) {
        tables.forEach((key, value) -> shadowTableRules.put(key, new ShadowTableRule(key, value.getDataSourceNames(), value.getShadowAlgorithmNames(), shadowAlgorithms)));
    }
    
    private void initShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        this.shadowAlgorithms.putAll(shadowAlgorithms);
    }
    
    private void initShadowDataSourceMappings(final Map<String, ShadowDataSourceConfiguration> dataSources) {
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
     * Get related note shadow algorithms.
     *
     * @return related note shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Optional<Collection<NoteShadowAlgorithm<Comparable<?>>>> getAllNoteShadowAlgorithms() {
        Collection<NoteShadowAlgorithm<Comparable<?>>> result = shadowAlgorithms.values().stream().filter(each -> each instanceof NoteShadowAlgorithm<?>)
                .map(each -> (NoteShadowAlgorithm<Comparable<?>>) each).collect(Collectors.toCollection(LinkedList::new));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    /**
     * Get related note shadow algorithms by table name.
     *
     * @param tableName table name
     * @return note shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Optional<Collection<NoteShadowAlgorithm<Comparable<?>>>> getRelatedNoteShadowAlgorithms(final String tableName) {
        Collection<NoteShadowAlgorithm<Comparable<?>>> result = shadowTableRules.get(tableName).getNoteShadowAlgorithmNames().stream().map(shadowAlgorithms::get)
                .filter(shadowAlgorithm -> !Objects.isNull(shadowAlgorithm)).map(shadowAlgorithm -> (NoteShadowAlgorithm<Comparable<?>>) shadowAlgorithm)
                .collect(Collectors.toCollection(LinkedList::new));
        return result.isEmpty() ? Optional.of(result) : Optional.of(result);
    }
    
    /**
     * Get related column shadow algorithms by table name.
     *
     * @param tableName table name
     * @param shadowOperationType shadow operation type
     * @return column shadow algorithms
     */
    @SuppressWarnings("unchecked")
    public Optional<Collection<ColumnShadowAlgorithm<Comparable<?>>>> getRelatedColumnShadowAlgorithms(final String tableName, final ShadowOperationType shadowOperationType) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> result = shadowTableRules.get(tableName).getColumnShadowAlgorithmNames().get(shadowOperationType).stream().map(shadowAlgorithms::get)
                .filter(shadowAlgorithm -> !Objects.isNull(shadowAlgorithm)).map(shadowAlgorithm -> (ColumnShadowAlgorithm<Comparable<?>>) shadowAlgorithm)
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
        Map<String, String> result = shadowTableRules.get(tableName).getShadowDataSources().stream().map(shadowDataSourceMappings::get).filter(Objects::nonNull)
                .collect(Collectors.toMap(ShadowDataSourceRule::getSourceDataSource, ShadowDataSourceRule::getShadowDataSource, (a, b) -> b, LinkedHashMap::new));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    /**
     * Get all shadow data source mappings.
     *
     * @return all shadow data source mappings
     */
    public Map<String, String> getAllShadowDataSourceMappings() {
        return shadowDataSourceMappings.values().stream().collect(Collectors.toMap(ShadowDataSourceRule::getSourceDataSource, ShadowDataSourceRule::getShadowDataSource, (key, value) -> value,
                LinkedHashMap::new));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        return shadowDataSourceMappings.values().stream().collect(Collectors.toMap(ShadowDataSourceRule::getSourceDataSource, each ->
                Collections.singletonList(each.getShadowDataSource()), (key, value) -> value, () -> new HashMap<>(shadowDataSourceMappings.size(), 1)));
    }
    
    @Override
    public String getType() {
        return ShadowRule.class.getSimpleName();
    }
}
