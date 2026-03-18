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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * YAML rule configuration reflection engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlRuleConfigurationReflectionEngine {
    
    /**
     * Find YAML rule configuration class.
     *
     * @param ruleType rule type
     * @return found class
     * @throws IllegalArgumentException throw if YAML rule configuration class not found
     */
    @SuppressWarnings("rawtypes")
    public static Class<? extends YamlRuleConfiguration> findClass(final String ruleType) {
        for (YamlRuleConfigurationSwapper each : ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)) {
            Class<? extends YamlRuleConfiguration> yamlRuleConfigClass = getYamlRuleConfigurationClass(each);
            RuleNodeTupleEntity entity = yamlRuleConfigClass.getAnnotation(RuleNodeTupleEntity.class);
            if (null != entity && entity.value().equals(ruleType)) {
                return yamlRuleConfigClass;
            }
        }
        throw new IllegalArgumentException(String.format("Can not find YAML rule configuration with type: %s", ruleType));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<? extends YamlRuleConfiguration> getYamlRuleConfigurationClass(final YamlRuleConfigurationSwapper swapper) {
        return (Class<? extends YamlRuleConfiguration>) ((ParameterizedType) swapper.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
    
    /**
     * Get YAML rule configuration fields.
     *
     * @param yamlRuleConfigClass YAML rule configuration class
     * @return got fields
     */
    public static Collection<Field> getFields(final Class<? extends YamlRuleConfiguration> yamlRuleConfigClass) {
        return Arrays.stream(yamlRuleConfigClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RuleNodeTupleField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RuleNodeTupleField.class).type().ordinal())).collect(Collectors.toList());
    }
    
    /**
     * Get rule node item name.
     *
     * @param field YAML rule configuration field
     * @return got name
     */
    public static String getRuleNodeItemName(final Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }
}
