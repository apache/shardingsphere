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

package org.apache.shardingsphere.agent.core.plugin.yaml.swapper;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.advisor.AdvisorConfigurationRegistryFactory;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlPointcutParameterConfiguration;

import java.util.Optional;

/**
 * YAML advisor configuration swapper.
 */
public final class YamlAdvisorConfigurationSwapper {
    
    /**
     * Swap from YAML advisor configuration to advisors configuration.
     * 
     * @param yamlAdvisorConfig YAML advisor configuration
     * @param type type
     * @return advisor configuration
     */
    public AdvisorConfiguration swapToObject(final YamlAdvisorConfiguration yamlAdvisorConfig, final String type) {
        AdvisorConfiguration result = AdvisorConfigurationRegistryFactory.getRegistry(type).getAdvisorConfiguration(yamlAdvisorConfig.getTarget());
        for (YamlPointcutConfiguration each : yamlAdvisorConfig.getPointcuts()) {
            Optional<Junction<? super MethodDescription>> pointcut = createPointcut(each);
            if (pointcut.isPresent()) {
                appendParameters(each, pointcut.get());
                result.getAdvisors().add(new MethodAdvisorConfiguration(pointcut.get(), yamlAdvisorConfig.getAdvice()));
            }
        }
        return result;
    }
    
    private Optional<Junction<? super MethodDescription>> createPointcut(final YamlPointcutConfiguration yamlPointcutConfig) {
        if ("constructor".equals(yamlPointcutConfig.getType())) {
            return Optional.of(ElementMatchers.isConstructor());
        }
        if ("method".equals(yamlPointcutConfig.getType())) {
            return Optional.of(ElementMatchers.namedOneOf(yamlPointcutConfig.getName()));
        }
        return Optional.empty();
    }
    
    private void appendParameters(final YamlPointcutConfiguration yamlPointcutConfig, final Junction<? super MethodDescription> pointcut) {
        for (YamlPointcutParameterConfiguration each : yamlPointcutConfig.getParams()) {
            pointcut.and(ElementMatchers.takesArgument(each.getIndex(), ElementMatchers.named(each.getType())));
        }
    }
}
