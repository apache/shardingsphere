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

package org.apache.shardingsphere.agent.core.builder;

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.core.classloader.ClassLoaderContext;
import org.apache.shardingsphere.agent.core.log.LoggerFactory;
import org.apache.shardingsphere.agent.core.log.LoggerFactory.Logger;
import org.apache.shardingsphere.agent.core.plugin.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.executor.AdviceExecutor;
import org.apache.shardingsphere.agent.core.plugin.executor.AdviceExecutorFactory;

import java.util.Optional;

/**
 * Method advisor builder decorator.
 */
public final class MethodAdvisorBuilderDecorator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodAdvisorBuilderDecorator.class);
    
    private final TypeDescription typePointcut;
    
    private final AdviceExecutorFactory adviceExecutorFactory;
    
    public MethodAdvisorBuilderDecorator(final TypeDescription typePointcut, final ClassLoaderContext classLoaderContext, final AdvisorConfiguration advisorConfig) {
        this.typePointcut = typePointcut;
        adviceExecutorFactory = new AdviceExecutorFactory(classLoaderContext, advisorConfig);
    }
    
    /**
     * Decorate agent builder with method advisor.
     * 
     * @param builder to be decorated agent builder
     * @return decorated agent builder
     */
    public Builder<?> decorate(final Builder<?> builder) {
        Builder<?> result = builder;
        for (InDefinedShape each : typePointcut.getDeclaredMethods()) {
            Optional<AdviceExecutor> adviceExecutor = adviceExecutorFactory.findMatchedAdviceExecutor(each);
            if (!adviceExecutor.isPresent()) {
                continue;
            }
            try {
                result = adviceExecutor.get().decorateBuilder(result, each);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
}
