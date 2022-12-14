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

package org.apache.shardingsphere.agent.metrics.prometheus.definition;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.advisor.ClassAdvisor;
import org.apache.shardingsphere.agent.advisor.ConstructorAdvisor;
import org.apache.shardingsphere.agent.advisor.InstanceMethodAdvisor;
import org.apache.shardingsphere.agent.advisor.StaticMethodAdvisor;
import org.apache.shardingsphere.agent.core.plugin.advisor.ClassAdvisorRegistryFactory;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.plugin.yaml.swapper.YamlAdvisorsConfigurationSwapper;
import org.apache.shardingsphere.agent.spi.AdvisorDefinitionService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Prometheus advisor definition service.
 */
public final class PrometheusAdvisorDefinitionService implements AdvisorDefinitionService {
    
    @Override
    public Collection<ClassAdvisor> getProxyAdvisors() {
        Collection<ClassAdvisor> result = new LinkedList<>();
        for (YamlAdvisorConfiguration each : new YamlAdvisorsConfigurationSwapper().unmarshal(getClass().getResourceAsStream("/prometheus/advisors.yaml")).getAdvisors()) {
            if (null != each.getTarget()) {
                result.add(createClassAdvisor(each));
            }
        }
        return result;
    }
    
    private ClassAdvisor createClassAdvisor(final YamlAdvisorConfiguration yamlAdvisorConfiguration) {
        ClassAdvisor result = ClassAdvisorRegistryFactory.getRegistry(getType()).getAdvisor(yamlAdvisorConfiguration.getTarget());
        if (null != yamlAdvisorConfiguration.getConstructAdvice() && !("".equals(yamlAdvisorConfiguration.getConstructAdvice()))) {
            result.getConstructorAdvisors().add(new ConstructorAdvisor(ElementMatchers.isConstructor(), yamlAdvisorConfiguration.getConstructAdvice()));
        }
        String[] instancePointcuts = yamlAdvisorConfiguration.getPointcuts().stream().filter(i -> "instance".equals(i.getType())).map(YamlPointcutConfiguration::getName).toArray(String[]::new);
        if (instancePointcuts.length > 0) {
            result.getInstanceMethodAdvisors().add(new InstanceMethodAdvisor(ElementMatchers.namedOneOf(instancePointcuts), yamlAdvisorConfiguration.getInstanceAdvice()));
        }
        String[] staticPointcuts = yamlAdvisorConfiguration.getPointcuts().stream().filter(i -> "static".equals(i.getType())).map(YamlPointcutConfiguration::getName).toArray(String[]::new);
        if (staticPointcuts.length > 0) {
            result.getStaticMethodAdvisors().add(new StaticMethodAdvisor(ElementMatchers.namedOneOf(staticPointcuts), yamlAdvisorConfiguration.getStaticAdvice()));
        }
        return result;
    }
    
    @Override
    public Collection<ClassAdvisor> getJDBCAdvisors() {
        // TODO add JDBC related interceptors
        return Collections.emptyList();
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
