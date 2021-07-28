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

package org.apache.shardingsphere.agent.metrics.api.definition;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
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
public final class MetricsPluginDefinitionService extends AbstractPluginDefinitionService {
    
    @Override
    public void defineInterceptors() {
        Yaml yaml = new Yaml();
        InputStream in = this.getClass().getResourceAsStream("/interceptors.yaml");
        Interceptors interceptors = yaml.loadAs(in, Interceptors.class);
        for (Interceptor interceptor : interceptors.getInterceptors()) { 
            String[] instancePoints = interceptor
                    .getPoints()
                    .stream()
                    .filter(i -> i.getType().equals("instance"))
                    .map(TargetPoint::getName)
                    .toArray(String[]::new);
            String[] staticPoints = interceptor
                    .getPoints()
                    .stream()
                    .filter(i -> i.getType().equals("static"))
                    .map(TargetPoint::getName)
                    .toArray(String[]::new);
            PluginInterceptorPoint.Builder builder = defineInterceptor(interceptor.getTarget());
            if (instancePoints.length > 0) {
                builder.aroundInstanceMethod(ElementMatchers.namedOneOf(instancePoints))
                        .implement(interceptor.getInstanceAdvice())
                        .build();
                log.debug("init instance:{}", interceptor.getInstanceAdvice());
            }
            if (staticPoints.length > 0) {
                builder.aroundClassStaticMethod(ElementMatchers.namedOneOf(staticPoints))
                        .implement(interceptor.getStaticAdvice())
                        .build();
                log.debug("init static:{}", interceptor.getStaticAdvice());
            }
        }
    }
    
    @Override
    public String getType() {
        return "Metrics";
    }
}
