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

package org.apache.shardingsphere.agent.core.advisor.config.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.ModifierMatcher;
import net.bytebuddy.matcher.ModifierMatcher.Mode;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutParameterConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * YAML pointcut configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlPointcutConfigurationSwapper {
    
    private static final Logger LOGGER = Logger.getLogger(YamlPointcutConfigurationSwapper.class.getName());
    
    /**
     * Swap from YAML pointcut configuration to method pointcut.
     *
     * @param yamlConfig YAML pointcut configuration
     * @return method pointcut
     */
    public static Optional<ElementMatcher<MethodDescription>> swap(final YamlPointcutConfiguration yamlConfig) {
        if ("constructor".equals(yamlConfig.getType())) {
            return Optional.of(createElementMatcher(yamlConfig, ElementMatchers.isConstructor()));
        }
        if ("method".equals(yamlConfig.getType())) {
            return Optional.of(createElementMatcher(yamlConfig, ElementMatchers.named(yamlConfig.getName())));
        }
        return Optional.empty();
    }
    
    private static ElementMatcher<MethodDescription> createElementMatcher(final YamlPointcutConfiguration yamlConfig, final Junction<MethodDescription> pointcut) {
        Junction<MethodDescription> result = appendMethodModifiers(yamlConfig.getModifiers(), pointcut);
        result = appendParameters(yamlConfig, result);
        return appendReturns(yamlConfig, result);
    }
    
    private static Junction<MethodDescription> appendMethodModifiers(final String methodModifiers, final Junction<MethodDescription> pointcut) {
        Junction<MethodDescription> result = pointcut;
        if (null == methodModifiers) {
            return result;
        }
        String[] modifiers = methodModifiers.split("\\s");
        for (String each : modifiers) {
            Optional<Mode> mode = findMode(each.trim().toUpperCase());
            if (mode.isPresent()) {
                result = result.and(ModifierMatcher.of(mode.get()));
            }
        }
        return result;
    }
    
    private static Optional<Mode> findMode(final String mode) {
        try {
            return Optional.of(Mode.valueOf(mode.toUpperCase()));
        } catch (final IllegalArgumentException ignored) {
            LOGGER.log(Level.SEVERE, "Invalid parameters `{0}`, valid values is [{1}]", new String[]{mode, getValidValues()});
        }
        return Optional.empty();
    }
    
    private static String getValidValues() {
        Collection<String> result = new LinkedList<>();
        for (Mode each : Mode.values()) {
            result.add(each.name().toLowerCase());
        }
        return String.join(",", result);
    }
    
    private static Junction<MethodDescription> appendParameters(final YamlPointcutConfiguration yamlConfig, final Junction<MethodDescription> pointcut) {
        Junction<MethodDescription> result = pointcut;
        if (null != yamlConfig.getParamLength() && 0 <= yamlConfig.getParamLength()) {
            result = result.and(ElementMatchers.takesArguments(yamlConfig.getParamLength()));
        }
        for (YamlPointcutParameterConfiguration each : yamlConfig.getParams()) {
            result = result.and(ElementMatchers.takesArgument(each.getIndex(), ElementMatchers.named(each.getType())));
        }
        return result;
    }
    
    private static Junction<MethodDescription> appendReturns(final YamlPointcutConfiguration yamlConfig, final Junction<MethodDescription> pointcut) {
        Junction<MethodDescription> result = pointcut;
        if (null == yamlConfig.getReturnType()) {
            return result;
        }
        result = result.and(ElementMatchers.returns(ElementMatchers.named(yamlConfig.getReturnType())));
        return result;
    }
}
