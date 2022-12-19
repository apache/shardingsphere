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
import org.apache.shardingsphere.agent.advice.type.ConstructorAdvice;
import org.apache.shardingsphere.agent.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.advice.type.StaticMethodAdvice;
import org.apache.shardingsphere.agent.config.advisor.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.executor.type.ConstructorAdviceExecutor;
import org.apache.shardingsphere.agent.core.plugin.executor.type.InstanceMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.plugin.executor.type.StaticMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.build.advise.AdviceFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Method advisor build engine.
 */
@RequiredArgsConstructor
public final class MethodAdvisorBuildEngine {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(MethodAdvisorBuildEngine.class);
    
    private final AdviceFactory adviceFactory;
    
    private final Collection<MethodAdvisorConfiguration> advisorConfigs;
    
    private final TypeDescription typePointcut;
    
    /**
     * Create method advisor builder.
     * 
     * @param builder original builder
     * @return created builder
     */
    public Builder<?> create(final Builder<?> builder) {
        Builder<?> result = builder;
        for (MethodAdvisor each : getMatchedMethodAdvisors()) {
            try {
                result = each.getAdviceExecutor().create(result, each);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private Collection<MethodAdvisor> getMatchedMethodAdvisors() {
        Collection<MethodAdvisor> result = new LinkedList<>();
        for (InDefinedShape each : typePointcut.getDeclaredMethods()) {
            result.addAll(getMatchedMethodAdvisors(each));
        }
        return result;
    }
    
    private Collection<MethodAdvisor> getMatchedMethodAdvisors(final InDefinedShape methodDescription) {
        Collection<MethodAdvisor> result = new LinkedList<>();
        if (isConstructor(methodDescription)) {
            Collection<ConstructorAdvice> advices = advisorConfigs.stream()
                    .filter(each -> each.getPointcut().matches(methodDescription)).map(each -> (ConstructorAdvice) adviceFactory.getAdvice(each.getAdviceClassName())).collect(Collectors.toList());
            result.add(new MethodAdvisor(methodDescription, new ConstructorAdviceExecutor(advices)));
        } else if (isStaticMethod(methodDescription)) {
            Collection<StaticMethodAdvice> advices = advisorConfigs.stream()
                    .filter(each -> each.getPointcut().matches(methodDescription)).map(each -> (StaticMethodAdvice) adviceFactory.getAdvice(each.getAdviceClassName())).collect(Collectors.toList());
            result.add(new MethodAdvisor(methodDescription, new StaticMethodAdviceExecutor(advices)));
        } else if (isMethod(methodDescription)) {
            Collection<InstanceMethodAdvice> advices = advisorConfigs.stream()
                    .filter(each -> each.getPointcut().matches(methodDescription)).map(each -> (InstanceMethodAdvice) adviceFactory.getAdvice(each.getAdviceClassName())).collect(Collectors.toList());
            result.add(new MethodAdvisor(methodDescription, new InstanceMethodAdviceExecutor(advices)));
        }
        return result;
    }
    
    private boolean isConstructor(final InDefinedShape methodDescription) {
        return methodDescription.isConstructor();
    }
    
    private boolean isStaticMethod(final InDefinedShape methodDescription) {
        return methodDescription.isStatic() && isMethod(methodDescription);
    }
    
    private boolean isMethod(final InDefinedShape methodDescription) {
        return !(methodDescription.isAbstract() || methodDescription.isSynthetic());
    }
}
