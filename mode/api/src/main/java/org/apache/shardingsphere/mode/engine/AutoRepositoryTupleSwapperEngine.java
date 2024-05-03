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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterPersistField;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterPersistType;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterRuleEntity;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterTupleKeyNameGenerator;
import org.apache.shardingsphere.mode.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
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
        RegistryCenterRuleEntity entity = yamlRuleConfig.getClass().getAnnotation(RegistryCenterRuleEntity.class);
        if (null == entity) {
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
        for (Field each : fields) {
            RegistryCenterPersistField persistField = each.getAnnotation(RegistryCenterPersistField.class);
            if (null == persistField) {
                continue;
            }
            RuleNodePath ruleNodePath = getRuleNodePathProvider(yamlRuleConfig).getRuleNodePath();
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
        if (!result.isEmpty()) {
            return result;
        }
        return Collections.emptyList();
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
}
