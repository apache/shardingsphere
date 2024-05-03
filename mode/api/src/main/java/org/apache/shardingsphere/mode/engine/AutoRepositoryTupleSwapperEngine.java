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

package org.apache.shardingsphere.mode.engine;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterPersistField;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterPersistType;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterRuleEntity;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterTupleKeyNameGenerator;
import org.apache.shardingsphere.mode.path.GlobalNodePath;
import org.apache.shardingsphere.mode.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;

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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Auto repository tuple swapper engine.
 */
public final class AutoRepositoryTupleSwapperEngine {
    
    /**
     * Swap to repository tuples.
     *
     * @param yamlRuleConfig YAML rule configuration to be swapped
     * @return repository tuples
     */
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlRuleConfiguration yamlRuleConfig) {
        if (null == yamlRuleConfig.getClass().getAnnotation(RegistryCenterRuleEntity.class)) {
            return Collections.emptyList();
        }
        RegistryCenterPersistType persistType = yamlRuleConfig.getClass().getAnnotation(RegistryCenterPersistType.class);
        if (null != persistType) {
            return Collections.singleton(new RepositoryTuple(persistType.value(), YamlEngine.marshal(yamlRuleConfig)));
        }
        Collection<RepositoryTuple> result = new LinkedList<>();
        Collection<Field> fields = Arrays.stream(yamlRuleConfig.getClass().getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RegistryCenterPersistField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RegistryCenterPersistField.class).order())).collect(Collectors.toList());
        RuleNodePath ruleNodePath = getRuleNodePathProvider(yamlRuleConfig).getRuleNodePath();
        for (Field each : fields) {
            RegistryCenterPersistField persistField = each.getAnnotation(RegistryCenterPersistField.class);
            if (null == persistField) {
                continue;
            }
            boolean isAccessible = each.isAccessible();
            each.setAccessible(true);
            Object fieldValue = each.get(yamlRuleConfig);
            RegistryCenterTupleKeyNameGenerator tupleKeyNameGenerator = each.getAnnotation(RegistryCenterTupleKeyNameGenerator.class);
            if (null != tupleKeyNameGenerator && fieldValue instanceof Collection) {
                for (Object value : (Collection) fieldValue) {
                    String tupleKeyName = tupleKeyNameGenerator.value().getConstructor().newInstance().generate(value);
                    result.add(new RepositoryTuple(ruleNodePath.getNamedItem(persistField.value()).getPath(tupleKeyName), value.toString()));
                }
            } else if (fieldValue instanceof Map) {
                for (Object entry : ((Map) fieldValue).entrySet()) {
                    result.add(new RepositoryTuple(ruleNodePath.getNamedItem(persistField.value()).getPath(((Entry) entry).getKey().toString()), YamlEngine.marshal(((Entry) entry).getValue())));
                }
            } else if (fieldValue instanceof Collection && !((Collection) fieldValue).isEmpty()) {
                result.add(new RepositoryTuple(ruleNodePath.getUniqueItem(persistField.value()).getPath(), YamlEngine.marshal(fieldValue)));
            } else if (fieldValue instanceof String && !((String) fieldValue).isEmpty()) {
                result.add(new RepositoryTuple(ruleNodePath.getUniqueItem(persistField.value()).getPath(), fieldValue.toString()));
            } else if (fieldValue instanceof Boolean || fieldValue instanceof Integer || fieldValue instanceof Long) {
                result.add(new RepositoryTuple(ruleNodePath.getUniqueItem(persistField.value()).getPath(), fieldValue.toString()));
            } else if (null != fieldValue) {
                result.add(new RepositoryTuple(ruleNodePath.getUniqueItem(persistField.value()).getPath(), YamlEngine.marshal(fieldValue)));
            }
            each.setAccessible(isAccessible);
        }
        return result;
    }
    
    // TODO 修改 RuleNodePathProvider 为 TypedSPI
    private RuleNodePathProvider getRuleNodePathProvider(final YamlRuleConfiguration yamlRuleConfig) {
        for (RuleNodePathProvider each : ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)) {
            if (yamlRuleConfig.getRuleConfigurationType().getSimpleName().toLowerCase().contains(String.join("", each.getRuleNodePath().getRoot().getRuleType().split("_")))) {
                return each;
            }
        }
        throw new ServiceProviderNotFoundException(RuleNodePathProvider.class, yamlRuleConfig);
    }
    
    /**
     * Swap from repository tuple to YAML rule configurations.
     *
     * @param repositoryTuples repository tuples
     * @param yamlRuleConfigurationClass YAML rule configuration class
     * @return swapped YAML rule configurations
     */
    public Optional<YamlRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples, final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        if (null == yamlRuleConfigurationClass.getAnnotation(RegistryCenterRuleEntity.class)) {
            return Optional.empty();
        }
        RegistryCenterPersistType persistType = yamlRuleConfigurationClass.getAnnotation(RegistryCenterPersistType.class);
        if (null != persistType) {
            return swapToObjectForTypePersist(repositoryTuples, yamlRuleConfigurationClass, persistType);
        }
        return swapToObjectForFieldPersist(repositoryTuples, yamlRuleConfigurationClass);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToObjectForTypePersist(final Collection<RepositoryTuple> repositoryTuples,
                                                                       final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass, final RegistryCenterPersistType persistType) {
        if (YamlGlobalRuleConfiguration.class.isAssignableFrom(yamlRuleConfigurationClass)) {
            for (RepositoryTuple each : repositoryTuples) {
                if (GlobalNodePath.getVersion(persistType.value(), each.getKey()).isPresent()) {
                    return Optional.of(YamlEngine.unmarshal(each.getValue(), yamlRuleConfigurationClass));
                }
            }
            return Optional.empty();
        }
        YamlRuleConfiguration yamlRuleConfig = yamlRuleConfigurationClass.getConstructor().newInstance();
        RuleNodePath ruleNodePath = getRuleNodePathProvider(yamlRuleConfig).getRuleNodePath();
        for (RepositoryTuple each : repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList())) {
            if (ruleNodePath.getUniqueItem(persistType.value()).isValidatedPath(each.getKey())) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), yamlRuleConfigurationClass));
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ReflectiveOperationException.class)
    private Optional<YamlRuleConfiguration> swapToObjectForFieldPersist(final Collection<RepositoryTuple> repositoryTuples, final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        YamlRuleConfiguration yamlRuleConfig = yamlRuleConfigurationClass.getConstructor().newInstance();
        RuleNodePath ruleNodePath = getRuleNodePathProvider(yamlRuleConfig).getRuleNodePath();
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        Collection<Field> fields = Arrays.stream(yamlRuleConfigurationClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RegistryCenterPersistField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RegistryCenterPersistField.class).order())).collect(Collectors.toList());
        for (RepositoryTuple each : validTuples) {
            for (Field field : fields) {
                RegistryCenterPersistField persistField = field.getAnnotation(RegistryCenterPersistField.class);
                if (null == persistField || Strings.isNullOrEmpty(each.getValue())) {
                    continue;
                }
                final boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                Object fieldValue = field.get(yamlRuleConfig);
                RegistryCenterTupleKeyNameGenerator tupleKeyNameGenerator = field.getAnnotation(RegistryCenterTupleKeyNameGenerator.class);
                if (null != tupleKeyNameGenerator && fieldValue instanceof Collection) {
                    ruleNodePath.getNamedItem(persistField.value()).getName(each.getKey()).ifPresent(optional -> ((Collection) fieldValue).add(each.getValue()));
                } else if (fieldValue instanceof Map) {
                    Class<?> valueClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
                    ruleNodePath.getNamedItem(persistField.value()).getName(each.getKey()).ifPresent(optional -> ((Map) fieldValue).put(optional, YamlEngine.unmarshal(each.getValue(), valueClass)));
                } else if (fieldValue instanceof Collection && !((Collection) fieldValue).isEmpty()) {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, each.getValue());
                    }
                } else if (field.getType().equals(String.class)) {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, each.getValue());
                    }
                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, Boolean.parseBoolean(each.getValue()));
                    }
                } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, Integer.parseInt(each.getValue()));
                    }
                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, Long.parseLong(each.getValue()));
                    }
                } else {
                    if (ruleNodePath.getUniqueItem(persistField.value()).isValidatedPath(each.getKey())) {
                        field.set(yamlRuleConfig, YamlEngine.unmarshal(each.getValue(), field.getType()));
                    }
                }
                field.setAccessible(isAccessible);
            }
        }
        return Optional.of(yamlRuleConfig);
    }
}
