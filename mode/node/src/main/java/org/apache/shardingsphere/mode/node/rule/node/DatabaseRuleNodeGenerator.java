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

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleKeyListNameGenerator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Database rule node generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRuleNodeGenerator {
    
    /**
     * Generate database rule node.
     *
     * @param yamlRuleConfigurationClass YAML rule configuration class
     * @return generated database rule node
     */
    public static DatabaseRuleNode generate(final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        RuleRepositoryTupleEntity tupleEntity = yamlRuleConfigurationClass.getAnnotation(RuleRepositoryTupleEntity.class);
        Preconditions.checkNotNull(tupleEntity, "Can not find @RuleRepositoryTupleEntity on class: ", yamlRuleConfigurationClass.getName());
        Collection<String> namedItems = new LinkedList<>();
        Collection<String> uniqueItems = new LinkedList<>();
        for (Field each : getFields(yamlRuleConfigurationClass)) {
            if (null == each.getAnnotation(RuleRepositoryTupleField.class)) {
                continue;
            }
            if (each.getType().equals(Map.class) || each.getType().equals(Collection.class) && null != each.getAnnotation(RuleRepositoryTupleKeyListNameGenerator.class)) {
                namedItems.add(getPersistTupleName(each));
            } else {
                uniqueItems.add(getPersistTupleName(each));
            }
        }
        return new DatabaseRuleNode(tupleEntity.value(), namedItems, uniqueItems);
    }
    
    private static Collection<Field> getFields(final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        return Arrays.stream(yamlRuleConfigurationClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RuleRepositoryTupleField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RuleRepositoryTupleField.class).type().ordinal())).collect(Collectors.toList());
    }
    
    private static String getPersistTupleName(final Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }
}
