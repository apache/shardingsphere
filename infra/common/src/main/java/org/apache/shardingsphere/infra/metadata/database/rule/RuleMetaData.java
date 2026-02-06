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

import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rule meta data.
 */
public final class RuleMetaData {
    
    private final Map<Class<?>, Collection<?>> ruleCache = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, Optional<ShardingSphereRule>> singleRuleCache = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, Collection<?>> attributeCache = new ConcurrentHashMap<>();
    
    @Getter
    private final Collection<ShardingSphereRule> rules;
    
    public RuleMetaData(final Collection<ShardingSphereRule> rules) {
        this.rules = new CacheInvalidatingCopyOnWriteArrayList(rules);
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
    @SuppressWarnings("unchecked")
    public <T extends ShardingSphereRule> Collection<T> findRules(final Class<T> clazz) {
        Collection<?> result = ruleCache.get(clazz);
        if (null == result) {
            result = ruleCache.computeIfAbsent(clazz, this::computeRules);
        }
        return (Collection<T>) result;
    }
    
    private Collection<? extends ShardingSphereRule> computeRules(final Class<?> clazz) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            if (clazz.isAssignableFrom(each.getClass())) {
                result.add(each);
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
    @SuppressWarnings("unchecked")
    public <T extends ShardingSphereRule> Optional<T> findSingleRule(final Class<T> clazz) {
        Optional<ShardingSphereRule> result = singleRuleCache.get(clazz);
        if (null == result) {
            result = singleRuleCache.computeIfAbsent(clazz, this::computeSingleRule);
        }
        return (Optional<T>) result;
    }
    
    /**
     * Get single rule by class.
     *
     * @param clazz target class
     * @param <T> type of rule
     * @return found single rule
     */
    @SuppressWarnings("unchecked")
    public <T extends ShardingSphereRule> T getSingleRule(final Class<T> clazz) {
        Optional<ShardingSphereRule> shardingSphereRule = singleRuleCache.get(clazz);
        if (null == shardingSphereRule) {
            shardingSphereRule = singleRuleCache.computeIfAbsent(clazz, this::computeSingleRule);
        }
        ShardingSpherePreconditions.checkState(shardingSphereRule.isPresent(),
                () -> new IllegalStateException(String.format("Rule `%s` should have and only have one instance.", clazz.getSimpleName())));
        return (T) shardingSphereRule.get();
    }
    
    @SuppressWarnings("unchecked")
    private Optional<ShardingSphereRule> computeSingleRule(final Class<?> clazz) {
        Collection<ShardingSphereRule> rules = findRules((Class<ShardingSphereRule>) clazz);
        return 1 == rules.size() ? Optional.of(rules.iterator().next()) : Optional.empty();
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
    @SuppressWarnings("unchecked")
    public <T extends RuleAttribute> Collection<T> getAttributes(final Class<T> attributeClass) {
        Collection<?> result = attributeCache.get(attributeClass);
        if (null == result) {
            result = attributeCache.computeIfAbsent(attributeClass, this::computeAttributes);
        }
        return (Collection<T>) result;
    }
    
    private Collection<? extends RuleAttribute> computeAttributes(final Class<?> attributeClass) {
        Collection<RuleAttribute> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            each.getAttributes().findAttribute(attributeClass.asSubclass(RuleAttribute.class)).ifPresent(result::add);
        }
        return result;
    }
    
    /**
     * Find rule attribute.
     *
     * @param attributeClass rule attribute class
     * @param <T> type of rule attributes
     * @return rule attribute
     */
    public <T extends RuleAttribute> Optional<T> findAttribute(final Class<T> attributeClass) {
        Collection<T> attributes = getAttributes(attributeClass);
        return attributes.isEmpty() ? Optional.empty() : Optional.of(attributes.iterator().next());
    }
    
    private final class CacheInvalidatingCopyOnWriteArrayList extends CopyOnWriteArrayList<ShardingSphereRule> {
        
        CacheInvalidatingCopyOnWriteArrayList(final Collection<ShardingSphereRule> rules) {
            super(rules);
        }
        
        private void invalidateCache() {
            ruleCache.clear();
            singleRuleCache.clear();
            attributeCache.clear();
        }
        
        @Override
        public boolean add(final ShardingSphereRule rule) {
            invalidateCache();
            return super.add(rule);
        }
        
        @Override
        public boolean addAll(final Collection<? extends ShardingSphereRule> collection) {
            invalidateCache();
            return super.addAll(collection);
        }
        
        @Override
        public boolean remove(final Object o) {
            invalidateCache();
            return super.remove(o);
        }
        
        @Override
        public boolean removeAll(final Collection<?> objects) {
            invalidateCache();
            return super.removeAll(objects);
        }
        
        @Override
        public boolean removeIf(final Predicate<? super ShardingSphereRule> filter) {
            invalidateCache();
            return super.removeIf(filter);
        }
        
        @Override
        public boolean retainAll(final Collection<?> objects) {
            invalidateCache();
            return super.retainAll(objects);
        }
        
        @Override
        public void clear() {
            invalidateCache();
            super.clear();
        }
    }
}
