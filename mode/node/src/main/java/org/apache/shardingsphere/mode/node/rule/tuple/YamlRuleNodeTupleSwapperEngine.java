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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.config.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeGenerator;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleKeyListNameGenerator;

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
 * YAML rule node tuple swapper engine.
 */
public final class YamlRuleNodeTupleSwapperEngine {
    
    /**
     * Swap to rule node tuple.
     *
     * @param yamlGlobalRuleConfig global YAML rule configuration to be swapped
     * @return rule node tuple
     */
    public RuleNodeTuple swapToTuple(final YamlGlobalRuleConfiguration yamlGlobalRuleConfig) {
        RuleNodeTupleEntity entity = yamlGlobalRuleConfig.getClass().getAnnotation(RuleNodeTupleEntity.class);
        Preconditions.checkNotNull(entity);
        Preconditions.checkArgument(entity.leaf());
        return new RuleNodeTuple(new GlobalRuleNodePath(entity.value()), YamlEngine.marshal(yamlGlobalRuleConfig));
    }
    
    /**
     * Swap to rule node tuples.
     *
     * @param databaseName database name
     * @param yamlRuleConfig YAML rule configuration to be swapped
     * @return rule node tuples
     */
    public Collection<RuleNodeTuple> swapToTuples(final String databaseName, final YamlRuleConfiguration yamlRuleConfig) {
        RuleNodeTupleEntity entity = yamlRuleConfig.getClass().getAnnotation(RuleNodeTupleEntity.class);
        Preconditions.checkNotNull(entity);
        Preconditions.checkArgument(!entity.leaf());
        String ruleType = entity.value();
        Collection<RuleNodeTuple> result = new LinkedList<>();
        for (Field each : YamlRuleConfigurationReflectionEngine.getFields(yamlRuleConfig.getClass())) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            result.addAll(swapToTuples(databaseName, ruleType, yamlRuleConfig, each));
            each.setAccessible(isAccessible);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<RuleNodeTuple> swapToTuples(final String databaseName, final String ruleType, final YamlRuleConfiguration yamlRuleConfig, final Field field) {
        Object fieldValue = field.get(yamlRuleConfig);
        if (null == fieldValue) {
            return Collections.emptyList();
        }
        String ruleItemName = YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field);
        return isNamedItem(field)
                ? swapToNamedTuples(databaseName, ruleType, ruleItemName, field, fieldValue)
                : swapToUniqueTuple(databaseName, ruleType, ruleItemName, fieldValue).map(Collections::singletonList).orElse(Collections.emptyList());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private Collection<RuleNodeTuple> swapToNamedTuples(final String databaseName, final String ruleType, final String ruleItemType, final Field field, final Object fieldValue) {
        Collection<RuleNodeTuple> result = new LinkedList<>();
        if (fieldValue instanceof Map) {
            for (Object entry : ((Map) fieldValue).entrySet()) {
                String ruleItemName = ((Entry) entry).getKey().toString();
                result.add(new RuleNodeTuple(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(ruleItemType, ruleItemName)), YamlEngine.marshal(((Entry) entry).getValue())));
            }
        } else {
            for (Object each : (Collection) fieldValue) {
                String ruleItemName = field.getAnnotation(RuleNodeTupleKeyListNameGenerator.class).value().getConstructor().newInstance().generate(each);
                result.add(new RuleNodeTuple(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(ruleItemType, ruleItemName)), each.toString()));
            }
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<RuleNodeTuple> swapToUniqueTuple(final String databaseName, final String ruleType, final String ruleItemType, final Object fieldValue) {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(ruleItemType));
        if (fieldValue instanceof Collection) {
            return ((Collection) fieldValue).isEmpty() ? Optional.empty() : Optional.of(new RuleNodeTuple(databaseRuleNodePath, YamlEngine.marshal(fieldValue)));
        }
        if (fieldValue instanceof String) {
            return ((String) fieldValue).isEmpty() ? Optional.empty() : Optional.of(new RuleNodeTuple(databaseRuleNodePath, fieldValue.toString()));
        }
        if (fieldValue instanceof Boolean || fieldValue instanceof Integer || fieldValue instanceof Long) {
            return Optional.of(new RuleNodeTuple(databaseRuleNodePath, fieldValue.toString()));
        }
        if (fieldValue instanceof Enum) {
            return Optional.of(new RuleNodeTuple(databaseRuleNodePath, ((Enum) fieldValue).name()));
        }
        return Optional.of(new RuleNodeTuple(databaseRuleNodePath, YamlEngine.marshal(fieldValue)));
    }
    
