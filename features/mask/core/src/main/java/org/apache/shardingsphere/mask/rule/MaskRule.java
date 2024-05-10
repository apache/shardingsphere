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

package org.apache.shardingsphere.mask.rule;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.rule.attribute.MaskTableMapperRuleAttribute;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Mask rule.
 */
public final class MaskRule implements DatabaseRule, PartialRuleUpdateSupported<MaskRuleConfiguration> {
    
    private final AtomicReference<MaskRuleConfiguration> configuration = new AtomicReference<>();
    
    private final ConcurrentHashMap<String, MaskAlgorithm<?, ?>> maskAlgorithms;
    
    private final ConcurrentHashMap<String, MaskTable> tables;
    
    private final AtomicReference<RuleAttributes> attributes = new AtomicReference<>();
    
    @SuppressWarnings("unchecked")
    public MaskRule(final MaskRuleConfiguration ruleConfig) {
        configuration.set(ruleConfig);
        maskAlgorithms = ruleConfig.getMaskAlgorithms().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> TypedSPILoader.getService(MaskAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()),
                        (oldValue, currentValue) -> oldValue, ConcurrentHashMap::new));
        tables = ruleConfig.getTables().stream()
                .collect(Collectors.toMap(each -> each.getName().toLowerCase(), each -> new MaskTable(each, maskAlgorithms), (oldValue, currentValue) -> oldValue, ConcurrentHashMap::new));
        attributes.set(new RuleAttributes(new MaskTableMapperRuleAttribute(tables.keySet())));
    }
    
    /**
     * Find mask table.
     *
     * @param tableName table name
     * @return found mask table
     */
    public Optional<MaskTable> findMaskTable(final String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }
    
    @Override
    public RuleAttributes getAttributes() {
        return attributes.get();
    }
    
    @Override
    public MaskRuleConfiguration getConfiguration() {
        return configuration.get();
    }
    
    @Override
    public void updateConfiguration(final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        configuration.set(toBeUpdatedRuleConfig);
    }
    
    @Override
    public boolean partialUpdate(final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        Collection<String> toBeAddedTableNames = toBeUpdatedRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
        toBeAddedTableNames.removeAll(tables.keySet());
        if (!toBeAddedTableNames.isEmpty()) {
            toBeAddedTableNames.forEach(each -> addTableRule(each, toBeUpdatedRuleConfig));
            attributes.set(new RuleAttributes(new MaskTableMapperRuleAttribute(tables.keySet())));
            return true;
        }
        Collection<String> toBeRemovedTableNames = new HashSet<>(tables.keySet());
        toBeRemovedTableNames.removeAll(toBeUpdatedRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList()));
        if (!toBeRemovedTableNames.isEmpty()) {
            toBeRemovedTableNames.stream().map(String::toLowerCase).forEach(tables::remove);
            attributes.set(new RuleAttributes(new MaskTableMapperRuleAttribute(tables.keySet())));
            // TODO check and remove unused INLINE mask algorithms
            return true;
        }
        // TODO Process update table
        // TODO Process CRUD mask algorithms
        return false;
    }
    
    private void addTableRule(final String tableName, final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        MaskTableRuleConfiguration tableRuleConfig = getTableRuleConfiguration(tableName, toBeUpdatedRuleConfig);
        for (Entry<String, AlgorithmConfiguration> entry : toBeUpdatedRuleConfig.getMaskAlgorithms().entrySet()) {
            maskAlgorithms.computeIfAbsent(entry.getKey(), key -> TypedSPILoader.getService(MaskAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()));
        }
        tables.put(tableName.toLowerCase(), new MaskTable(tableRuleConfig, maskAlgorithms));
    }
    
    private MaskTableRuleConfiguration getTableRuleConfiguration(final String tableName, final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        Optional<MaskTableRuleConfiguration> result = toBeUpdatedRuleConfig.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst();
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
}
