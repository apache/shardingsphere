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

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearchCriteria;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeProvider;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleKeyListNameGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * YAML rule repository tuple swapper engine.
 */
public final class YamlRuleRepositoryTupleSwapperEngine {
    
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
        if (entity.leaf()) {
            return Collections.singleton(new RuleRepositoryTuple(entity.value(), YamlEngine.marshal(yamlRuleConfig)));
        }
        Collection<RuleRepositoryTuple> result = new LinkedList<>();
        for (Field each : getFields(yamlRuleConfig.getClass())) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            result.addAll(swapToTuples(yamlRuleConfig, each));
            each.setAccessible(isAccessible);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private Collection<RuleRepositoryTuple> swapToTuples(final YamlRuleConfiguration yamlRuleConfig, final Field field) {
        Object fieldValue = field.get(yamlRuleConfig);
        if (null == fieldValue) {
            return Collections.emptyList();
        }
        String tupleName = getTupleName(field);
        RuleRepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator = field.getAnnotation(RuleRepositoryTupleKeyListNameGenerator.class);
        if (null != tupleKeyListNameGenerator && fieldValue instanceof Collection) {
            Collection<RuleRepositoryTuple> result = new LinkedList<>();
            for (Object value : (Collection) fieldValue) {
                String tupleKeyName = tupleKeyListNameGenerator.value().getConstructor().newInstance().generate(value);
                result.add(new RuleRepositoryTuple(new DatabaseRuleItem(tupleName, tupleKeyName).toString(), value.toString()));
            }
            return result;
        }
        if (fieldValue instanceof Map) {
            Collection<RuleRepositoryTuple> result = new LinkedList<>();
            for (Object entry : ((Map) fieldValue).entrySet()) {
                result.add(new RuleRepositoryTuple(new DatabaseRuleItem(tupleName, ((Entry) entry).getKey().toString()).toString(), YamlEngine.marshal(((Entry) entry).getValue())));
            }
            return result;
        }
        if (fieldValue instanceof Collection) {
            return ((Collection) fieldValue).isEmpty() ? Collections.emptyList() : Collections.singleton(new RuleRepositoryTuple(tupleName, YamlEngine.marshal(fieldValue)));
        }
        if (fieldValue instanceof String) {
            return ((String) fieldValue).isEmpty() ? Collections.emptyList() : Collections.singleton(new RuleRepositoryTuple(tupleName, fieldValue.toString()));
        }
        if (fieldValue instanceof Boolean || fieldValue instanceof Integer || fieldValue instanceof Long) {
            return Collections.singleton(new RuleRepositoryTuple(tupleName, fieldValue.toString()));
        }
        if (fieldValue instanceof Enum) {
            return Collections.singleton(new RuleRepositoryTuple(tupleName, ((Enum) fieldValue).name()));
        }
        return Collections.singleton(new RuleRepositoryTuple(tupleName, YamlEngine.marshal(fieldValue)));
    }
    
