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

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint.Builder;
import org.apache.shardingsphere.agent.core.entity.Interceptor;
import org.apache.shardingsphere.agent.core.entity.Interceptors;
import org.apache.shardingsphere.agent.core.entity.TargetPoint;
import org.apache.shardingsphere.agent.spi.definition.AbstractPluginDefinitionService;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Metrics plugin definition service.
 */
@Slf4j
public final class PrometheusPluginDefinitionService extends AbstractPluginDefinitionService {
    
    @Override
    public void defineInterceptors() {
        InputStream inputStream = getClass().getResourceAsStream("/prometheus/interceptors.yaml");
        Interceptors interceptors = new Yaml().loadAs(inputStream, Interceptors.class);
        for (Interceptor each : interceptors.getInterceptors()) {
            if (null == each.getTarget()) {
                continue;
            }
            Builder builder = defineInterceptor(each.getTarget());
            if (null != each.getConstructAdvice() && !("".equals(each.getConstructAdvice()))) {
                builder.onConstructor(ElementMatchers.isConstructor()).implement(each.getConstructAdvice()).build();
                log.debug("Init construct: {}", each.getConstructAdvice());
            }
            if (null == each.getPoints()) {
                continue;
            }
            String[] instancePoints = each.getPoints().stream().filter(i -> "instance".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
            String[] staticPoints = each.getPoints().stream().filter(i -> "static".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
            if (instancePoints.length > 0) {
                builder.aroundInstanceMethod(ElementMatchers.namedOneOf(instancePoints)).implement(each.getInstanceAdvice()).build();
                log.debug("Init instance: {}", each.getInstanceAdvice());
            }
            if (staticPoints.length > 0) {
                builder.aroundClassStaticMethod(ElementMatchers.namedOneOf(staticPoints)).implement(each.getStaticAdvice()).build();
                log.debug("Init static: {}", each.getStaticAdvice());
            }
        }
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
