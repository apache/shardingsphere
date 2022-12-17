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

package org.apache.shardingsphere.agent.metrics.prometheus;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.advisor.AdvisorConfigurationRegistryFactory;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.swapper.YamlAdvisorsConfigurationSwapper;
import org.apache.shardingsphere.agent.spi.advisor.AdvisorDefinitionService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Prometheus advisor definition service.
 */
public final class PrometheusAdvisorDefinitionService implements AdvisorDefinitionService {
    
    @Override
    public Collection<AdvisorConfiguration> getProxyAdvisorConfigurations() {
        Collection<AdvisorConfiguration> result = new LinkedList<>();
        for (YamlAdvisorConfiguration each : new YamlAdvisorsConfigurationSwapper().unmarshal(getClass().getResourceAsStream("/prometheus/advisors.yaml")).getAdvisors()) {
            if (null != each.getTarget()) {
                result.add(createAdvisorConfiguration(each));
            }
        }
        return result;
    }
    
    private AdvisorConfiguration createAdvisorConfiguration(final YamlAdvisorConfiguration yamlAdvisorConfig) {
        AdvisorConfiguration result = AdvisorConfigurationRegistryFactory.getRegistry(getType()).getAdvisorConfiguration(yamlAdvisorConfig.getTarget());
        if (null != yamlAdvisorConfig.getConstructAdvice() && !("".equals(yamlAdvisorConfig.getConstructAdvice()))) {
            result.getConstructorAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.isConstructor(), yamlAdvisorConfig.getConstructAdvice()));
        }
        String[] instancePointcuts = yamlAdvisorConfig.getPointcuts().stream().filter(i -> "instance".equals(i.getType())).map(YamlPointcutConfiguration::getName).toArray(String[]::new);
        if (instancePointcuts.length > 0) {
            result.getInstanceMethodAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.namedOneOf(instancePointcuts), yamlAdvisorConfig.getInstanceAdvice()));
        }
        String[] staticPointcuts = yamlAdvisorConfig.getPointcuts().stream().filter(i -> "static".equals(i.getType())).map(YamlPointcutConfiguration::getName).toArray(String[]::new);
        if (staticPointcuts.length > 0) {
            result.getStaticMethodAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.namedOneOf(staticPointcuts), yamlAdvisorConfig.getStaticAdvice()));
        }
        return result;
    }
    
    @Override
    public Collection<AdvisorConfiguration> getJDBCAdvisorConfigurations() {
        // TODO add JDBC related interceptors
        return Collections.emptyList();
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