    private Collection<Field> getFields(final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        return Arrays.stream(yamlRuleConfigurationClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RuleRepositoryTupleField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RuleRepositoryTupleField.class).type().ordinal())).collect(Collectors.toList());
    }
    
    private String getTupleName(final Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }
    
    /**
     * Swap from rule repository tuple to YAML rule configurations.
     *
     * @param tuples rule repository tuples
     * @param toBeSwappedType to be swapped type YAML rule configuration class type
     * @return swapped YAML rule configurations
     */
    public Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RuleRepositoryTuple> tuples, final Class<? extends YamlRuleConfiguration> toBeSwappedType) {
        RuleRepositoryTupleEntity entity = toBeSwappedType.getAnnotation(RuleRepositoryTupleEntity.class);
        if (null == entity) {
            return Optional.empty();
        }
        return entity.leaf() ? swapToYamlRuleConfiguration(tuples, toBeSwappedType, entity) : swapToYamlRuleConfiguration(tuples, toBeSwappedType, getFields(toBeSwappedType));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RuleRepositoryTuple> tuples,
                                                                        final Class<? extends YamlRuleConfiguration> toBeSwappedType, final RuleRepositoryTupleEntity entity) {
        if (YamlGlobalRuleConfiguration.class.isAssignableFrom(toBeSwappedType)) {
            for (RuleRepositoryTuple each : tuples) {
                if (NodePathSearcher.isMatchedPath(each.getKey(), new NodePathSearchCriteria(new GlobalRuleNodePath(entity.value()), false, true, 1))) {
                    return Optional.of(YamlEngine.unmarshal(each.getValue(), toBeSwappedType));
                }
            }
            return Optional.empty();
        }
        YamlRuleConfiguration yamlRuleConfig = toBeSwappedType.getConstructor().newInstance();
        DatabaseRuleNode databaseRuleNode = TypedSPILoader.getService(DatabaseRuleNodeProvider.class, yamlRuleConfig.getRuleConfigurationType()).getDatabaseRuleNode();
        for (RuleRepositoryTuple each : tuples.stream()
                .filter(each -> NodePathSearcher.isMatchedPath(each.getKey(), DatabaseRuleNodePath.createValidRuleTypeSearchCriteria(databaseRuleNode.getRuleType()))).collect(Collectors.toList())) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, databaseRuleNode.getRuleType(), new DatabaseRuleItem(entity.value()));
            if (NodePathSearcher.isMatchedPath(each.getKey(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 1))) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), toBeSwappedType));
            }
        }
        return Optional.empty();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RuleRepositoryTuple> tuples,
                                                                        final Class<? extends YamlRuleConfiguration> toBeSwappedType, final Collection<Field> fields) {
        YamlRuleConfiguration yamlRuleConfig = toBeSwappedType.getConstructor().newInstance();
        DatabaseRuleNode databaseRuleNode = TypedSPILoader.getService(DatabaseRuleNodeProvider.class, yamlRuleConfig.getRuleConfigurationType()).getDatabaseRuleNode();
        List<RuleRepositoryTuple> validTuples = tuples.stream()
                .filter(each -> NodePathSearcher.isMatchedPath(each.getKey(), DatabaseRuleNodePath.createValidRuleTypeSearchCriteria(databaseRuleNode.getRuleType()))).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        for (RuleRepositoryTuple each : validTuples) {
            if (!Strings.isNullOrEmpty(each.getValue())) {
                setFieldValue(yamlRuleConfig, fields, databaseRuleNode, each);
            }
        }
        return Optional.of(yamlRuleConfig);
    }
    
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Collection<Field> fields, final DatabaseRuleNode databaseRuleNode, final RuleRepositoryTuple tuple) {
        for (Field each : fields) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            setFieldValue(yamlRuleConfig, each, databaseRuleNode, tuple);
            each.setAccessible(isAccessible);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Field field, final DatabaseRuleNode databaseRuleNode, final RuleRepositoryTuple tuple) {
        Object fieldValue = field.get(yamlRuleConfig);
        String tupleName = getTupleName(field);
        RuleRepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator = field.getAnnotation(RuleRepositoryTupleKeyListNameGenerator.class);
        if (null != tupleKeyListNameGenerator && fieldValue instanceof Collection) {
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(
                    NodePathPattern.IDENTIFIER, databaseRuleNode.getRuleType(), new DatabaseRuleItem(tupleName, NodePathPattern.QUALIFIED_IDENTIFIER));
            NodePathSearcher.find(tuple.getKey(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 2)).ifPresent(optional -> ((Collection) fieldValue).add(tuple.getValue()));
            return;
        }
        if (fieldValue instanceof Map) {
            Class<?> valueClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(
                    NodePathPattern.IDENTIFIER, databaseRuleNode.getRuleType(), new DatabaseRuleItem(tupleName, NodePathPattern.QUALIFIED_IDENTIFIER));
            NodePathSearcher.find(tuple.getKey(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 2))
                    .ifPresent(optional -> ((Map) fieldValue).put(optional, YamlEngine.unmarshal(tuple.getValue(), valueClass)));
            return;
        }
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(NodePathPattern.IDENTIFIER, databaseRuleNode.getRuleType(), new DatabaseRuleItem(tupleName));
        if (!NodePathSearcher.isMatchedPath(tuple.getKey(), new NodePathSearchCriteria(databaseRuleNodePath, false, true, 1))) {
            return;
        }
        if (fieldValue instanceof Collection) {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(tuple.getValue(), List.class));
        } else if (field.getType().equals(String.class)) {
            field.set(yamlRuleConfig, tuple.getValue());
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            field.set(yamlRuleConfig, Boolean.parseBoolean(tuple.getValue()));
        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            field.set(yamlRuleConfig, Integer.parseInt(tuple.getValue()));
        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
            field.set(yamlRuleConfig, Long.parseLong(tuple.getValue()));
        } else {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(tuple.getValue(), field.getType()));
        }
    }
    
    /**
     * Swap to rule configurations.
     *
     * @param tuples rule repository tuples
     * @return global rule configurations
     */
    @SuppressWarnings("rawtypes")
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<RuleRepositoryTuple> tuples) {
        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<RuleConfiguration> result = new LinkedList<>();
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        for (YamlRuleConfigurationSwapper each : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            swapToYamlRuleConfiguration(tuples, yamlRuleConfigClass).ifPresent(optional -> result.add(swapperEngine.swapToRuleConfiguration(optional)));
        }
        return result;
    }
    
    /**
     * Swap to rule configuration.
     *
     * @param ruleType rule type
     * @param tuples rule repository tuples
     * @return global rule configuration
     */
    @SuppressWarnings("rawtypes")
    public Optional<RuleConfiguration> swapToRuleConfiguration(final String ruleType, final Collection<RuleRepositoryTuple> tuples) {
        if (tuples.isEmpty()) {
            return Optional.empty();
        }
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        for (YamlRuleConfigurationSwapper each : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            if (ruleType.equals(Objects.requireNonNull(yamlRuleConfigClass.getAnnotation(RuleRepositoryTupleEntity.class)).value())) {
                return swapToYamlRuleConfiguration(tuples, yamlRuleConfigClass).map(swapperEngine::swapToRuleConfiguration);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends YamlRuleConfiguration> getYamlRuleConfigurationClass(final YamlRuleConfigurationSwapper swapper) {
        return (Class<? extends YamlRuleConfiguration>) ((ParameterizedType) swapper.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
