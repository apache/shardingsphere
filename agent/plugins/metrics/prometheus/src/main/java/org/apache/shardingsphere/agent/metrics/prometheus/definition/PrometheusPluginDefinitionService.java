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

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint.Builder;
import org.apache.shardingsphere.agent.core.entity.Interceptor;
import org.apache.shardingsphere.agent.core.entity.Interceptors;
import org.apache.shardingsphere.agent.core.entity.TargetPoint;
import org.apache.shardingsphere.agent.spi.definition.AbstractPluginDefinitionService;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Metrics plugin definition service.
 */
public final class PrometheusPluginDefinitionService extends AbstractPluginDefinitionService {
    
    @Override
    public void defineProxyInterceptors() {
        defineInterceptors("/prometheus/proxy/interceptors.yaml");
    }
    
    @Override
    public void defineJdbcInterceptors() {
        defineInterceptors("/prometheus/jdbc/interceptors.yaml");
    }
    
    private void defineInterceptors(final String resourceName) {
        InputStream inputStream = getClass().getResourceAsStream(resourceName);
        Interceptors interceptors = new Yaml().loadAs(inputStream, Interceptors.class);
        if (Objects.isNull(interceptors) || Objects.isNull(interceptors.getInterceptors())) {
            return;
        }
        interceptors.getInterceptors().forEach(this::buildInterceptor);
    }
    
    private void buildInterceptor(final Interceptor interceptor) {
        if (null == interceptor.getTarget()) {
            return;
        }
        Builder builder = defineInterceptor(interceptor.getTarget());
        if (null != interceptor.getConstructAdvice() && !("".equals(interceptor.getConstructAdvice()))) {
            builder.onConstructor(ElementMatchers.isConstructor()).implement(interceptor.getConstructAdvice()).build();
        }
        if (null == interceptor.getPoints() || interceptor.getPoints().isEmpty()) {
            return;
        }
        Collection<TargetPoint> instanceMethods = interceptor.getPoints().stream().filter(each -> "instance".equals(each.getType())).collect(Collectors.toList());
        Collection<TargetPoint> classStaticMethods = interceptor.getPoints().stream().filter(each -> "static".equals(each.getType())).collect(Collectors.toList());
        instanceMethods.forEach(each -> buildInstanceMethod(builder, interceptor, each));
        classStaticMethods.forEach(each -> buildClassStaticMethod(builder, interceptor, each));
    }
    
    private void buildInstanceMethod(final Builder builder, final Interceptor interceptor, final TargetPoint targetPoint) {
        builder.aroundInstanceMethod(createElementMatcher(targetPoint)).implement(interceptor.getInstanceAdvice()).build();
    }
    
    private void buildClassStaticMethod(final Builder builder, final Interceptor interceptor, final TargetPoint targetPoint) {
        builder.aroundClassStaticMethod(createElementMatcher(targetPoint)).implement(interceptor.getStaticAdvice()).build();
    }
    
    private ElementMatcher<? super MethodDescription> createElementMatcher(final TargetPoint targetPoint) {
        Junction<NamedElement> result = ElementMatchers.named(targetPoint.getName());
        if (Objects.nonNull(targetPoint.getParameterLength()) && targetPoint.getParameterLength() >= 0) {
            result.and(ElementMatchers.takesArguments(targetPoint.getParameterLength()));
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
