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

package org.apache.shardingsphere.mode.node.tuple;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.node.path.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleField;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleKeyListNameGenerator;

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
 * Repository tuple swapper engine.
 */
public final class RepositoryTupleSwapperEngine {
    
    /**
     * Swap to repository tuples.
     *
     * @param yamlRuleConfig YAML rule configuration to be swapped
     * @return repository tuples
     */
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlRuleConfiguration yamlRuleConfig) {
        RepositoryTupleEntity tupleEntity = yamlRuleConfig.getClass().getAnnotation(RepositoryTupleEntity.class);
        if (null == tupleEntity) {
            return Collections.emptyList();
        }
        if (tupleEntity.leaf()) {
            return Collections.singleton(new RepositoryTuple(tupleEntity.value(), YamlEngine.marshal(yamlRuleConfig)));
        }
        Collection<RepositoryTuple> result = new LinkedList<>();
        RuleNodePath ruleNodePath = TypedSPILoader.getService(RuleNodePathProvider.class, yamlRuleConfig.getRuleConfigurationType()).getRuleNodePath();
        for (Field each : getFields(yamlRuleConfig.getClass())) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            result.addAll(swapToRepositoryTuples(yamlRuleConfig, ruleNodePath, each));
            each.setAccessible(isAccessible);
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private Collection<RepositoryTuple> swapToRepositoryTuples(final YamlRuleConfiguration yamlRuleConfig, final RuleNodePath ruleNodePath, final Field field) {
        Object fieldValue = field.get(yamlRuleConfig);
        if (null == fieldValue) {
            return Collections.emptyList();
        }
        String tupleName = getTupleName(field);
        RepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator = field.getAnnotation(RepositoryTupleKeyListNameGenerator.class);
        if (null != tupleKeyListNameGenerator && fieldValue instanceof Collection) {
            Collection<RepositoryTuple> result = new LinkedList<>();
            for (Object value : (Collection) fieldValue) {
                String tupleKeyName = tupleKeyListNameGenerator.value().getConstructor().newInstance().generate(value);
                result.add(new RepositoryTuple(ruleNodePath.getNamedItem(tupleName).getPath(tupleKeyName), value.toString()));
            }
            return result;
        }
        if (fieldValue instanceof Map) {
            Collection<RepositoryTuple> result = new LinkedList<>();
            for (Object entry : ((Map) fieldValue).entrySet()) {
                result.add(new RepositoryTuple(ruleNodePath.getNamedItem(tupleName).getPath(((Entry) entry).getKey().toString()), YamlEngine.marshal(((Entry) entry).getValue())));
            }
            return result;
        }
        if (fieldValue instanceof Collection) {
            return ((Collection) fieldValue).isEmpty()
                    ? Collections.emptyList()
                    : Collections.singleton(new RepositoryTuple(ruleNodePath.getUniqueItem(tupleName).getPath(), YamlEngine.marshal(fieldValue)));
        }
        if (fieldValue instanceof String) {
            return ((String) fieldValue).isEmpty() ? Collections.emptyList() : Collections.singleton(new RepositoryTuple(ruleNodePath.getUniqueItem(tupleName).getPath(), fieldValue.toString()));
        }
        if (fieldValue instanceof Boolean || fieldValue instanceof Integer || fieldValue instanceof Long) {
            return Collections.singleton(new RepositoryTuple(ruleNodePath.getUniqueItem(tupleName).getPath(), fieldValue.toString()));
        }
        if (fieldValue instanceof Enum) {
            return Collections.singleton(new RepositoryTuple(ruleNodePath.getUniqueItem(tupleName).getPath(), ((Enum) fieldValue).name()));
        }
        return Collections.singleton(new RepositoryTuple(ruleNodePath.getUniqueItem(tupleName).getPath(), YamlEngine.marshal(fieldValue)));
    }
    
    private Collection<Field> getFields(final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        return Arrays.stream(yamlRuleConfigurationClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RepositoryTupleField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RepositoryTupleField.class).type().ordinal())).collect(Collectors.toList());
    }
    
    private String getTupleName(final Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }
    
    /**
     * Swap from repository tuple to YAML rule configurations.
     *
     * @param repositoryTuples repository tuples
     * @param toBeSwappedType to be swapped type YAML rule configuration class type
     * @return swapped YAML rule configurations
     */
    public Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RepositoryTuple> repositoryTuples, final Class<? extends YamlRuleConfiguration> toBeSwappedType) {
        RepositoryTupleEntity tupleEntity = toBeSwappedType.getAnnotation(RepositoryTupleEntity.class);
        if (null == tupleEntity) {
            return Optional.empty();
        }
        return tupleEntity.leaf()
                ? swapToYamlRuleConfiguration(repositoryTuples, toBeSwappedType, tupleEntity)
                : swapToYamlRuleConfiguration(repositoryTuples, toBeSwappedType, getFields(toBeSwappedType));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RepositoryTuple> repositoryTuples,
                                                                        final Class<? extends YamlRuleConfiguration> toBeSwappedType, final RepositoryTupleEntity tupleEntity) {
        if (YamlGlobalRuleConfiguration.class.isAssignableFrom(toBeSwappedType)) {
            for (RepositoryTuple each : repositoryTuples) {
                if (GlobalRuleNodePath.findVersion(tupleEntity.value(), each.getKey()).isPresent()) {
                    return Optional.of(YamlEngine.unmarshal(each.getValue(), toBeSwappedType));
                }
            }
            return Optional.empty();
        }
        YamlRuleConfiguration yamlRuleConfig = toBeSwappedType.getConstructor().newInstance();
        RuleNodePath ruleNodePath = TypedSPILoader.getService(RuleNodePathProvider.class, yamlRuleConfig.getRuleConfigurationType()).getRuleNodePath();
        for (RepositoryTuple each : repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList())) {
            if (ruleNodePath.getUniqueItem(tupleEntity.value()).isValidatedPath(each.getKey())) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), toBeSwappedType));
            }
        }
        return Optional.empty();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToYamlRuleConfiguration(final Collection<RepositoryTuple> repositoryTuples,
                                                                        final Class<? extends YamlRuleConfiguration> toBeSwappedType, final Collection<Field> fields) {
        YamlRuleConfiguration yamlRuleConfig = toBeSwappedType.getConstructor().newInstance();
        RuleNodePath ruleNodePath = TypedSPILoader.getService(RuleNodePathProvider.class, yamlRuleConfig.getRuleConfigurationType()).getRuleNodePath();
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        for (RepositoryTuple each : validTuples) {
            if (!Strings.isNullOrEmpty(each.getValue())) {
                setFieldValue(yamlRuleConfig, fields, ruleNodePath, each);
            }
        }
        return Optional.of(yamlRuleConfig);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Collection<Field> fields, final RuleNodePath ruleNodePath, final RepositoryTuple repositoryTuple) {
        for (Field each : fields) {
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            setFieldValue(yamlRuleConfig, each, ruleNodePath, repositoryTuple);
            each.setAccessible(isAccessible);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setFieldValue(final YamlRuleConfiguration yamlRuleConfig, final Field field, final RuleNodePath ruleNodePath, final RepositoryTuple repositoryTuple) throws IllegalAccessException {
        Object fieldValue = field.get(yamlRuleConfig);
        String tupleName = getTupleName(field);
        RepositoryTupleKeyListNameGenerator tupleKeyListNameGenerator = field.getAnnotation(RepositoryTupleKeyListNameGenerator.class);
        if (null != tupleKeyListNameGenerator && fieldValue instanceof Collection) {
            ruleNodePath.getNamedItem(tupleName).getName(repositoryTuple.getKey()).ifPresent(optional -> ((Collection) fieldValue).add(repositoryTuple.getValue()));
            return;
        }
        if (fieldValue instanceof Map) {
            Class<?> valueClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
            ruleNodePath.getNamedItem(tupleName).getName(repositoryTuple.getKey())
                    .ifPresent(optional -> ((Map) fieldValue).put(optional, YamlEngine.unmarshal(repositoryTuple.getValue(), valueClass)));
            return;
        }
        if (!ruleNodePath.getUniqueItem(tupleName).isValidatedPath(repositoryTuple.getKey())) {
            return;
        }
        if (fieldValue instanceof Collection) {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(repositoryTuple.getValue(), List.class));
        } else if (field.getType().equals(String.class)) {
            field.set(yamlRuleConfig, repositoryTuple.getValue());
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            field.set(yamlRuleConfig, Boolean.parseBoolean(repositoryTuple.getValue()));
        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            field.set(yamlRuleConfig, Integer.parseInt(repositoryTuple.getValue()));
        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
            field.set(yamlRuleConfig, Long.parseLong(repositoryTuple.getValue()));
        } else {
            field.set(yamlRuleConfig, YamlEngine.unmarshal(repositoryTuple.getValue(), field.getType()));
        }
    }
    
    /**
     * Swap to rule configurations.
     *
     * @param repositoryTuples repository tuples
     * @return global rule configurations
     */
    @SuppressWarnings("rawtypes")
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<RepositoryTuple> repositoryTuples) {
        if (repositoryTuples.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<RuleConfiguration> result = new LinkedList<>();
        YamlRuleConfigurationSwapperEngine yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        for (YamlRuleConfigurationSwapper each : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            swapToYamlRuleConfiguration(repositoryTuples, yamlRuleConfigClass).ifPresent(optional -> result.add(yamlSwapperEngine.swapToRuleConfiguration(optional)));
        }
        return result;
    }
    
    /**
     * Swap to rule configuration.
     *
     * @param ruleTypeName rule type name
     * @param repositoryTuples repository tuples
     * @return global rule configuration
     */
    @SuppressWarnings("rawtypes")
    public Optional<RuleConfiguration> swapToRuleConfiguration(final String ruleTypeName, final Collection<RepositoryTuple> repositoryTuples) {
        if (repositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlRuleConfigurationSwapperEngine yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        for (YamlRuleConfigurationSwapper each : ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            if (ruleTypeName.equals(Objects.requireNonNull(yamlRuleConfigClass.getAnnotation(RepositoryTupleEntity.class)).value())) {
                Optional<YamlRuleConfiguration> yamlRuleConfig = swapToYamlRuleConfiguration(repositoryTuples, yamlRuleConfigClass);
                return yamlRuleConfig.map(yamlSwapperEngine::swapToRuleConfiguration);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends YamlRuleConfiguration> getYamlRuleConfigurationClass(final YamlRuleConfigurationSwapper swapper) {
        return (Class<? extends YamlRuleConfiguration>) ((ParameterizedType) swapper.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
