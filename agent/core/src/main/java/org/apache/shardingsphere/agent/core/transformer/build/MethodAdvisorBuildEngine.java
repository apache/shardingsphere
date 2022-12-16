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

package org.apache.shardingsphere.agent.core.transformer.build;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.config.advisor.method.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.build.builder.MethodAdvisorBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Method advisor build engine.
 */
@RequiredArgsConstructor
public final class MethodAdvisorBuildEngine<T extends MethodAdvisorConfiguration> {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(MethodAdvisorBuildEngine.class);
    
    private final Collection<T> advisorConfigs;
    
    private final TypeDescription typePointcut;
    
    /**
     * Create method advisor builder.
     * 
     * @param builder original builder
     * @param methodAdvisorBuilder method advisor builder
     * @return created builder
     */
    public Builder<?> create(final Builder<?> builder, final MethodAdvisorBuilder<T> methodAdvisorBuilder) {
        Builder<?> result = builder;
        Collection<MethodAdvisor> matchedAdvisor = typePointcut.getDeclaredMethods().stream()
                .filter(methodAdvisorBuilder::isMatchedMethod).map(each -> findMatchedAdvisor(each, methodAdvisorBuilder)).filter(Objects::nonNull).collect(Collectors.toList());
        for (MethodAdvisor each : matchedAdvisor) {
            try {
                result = methodAdvisorBuilder.create(result, each);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private MethodAdvisor findMatchedAdvisor(final InDefinedShape methodDescription, final MethodAdvisorBuilder<T> methodAdvisorBuilder) {
        List<T> matchedAdvisorConfigs = advisorConfigs.stream().filter(each -> each.getPointcut().matches(methodDescription)).collect(Collectors.toList());
        if (matchedAdvisorConfigs.isEmpty()) {
            return null;
        }
        if (1 == matchedAdvisorConfigs.size()) {
            return methodAdvisorBuilder.getSingleMethodAdvisor(methodDescription, matchedAdvisorConfigs.get(0));
        }
        return methodAdvisorBuilder.getComposedMethodAdvisor(methodDescription, matchedAdvisorConfigs);
    }
}
