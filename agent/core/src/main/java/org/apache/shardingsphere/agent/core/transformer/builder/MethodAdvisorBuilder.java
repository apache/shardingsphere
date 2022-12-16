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

package org.apache.shardingsphere.agent.core.transformer.builder;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.config.advisor.method.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Method advisor builder.
 * 
 * @param <T> type of method advisor configuration
 */
@RequiredArgsConstructor
public abstract class MethodAdvisorBuilder<T extends MethodAdvisorConfiguration> {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(MethodAdvisorBuilder.class);
    
    private final Collection<T> advisorConfigs;
    
    private final TypeDescription typePointcut;
    
    /**
     * Create constructor advisor builder.
     * 
     * @param builder original builder
     * @return created builder
     */
    public final Builder<?> create(final Builder<?> builder) {
        Builder<?> result = builder;
        Collection<MethodAdvisor> matchedAdvisor = typePointcut.getDeclaredMethods()
                .stream().filter(this::isMatchedMethod).map(this::getMatchedAdvisor).filter(Objects::nonNull).collect(Collectors.toList());
        for (MethodAdvisor each : matchedAdvisor) {
            try {
                result = create(result, each);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
    
    protected abstract Builder<?> create(Builder<?> builder, MethodAdvisor methodAdvisor);
    
    private MethodAdvisor getMatchedAdvisor(final InDefinedShape methodPointcut) {
        List<T> matchedAdvisorConfigs = advisorConfigs.stream().filter(each -> each.getPointcut().matches(methodPointcut)).collect(Collectors.toList());
        if (matchedAdvisorConfigs.isEmpty()) {
            return null;
        }
        if (1 == matchedAdvisorConfigs.size()) {
            return getSingleMethodAdvisor(methodPointcut, matchedAdvisorConfigs.get(0));
        }
        return getComposedMethodAdvisor(methodPointcut, matchedAdvisorConfigs);
    }
    
    protected abstract boolean isMatchedMethod(InDefinedShape methodPointcut);
    
    protected abstract MethodAdvisor getSingleMethodAdvisor(InDefinedShape methodPointcut, T advisorConfig);
    
    protected abstract MethodAdvisor getComposedMethodAdvisor(InDefinedShape methodPointcut, List<T> advisorConfigs);
}
