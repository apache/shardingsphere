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

package org.apache.shardingsphere.agent.core.advisor.executor;

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import org.apache.shardingsphere.agent.api.advice.AgentAdvice;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.executor.type.ConstructorAdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.type.InstanceMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.type.StaticMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Advice executor factory.
 */
public final class AdviceExecutorFactory {
    
    private final AdviceFactory adviceFactory;
    
    private final AdvisorConfiguration advisorConfig;
    
    public AdviceExecutorFactory(final ClassLoaderContext classLoaderContext, final AdvisorConfiguration advisorConfig) {
        adviceFactory = new AdviceFactory(classLoaderContext);
        this.advisorConfig = advisorConfig;
    }
    
    /**
     * Find matched advice executor.
     * 
     * @param methodDescription method description
     * @return found advice executor
     */
    public Optional<AdviceExecutor> findMatchedAdviceExecutor(final InDefinedShape methodDescription) {
        Map<String, Collection<AgentAdvice>> advices = new HashMap<>(advisorConfig.getAdvisors().size(), 1F);
        for (MethodAdvisorConfiguration each : advisorConfig.getAdvisors()) {
            if (each.getPointcut().matches(methodDescription)) {
                advices.computeIfAbsent(each.getPluginType(), key -> new LinkedList<>());
                advices.get(each.getPluginType()).add(adviceFactory.getAdvice(each.getAdviceClassName()));
            }
        }
        if (advices.isEmpty()) {
            return Optional.empty();
        }
        if (isConstructor(methodDescription)) {
            return Optional.of(new ConstructorAdviceExecutor(convert(advices)));
        }
        if (isStaticMethod(methodDescription)) {
            return Optional.of(new StaticMethodAdviceExecutor(convert(advices)));
        }
        if (isMethod(methodDescription)) {
            return Optional.of(new InstanceMethodAdviceExecutor(convert(advices)));
        }
        return Optional.empty();
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
    
    @SuppressWarnings("unchecked")
    private <T extends AgentAdvice> Map<String, Collection<T>> convert(final Map<String, Collection<AgentAdvice>> advices) {
        Map<String, Collection<T>> result = new HashMap<>(advices.size(), 1F);
        for (Entry<String, Collection<AgentAdvice>> entry : advices.entrySet()) {
            result.put(entry.getKey(), new LinkedList<>());
            for (AgentAdvice each : entry.getValue()) {
                result.get(entry.getKey()).add((T) each);
            }
        }
        return result;
    }
}
