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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.rule.attribute.MaskTableMapperRuleAttribute;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Mask rule.
 */
public final class MaskRule implements DatabaseRule, PartialRuleUpdateSupported<MaskRuleConfiguration> {
    
    private final AtomicReference<MaskRuleConfiguration> configuration = new AtomicReference<>();
    
    private final Map<String, MaskAlgorithm<?, ?>> maskAlgorithms = new CaseInsensitiveMap<>(Collections.emptyMap(), new ConcurrentHashMap<>());
    
    private final Map<String, MaskTable> tables = new CaseInsensitiveMap<>(Collections.emptyMap(), new ConcurrentHashMap<>());
    
    private final AtomicReference<RuleAttributes> attributes = new AtomicReference<>();
    
    public MaskRule(final MaskRuleConfiguration ruleConfig) {
        configuration.set(ruleConfig);
        ruleConfig.getMaskAlgorithms().forEach((key, value) -> maskAlgorithms.put(key, TypedSPILoader.getService(MaskAlgorithm.class, value.getType(), value.getProps())));
        ruleConfig.getTables().forEach(each -> tables.put(each.getName(), new MaskTable(each, maskAlgorithms)));
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
        handleAddedMaskAlgorithm(toBeUpdatedRuleConfig);
        handleRemovedMaskAlgorithm(toBeUpdatedRuleConfig);
        Collection<String> toBeUpdatedTablesNames = toBeUpdatedRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        Collection<String> toBeRemovedTableNames = tables.keySet().stream().filter(each -> !toBeUpdatedTablesNames.contains(each)).collect(Collectors.toList());
        if (!toBeRemovedTableNames.isEmpty()) {
            toBeRemovedTableNames.forEach(tables::remove);
        }
        for (MaskTableRuleConfiguration maskTableRuleConfiguration : toBeUpdatedRuleConfig.getTables()) {
            tables.put(maskTableRuleConfiguration.getName(), new MaskTable(maskTableRuleConfiguration, maskAlgorithms));
            attributes.set(new RuleAttributes(new MaskTableMapperRuleAttribute(tables.keySet())));
        }
        return false;
    }
    
    private void handleAddedMaskAlgorithm(final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        toBeUpdatedRuleConfig.getMaskAlgorithms().entrySet().stream()
                .filter(entry -> !maskAlgorithms.containsKey(entry.getKey()))
                .forEach(entry -> maskAlgorithms.computeIfAbsent(entry.getKey(), key -> TypedSPILoader.getService(MaskAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps())));
    }
    
    private void handleRemovedMaskAlgorithm(final MaskRuleConfiguration toBeUpdatedRuleConfig) {
        maskAlgorithms.entrySet().stream()
                .filter(entry -> !toBeUpdatedRuleConfig.getMaskAlgorithms().containsKey(entry.getKey()))
                .forEach(entry -> maskAlgorithms.remove(entry.getKey()));
    }
    
    @Override
    public int getOrder() {
        return MaskOrder.ORDER;
    }
}
