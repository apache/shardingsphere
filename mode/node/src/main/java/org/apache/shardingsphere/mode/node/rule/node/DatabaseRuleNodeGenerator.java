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

package org.apache.shardingsphere.mode.node.rule.node;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.node.rule.tuple.YamlRuleConfigurationReflectionEngine;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleKeyListNameGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Database rule node generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleNodeGenerator {
    
    /**
     * Generate database rule node.
     *
     * @param yamlRuleConfigClass YAML rule configuration class
     * @return generated database rule node
     */
    public static DatabaseRuleNode generate(final Class<? extends YamlRuleConfiguration> yamlRuleConfigClass) {
        RuleNodeTupleEntity tupleEntity = yamlRuleConfigClass.getAnnotation(RuleNodeTupleEntity.class);
        Preconditions.checkNotNull(tupleEntity, "Can not find @RuleNodeTupleEntity on class: ", yamlRuleConfigClass.getName());
        Collection<String> namedItems = new LinkedList<>();
        Collection<String> uniqueItems = new LinkedList<>();
        for (Field each : YamlRuleConfigurationReflectionEngine.getFields(yamlRuleConfigClass)) {
            if (null == each.getAnnotation(RuleNodeTupleField.class)) {
                continue;
            }
            String tupleName = YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(each);
            if (each.getType().equals(Map.class) || each.getType().equals(Collection.class) && null != each.getAnnotation(RuleNodeTupleKeyListNameGenerator.class)) {
                namedItems.add(tupleName);
            } else {
                uniqueItems.add(tupleName);
            }
        }
        return new DatabaseRuleNode(tupleEntity.value(), namedItems, uniqueItems);
    }
    
    /**
     * Generate database rule node.
     *
     * @param ruleType rule type
     * @return generated database rule node
     */
    public static DatabaseRuleNode generate(final String ruleType) {
        return findYamlRuleConfigurationClass(ruleType).map(DatabaseRuleNodeGenerator::generate).orElseThrow(() -> new IllegalArgumentException(ruleType));
    }
    
    private static Optional<Class<? extends YamlRuleConfiguration>> findYamlRuleConfigurationClass(final String ruleType) {
        for (YamlRuleConfigurationSwapper<?, ?> each : ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)) {
            Optional<Class<? extends YamlRuleConfiguration>> yamlRuleConfigClass = findYamlRuleConfigurationClass(each.getClass());
            if (!yamlRuleConfigClass.isPresent()) {
                continue;
            }
            RuleNodeTupleEntity entity = yamlRuleConfigClass.get().getAnnotation(RuleNodeTupleEntity.class);
            if (null != entity && entity.value().equals(ruleType)) {
                return yamlRuleConfigClass;
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Optional<Class<? extends YamlRuleConfiguration>> findYamlRuleConfigurationClass(final Class<? extends YamlRuleConfigurationSwapper> swapperClass) {
        for (Type each : swapperClass.getGenericInterfaces()) {
            if (each instanceof ParameterizedType) {
                return Optional.of((Class<? extends YamlRuleConfiguration>) ((ParameterizedType) each).getActualTypeArguments()[0]);
            }
        }
        return Optional.empty();
    }
}
