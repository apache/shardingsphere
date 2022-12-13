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
import org.apache.shardingsphere.agent.core.definition.PointcutDefinitionServiceEngine;
import org.apache.shardingsphere.agent.core.yaml.entity.Interceptor;
import org.apache.shardingsphere.agent.core.yaml.entity.TargetPoint;
import org.apache.shardingsphere.agent.core.yaml.swapper.InterceptorsYamlSwapper;
import org.apache.shardingsphere.agent.pointcut.ClassPointcuts;
import org.apache.shardingsphere.agent.pointcut.ConstructorPointcut;
import org.apache.shardingsphere.agent.pointcut.InstanceMethodPointcut;
import org.apache.shardingsphere.agent.pointcut.StaticMethodPointcut;
import org.apache.shardingsphere.agent.spi.PointcutDefinitionService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Metrics pointcut definition service.
 */
public final class PrometheusPointcutDefinitionService implements PointcutDefinitionService {
    
    @Override
    public Collection<ClassPointcuts> getProxyPointcuts() {
        Collection<ClassPointcuts> result = new LinkedList<>();
        PointcutDefinitionServiceEngine engine = new PointcutDefinitionServiceEngine(this);
        for (Interceptor each : new InterceptorsYamlSwapper().unmarshal(getClass().getResourceAsStream("/prometheus/interceptors.yaml")).getInterceptors()) {
            if (null != each.getTarget()) {
                result.add(createClassPointcuts(engine, each));
            }
        }
        return result;
    }
    
    private ClassPointcuts createClassPointcuts(final PointcutDefinitionServiceEngine engine, final Interceptor interceptor) {
        ClassPointcuts result = engine.getAllPointcuts(interceptor.getTarget());
        if (null != interceptor.getConstructAdvice() && !("".equals(interceptor.getConstructAdvice()))) {
            result.getConstructorPointcuts().add(new ConstructorPointcut(ElementMatchers.isConstructor(), interceptor.getConstructAdvice()));
        }
        String[] instancePoints = interceptor.getPoints().stream().filter(i -> "instance".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
        if (instancePoints.length > 0) {
            result.getInstanceMethodPointcuts().add(new InstanceMethodPointcut(ElementMatchers.namedOneOf(instancePoints), interceptor.getInstanceAdvice()));
        }
        String[] staticPoints = interceptor.getPoints().stream().filter(i -> "static".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
        if (staticPoints.length > 0) {
            result.getStaticMethodPointcuts().add(new StaticMethodPointcut(ElementMatchers.namedOneOf(staticPoints), interceptor.getStaticAdvice()));
        }
        return result;
    }
    
    @Override
    public Collection<ClassPointcuts> getJDBCPointcuts() {
        // TODO add JDBC related interceptors
        return Collections.emptyList();
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
