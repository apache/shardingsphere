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
import org.apache.shardingsphere.agent.pointcut.PluginPointcuts.Builder;
import org.apache.shardingsphere.agent.core.definition.AbstractPluginDefinitionService;
import org.apache.shardingsphere.agent.core.yaml.entity.Interceptor;
import org.apache.shardingsphere.agent.core.yaml.entity.TargetPoint;
import org.apache.shardingsphere.agent.core.yaml.swapper.InterceptorsYamlSwapper;

/**
 * Metrics plugin definition service.
 */
public final class PrometheusPluginDefinitionService extends AbstractPluginDefinitionService {
    
    @Override
    protected void defineProxyInterceptors() {
        for (Interceptor each : new InterceptorsYamlSwapper().unmarshal(getClass().getResourceAsStream("/prometheus/interceptors.yaml")).getInterceptors()) {
            if (null == each.getTarget()) {
                continue;
            }
            Builder builder = defineInterceptor(each.getTarget());
            if (null != each.getConstructAdvice() && !("".equals(each.getConstructAdvice()))) {
                builder.onConstructor(ElementMatchers.isConstructor()).implement(each.getConstructAdvice()).build();
            }
            String[] instancePoints = each.getPoints().stream().filter(i -> "instance".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
            String[] staticPoints = each.getPoints().stream().filter(i -> "static".equals(i.getType())).map(TargetPoint::getName).toArray(String[]::new);
            if (instancePoints.length > 0) {
                builder.aroundInstanceMethod(ElementMatchers.namedOneOf(instancePoints)).implement(each.getInstanceAdvice()).build();
            }
            if (staticPoints.length > 0) {
                builder.aroundStaticMethod(ElementMatchers.namedOneOf(staticPoints)).implement(each.getStaticAdvice()).build();
            }
        }
    }
    
    @Override
    protected void defineJdbcInterceptors() {
        // TODO add JDBC related interception
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
