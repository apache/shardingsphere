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

package org.apache.shardingsphere.mode.node.rule.tuple;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeGenerator;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleKeyListNameGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * YAML rule repository tuple swapper engine.
 */
@RequiredArgsConstructor
public final class YamlRuleRepositoryTupleSwapperEngine {
    
    private final String databaseName;
    
    public YamlRuleRepositoryTupleSwapperEngine() {
        this(NodePathPattern.IDENTIFIER);
    }
    
    /**
     * Swap to rule repository tuples.
     *
     * @param yamlRuleConfig YAML rule configuration to be swapped
     * @return rule repository tuples
     */
    public Collection<RuleRepositoryTuple> swapToTuples(final YamlRuleConfiguration yamlRuleConfig) {
        RuleRepositoryTupleEntity entity = yamlRuleConfig.getClass().getAnnotation(RuleRepositoryTupleEntity.class);
        if (null == entity) {
            return Collections.emptyList();
        }
        String ruleType = entity.value();
        if (entity.leaf()) {
            return Collections.singleton(new RuleRepositoryTuple(NodePathGenerator.toPath(new GlobalRuleNodePath(ruleType), false), YamlEngine.marshal(yamlRuleConfig)));
        }
        Collection<RuleRepositoryTuple> result = new LinkedList<>();
        for (Field each : YamlRuleConfigurationFieldUtil.getFields(yamlRuleConfig.getClass())) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            result.addAll(swapToTuples(ruleType, yamlRuleConfig, each));
            each.setAccessible(isAccessible);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private Collection<RuleRepositoryTuple> swapToTuples(final String ruleType, final YamlRuleConfiguration yamlRuleConfig, final Field field) {
        Object fieldValue = field.get(yamlRuleConfig);
        if (null == fieldValue) {
            return Collections.emptyList();
        }
        String tupleName = YamlRuleConfigurationFieldUtil.getTupleItemName(field);
        RuleRepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator = field.getAnnotation(RuleRepositoryTupleKeyListNameGenerator.class);
        if (null != tupleKeyListNameGenerator && fieldValue instanceof Collection) {
            Collection<RuleRepositoryTuple> result = new LinkedList<>();
            for (Object value : (Collection) fieldValue) {
                String tupleKeyName = tupleKeyListNameGenerator.value().getConstructor().newInstance().generate(value);
                String tupleKey = NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(tupleName, tupleKeyName)), false);
                result.add(new RuleRepositoryTuple(tupleKey, value.toString()));
            }
            return result;
        }
        if (fieldValue instanceof Map) {
            Collection<RuleRepositoryTuple> result = new LinkedList<>();
            for (Object entry : ((Map) fieldValue).entrySet()) {
                String tupleKey = NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(tupleName, ((Entry) entry).getKey().toString())), false);
                result.add(new RuleRepositoryTuple(tupleKey, YamlEngine.marshal(((Entry) entry).getValue())));
            }
            return result;
        }
        String tupleKey = NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(tupleName)), false);
        if (fieldValue instanceof Collection) {
            return ((Collection) fieldValue).isEmpty() ? Collections.emptyList() : Collections.singleton(new RuleRepositoryTuple(tupleKey, YamlEngine.marshal(fieldValue)));
        }
        if (fieldValue instanceof String) {
            return ((String) fieldValue).isEmpty() ? Collections.emptyList() : Collections.singleton(new RuleRepositoryTuple(tupleKey, fieldValue.toString()));
        }
        if (fieldValue instanceof Boolean || fieldValue instanceof Integer || fieldValue instanceof Long) {
            return Collections.singleton(new RuleRepositoryTuple(tupleKey, fieldValue.toString()));
        }
        if (fieldValue instanceof Enum) {
            return Collections.singleton(new RuleRepositoryTuple(tupleKey, ((Enum) fieldValue).name()));
        }
        return Collections.singleton(new RuleRepositoryTuple(tupleKey, YamlEngine.marshal(fieldValue)));
    }
    
    /**
     * Swap to YAML global rule configuration.
     *
     * @param ruleType rule type
     * @param ruleContent rule content
     * @return global rule configuration
     * @throws IllegalArgumentException throw if rule configuration not found
     */
    @SuppressWarnings("rawtypes")
    public YamlRuleConfiguration swapToYamlGlobalRuleConfiguration(final String ruleType, final String ruleContent) {
        for (YamlRuleConfigurationSwapper each : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            RuleRepositoryTupleEntity entity = yamlRuleConfigClass.getAnnotation(RuleRepositoryTupleEntity.class);
            if (null != entity && ruleType.equals(entity.value())) {
                return YamlEngine.unmarshal(ruleContent, yamlRuleConfigClass);
            }
        }
        throw new IllegalArgumentException(String.format("Can not find rule configuration with type: %s", ruleType));
    }
    
    /**
     * Swap to YAML database rule configuration.
     *
     * @param ruleType rule type
     * @param tuples rule repository tuples
     * @return database rule configuration
     * @throws IllegalArgumentException throw if rule configuration not found
     */
    @SuppressWarnings("rawtypes")
    public YamlRuleConfiguration swapToYamlDatabaseRuleConfiguration(final String ruleType, final Collection<RuleRepositoryTuple> tuples) {
        for (YamlRuleConfigurationSwapper each : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            RuleRepositoryTupleEntity entity = yamlRuleConfigClass.getAnnotation(RuleRepositoryTupleEntity.class);
            if (null != entity && entity.value().equals(ruleType)) {
                return swapToYamlDatabaseRuleConfiguration(yamlRuleConfigClass, tuples)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("Can not find rule configuration with type: %s", ruleType)));
            }
        }
        throw new IllegalArgumentException(String.format("Can not find rule configuration with type: %s", ruleType));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToYamlDatabaseRuleConfiguration(final Class<? extends YamlRuleConfiguration> toBeSwappedType, final Collection<RuleRepositoryTuple> tuples) {
        Collection<Field> fields = YamlRuleConfigurationFieldUtil.getFields(toBeSwappedType);
        YamlRuleConfiguration yamlRuleConfig = toBeSwappedType.getConstructor().newInstance();
        DatabaseRuleNode databaseRuleNode = DatabaseRuleNodeGenerator.generate(toBeSwappedType);
        for (RuleRepositoryTuple each : tuples) {
            if (!Strings.isNullOrEmpty(each.getContent())) {
                setFieldValue(yamlRuleConfig, fields, databaseRuleNode.getRuleType(), each);
            }
        }
        return Optional.of(yamlRuleConfig);
    }
    
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Collection<Field> fields, final String ruleType, final RuleRepositoryTuple tuple) {
        for (Field each : fields) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            setFieldValue(yamlRuleConfig, each, ruleType, tuple);
            each.setAccessible(isAccessible);
        }
    }
    
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Field field, final String ruleType, final RuleRepositoryTuple tuple) {
        String itemType = YamlRuleConfigurationFieldUtil.getTupleItemName(field);
        if (isNamedItem(field, field.getAnnotation(RuleRepositoryTupleKeyListNameGenerator.class))) {
            setNamedItemFieldValue(yamlRuleConfig, ruleType, tuple, itemType, field);
        } else {
            setUniqueItemFieldValue(yamlRuleConfig, ruleType, tuple, itemType, field);
        }
    }
    
    private boolean isNamedItem(final Field field, final RuleRepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator) {
        return field.getType().equals(Map.class) || null != tupleKeyListNameGenerator && field.getType().equals(Collection.class);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setNamedItemFieldValue(final YamlRuleConfiguration yamlRuleConfig, final String ruleType, final RuleRepositoryTuple tuple, final String itemType, final Field field) {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(itemType, NodePathPattern.QUALIFIED_IDENTIFIER));
        Optional<String> itemValue = NodePathSearcher.find(tuple.getPath(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 1));
        if (!itemValue.isPresent()) {
            return;
        }
        Object fieldValue = field.get(yamlRuleConfig);
        if (field.getType().equals(Map.class)) {
            if (null == fieldValue) {
                field.set(yamlRuleConfig, new HashMap<>());
            }
            fieldValue = field.get(yamlRuleConfig);
            Class<?> valueClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            ((Map) fieldValue).put(itemValue.get(), YamlEngine.unmarshal(tuple.getContent(), valueClass));
        } else {
            if (null == fieldValue) {
                field.set(yamlRuleConfig, new LinkedList<>());
            }
            fieldValue = field.get(yamlRuleConfig);
            ((Collection) fieldValue).add(tuple.getContent());
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setUniqueItemFieldValue(final YamlRuleConfiguration yamlRuleConfig, final String ruleType, final RuleRepositoryTuple tuple, final String itemType, final Field field) {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(itemType));
        if (!NodePathSearcher.isMatchedPath(tuple.getPath(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 1))) {
            return;
        }
        if (field.getType().equals(Collection.class)) {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(tuple.getContent(), List.class));
        } else if (field.getType().equals(String.class)) {
            field.set(yamlRuleConfig, tuple.getContent());
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            field.set(yamlRuleConfig, Boolean.parseBoolean(tuple.getContent()));
        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            field.set(yamlRuleConfig, Integer.parseInt(tuple.getContent()));
        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
            field.set(yamlRuleConfig, Long.parseLong(tuple.getContent()));
        } else {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(tuple.getContent(), field.getType()));
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends YamlRuleConfiguration> getYamlRuleConfigurationClass(final YamlRuleConfigurationSwapper swapper) {
        return (Class<? extends YamlRuleConfiguration>) ((ParameterizedType) swapper.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
