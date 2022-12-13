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
import org.apache.shardingsphere.agent.core.advisor.AdvisorDefinitionServiceEngine;
import org.apache.shardingsphere.agent.core.yaml.entity.Interceptor;
import org.apache.shardingsphere.agent.core.yaml.entity.TargetPoint;
import org.apache.shardingsphere.agent.core.yaml.swapper.InterceptorsYamlSwapper;
import org.apache.shardingsphere.agent.advisor.ClassAdvisor;
import org.apache.shardingsphere.agent.advisor.ConstructorAdvisor;
import org.apache.shardingsphere.agent.advisor.InstanceMethodAdvisor;
import org.apache.shardingsphere.agent.advisor.StaticMethodAdvisor;
import org.apache.shardingsphere.agent.spi.AdvisorDefinitionService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Prometheus advisor definition service.
 */
public final class PrometheusAdvisorDefinitionService implements AdvisorDefinitionService {
    
    private final AdvisorDefinitionServiceEngine engine = new AdvisorDefinitionServiceEngine(this);
    
    @Override
    public Collection<ClassAdvisor> getProxyAdvisors() {
        Collection<ClassAdvisor> result = new LinkedList<>();
        for (Interceptor each : new InterceptorsYamlSwapper().unmarshal(getClass().getResourceAsStream("/prometheus/interceptors.yaml")).getInterceptors()) {
            if (null != each.getTarget()) {
                result.add(createClassAdvisor(each));
            }
        }
        return result;
    }
    
    private ClassAdvisor createClassAdvisor(final Interceptor interceptor) {
        ClassAdvisor result = engine.getAdvisors(interceptor.getTarget());
        if (null != interceptor.getConstructAdvice() && !("".equals(interceptor.getConstructAdvice()))) {
            result.getConstructorAdvisors().add(new ConstructorAdvisor(ElementMatchers.isConstructor(), interceptor.getConstructAdvice()));
        }
        String[] instancePoints = interceptor.getPoints().stream().filter(i -> "instance".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
        if (instancePoints.length > 0) {
            result.getInstanceMethodAdvisors().add(new InstanceMethodAdvisor(ElementMatchers.namedOneOf(instancePoints), interceptor.getInstanceAdvice()));
        }
        String[] staticPoints = interceptor.getPoints().stream().filter(i -> "static".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
        if (staticPoints.length > 0) {
            result.getStaticMethodAdvisors().add(new StaticMethodAdvisor(ElementMatchers.namedOneOf(staticPoints), interceptor.getStaticAdvice()));
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
