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
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutParameterConfiguration;

import java.util.Optional;

/**
 * YAML pointcut configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlPointcutConfigurationSwapper {
    
    /**
     * Swap from YAML pointcut configuration to method pointcut.
     * 
     * @param yamlConfig YAML pointcut configuration
     * @return method pointcut
     */
    public static Optional<ElementMatcher<MethodDescription>> swap(final YamlPointcutConfiguration yamlConfig) {
        if ("constructor".equals(yamlConfig.getType())) {
            return Optional.of(appendParameters(yamlConfig, ElementMatchers.isConstructor()));
        }
        if ("method".equals(yamlConfig.getType())) {
            return Optional.of(appendParameters(yamlConfig, ElementMatchers.named(yamlConfig.getName())));
        }
        return Optional.empty();
    }
    
    private static ElementMatcher<MethodDescription> appendParameters(final YamlPointcutConfiguration yamlConfig, final Junction<MethodDescription> pointcut) {
        Junction<MethodDescription> result = pointcut;
        for (YamlPointcutParameterConfiguration each : yamlConfig.getParams()) {
            result = result.and(ElementMatchers.takesArgument(each.getIndex(), ElementMatchers.named(each.getType())));
        }
        return result;
    }
}
