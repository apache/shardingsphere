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

package org.apache.shardingsphere.agent.core.builder.interceptor.impl;

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutorFactory;
import org.apache.shardingsphere.agent.core.builder.interceptor.AgentBuilderInterceptor;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Method advisor builder interceptor.
 */
public final class MethodAdvisorBuilderInterceptor implements AgentBuilderInterceptor {
    
    private static final Logger LOGGER = Logger.getLogger(MethodAdvisorBuilderInterceptor.class.getName());
    
    private final TypeDescription typePointcut;
    
    private final AdviceExecutorFactory adviceExecutorFactory;
    
    public MethodAdvisorBuilderInterceptor(final TypeDescription typePointcut, final ClassLoaderContext classLoaderContext, final AdvisorConfiguration advisorConfig) {
        this.typePointcut = typePointcut;
        adviceExecutorFactory = new AdviceExecutorFactory(classLoaderContext, advisorConfig);
    }
    
    @Override
    public Builder<?> intercept(final Builder<?> builder) {
        Builder<?> result = builder;
        for (InDefinedShape each : typePointcut.getDeclaredMethods()) {
            Optional<AdviceExecutor> adviceExecutor = adviceExecutorFactory.findMatchedAdviceExecutor(each);
            if (!adviceExecutor.isPresent()) {
                continue;
            }
            try {
                result = adviceExecutor.get().intercept(result, each);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.log(Level.SEVERE, "Failed to load advice class: {0}, {1}", new String[]{typePointcut.getTypeName(), ex.getMessage()});
            }
        }
        return result;
    }
}