    /**
     * Swap to YAML global rule configuration.
     *
     * @param ruleType rule type
     * @param ruleContent rule content
     * @return global rule configuration
     */
    public YamlRuleConfiguration swapToYamlGlobalRuleConfiguration(final String ruleType, final String ruleContent) {
        return YamlEngine.unmarshal(ruleContent, YamlRuleConfigurationReflectionEngine.findClass(ruleType));
    }
    
    /**
     * Swap to YAML database rule configuration.
     *
     * @param databaseName database name
     * @param ruleType rule type
     * @param tuples rule node tuples
     * @return database rule configuration
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public YamlRuleConfiguration swapToYamlDatabaseRuleConfiguration(final String databaseName, final String ruleType, final Collection<RuleNodeTuple> tuples) {
        Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = YamlRuleConfigurationReflectionEngine.findClass(ruleType);
        Collection<Field> fields = YamlRuleConfigurationReflectionEngine.getFields(yamlRuleConfigClass);
        YamlRuleConfiguration result = yamlRuleConfigClass.getConstructor().newInstance();
        DatabaseRuleNode databaseRuleNode = DatabaseRuleNodeGenerator.generate(yamlRuleConfigClass);
        for (RuleNodeTuple each : tuples) {
            if (!Strings.isNullOrEmpty(each.getContent())) {
                setFieldValue(databaseName, result, fields, databaseRuleNode.getRuleType(), each);
            }
        }
        return result;
    }
    
    private void setFieldValue(final String databaseName, final YamlRuleConfiguration yamlRuleConfig, final Collection<Field> fields, final String ruleType, final RuleNodeTuple tuple) {
        for (Field each : fields) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            setFieldValue(databaseName, yamlRuleConfig, each, ruleType, tuple);
            each.setAccessible(isAccessible);
        }
    }
    
    private void setFieldValue(final String databaseName, final YamlRuleConfiguration yamlRuleConfig, final Field field, final String ruleType, final RuleNodeTuple tuple) {
        String itemType = YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field);
        if (isNamedItem(field)) {
            setNamedItemFieldValue(databaseName, yamlRuleConfig, ruleType, tuple, itemType, field);
        } else {
            setUniqueItemFieldValue(databaseName, yamlRuleConfig, ruleType, tuple, itemType, field);
        }
    }
    
    private boolean isNamedItem(final Field field) {
        return field.getType().equals(Map.class) || null != field.getAnnotation(RuleNodeTupleKeyListNameGenerator.class) && field.getType().equals(Collection.class);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setNamedItemFieldValue(final String databaseName, final YamlRuleConfiguration yamlRuleConfig,
                                        final String ruleType, final RuleNodeTuple tuple, final String itemType, final Field field) {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(itemType, NodePathPattern.QUALIFIED_IDENTIFIER));
        Optional<String> itemValue = NodePathSearcher.find(tuple.getPath(), new NodePathSearchCriteria(databaseRuleNodePath, true, 1));
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
    private void setUniqueItemFieldValue(final String databaseName, final YamlRuleConfiguration yamlRuleConfig,
                                         final String ruleType, final RuleNodeTuple tuple, final String itemType, final Field field) {
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(itemType));
        if (!NodePathSearcher.isMatchedPath(tuple.getPath(), new NodePathSearchCriteria(databaseRuleNodePath, true, 1))) {
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
}
