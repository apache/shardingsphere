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

package org.apache.shardingsphere.infra.metadata.database.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Rule meta data.
 */
@Getter
public final class RuleMetaData {
    
    private final Collection<ShardingSphereRule> rules;
    
    public RuleMetaData(final Collection<ShardingSphereRule> rules) {
        this.rules = new CopyOnWriteArrayList<>(rules);
    }
    
    /**
     * Get rule configurations.
     *
     * @return got rule configurations
     */
    public Collection<RuleConfiguration> getConfigurations() {
        return rules.stream().map(ShardingSphereRule::getConfiguration).collect(Collectors.toList());
    }
    
    /**
     * Find rules by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found rules
     */
    public <T extends ShardingSphereRule> Collection<T> findRules(final Class<T> clazz) {
        Collection<T> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            if (clazz.isAssignableFrom(each.getClass())) {
                result.add(clazz.cast(each));
            }
        }
        return result;
    }
    
    /**
     * Find single rule by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found single rule
     */
    public <T extends ShardingSphereRule> Optional<T> findSingleRule(final Class<T> clazz) {
        Collection<T> foundRules = findRules(clazz);
        return foundRules.isEmpty() ? Optional.empty() : Optional.of(foundRules.iterator().next());
    }
    
    /**
     * Get single rule by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found single rule
     */
    public <T extends ShardingSphereRule> T getSingleRule(final Class<T> clazz) {
        Collection<T> foundRules = findRules(clazz);
        Preconditions.checkState(1 == foundRules.size(), "Rule `%s` should have and only have one instance.", clazz.getSimpleName());
        return foundRules.iterator().next();
    }
    
    /**
     * Get in used storage units name and used rule classes map.
     *
     * @return in used storage units name and used rule classes map
     */
    public Map<String, Collection<Class<? extends ShardingSphereRule>>> getInUsedStorageUnitNameAndRulesMap() {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> result = new LinkedHashMap<>();
        for (ShardingSphereRule each : rules) {
            Collection<String> inUsedStorageUnitNames = getInUsedStorageUnitNames(each);
            if (!inUsedStorageUnitNames.isEmpty()) {
                mergeInUsedStorageUnitNameAndRules(result, getInUsedStorageUnitNameAndRulesMap(each, inUsedStorageUnitNames));
            }
        }
        return result;
    }
    
    private Map<String, Collection<Class<? extends ShardingSphereRule>>> getInUsedStorageUnitNameAndRulesMap(final ShardingSphereRule rule, final Collection<String> inUsedStorageUnitNames) {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> result = new LinkedHashMap<>();
        for (String each : inUsedStorageUnitNames) {
            result.computeIfAbsent(each, unused -> new LinkedHashSet<>()).add(rule.getClass());
        }
        return result;
    }
    
    private Collection<String> getInUsedStorageUnitNames(final ShardingSphereRule rule) {
        Optional<DataSourceMapperRuleAttribute> dataSourceMapperRuleAttribute = rule.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class);
        if (dataSourceMapperRuleAttribute.isPresent()) {
            return getInUsedStorageUnitNames(dataSourceMapperRuleAttribute.get());
        }
        Optional<DataNodeRuleAttribute> dataNodeRuleAttribute = rule.getAttributes().findAttribute(DataNodeRuleAttribute.class);
        if (dataNodeRuleAttribute.isPresent()) {
            return getInUsedStorageUnitNames(dataNodeRuleAttribute.get());
        }
        return Collections.emptyList();
    }
    
    private Collection<String> getInUsedStorageUnitNames(final DataSourceMapperRuleAttribute ruleAttribute) {
        return ruleAttribute.getDataSourceMapper().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    private Collection<String> getInUsedStorageUnitNames(final DataNodeRuleAttribute ruleAttribute) {
        return ruleAttribute.getAllDataNodes().values().stream().flatMap(each -> each.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()).stream()).collect(Collectors.toSet());
    }
    
    private void mergeInUsedStorageUnitNameAndRules(final Map<String, Collection<Class<? extends ShardingSphereRule>>> storageUnitNameAndRules,
                                                    final Map<String, Collection<Class<? extends ShardingSphereRule>>> toBeMergedStorageUnitNameAndRules) {
        for (Entry<String, Collection<Class<? extends ShardingSphereRule>>> entry : toBeMergedStorageUnitNameAndRules.entrySet()) {
            if (storageUnitNameAndRules.containsKey(entry.getKey())) {
                for (Class<? extends ShardingSphereRule> each : entry.getValue()) {
                    if (!storageUnitNameAndRules.get(entry.getKey()).contains(each)) {
                        storageUnitNameAndRules.get(entry.getKey()).add(each);
                    }
                }
            } else {
                storageUnitNameAndRules.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Get rule attributes.
     *
     * @param attributeClass rule attribute class
     * @param <T> type of rule attributes
     * @return rule attributes
     */
    public <T extends RuleAttribute> Collection<T> getAttributes(final Class<T> attributeClass) {
        Collection<T> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            each.getAttributes().findAttribute(attributeClass).ifPresent(result::add);
        }
        return result;
    }
    
    /**
     * Get rule attributes.
     *
     * @param attributeClass rule attribute class
     * @param <T> type of rule attributes
     * @return rule attributes
     */
    public <T extends RuleAttribute> Optional<T> findAttribute(final Class<T> attributeClass) {
        for (ShardingSphereRule each : rules) {
            if (each.getAttributes().findAttribute(attributeClass).isPresent()) {
                return each.getAttributes().findAttribute(attributeClass);
            }
        }
        return Optional.empty();
    }
}
