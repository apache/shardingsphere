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
import org.apache.shardingsphere.agent.api.advice.type.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.api.advice.type.StaticMethodAdvice;
import org.apache.shardingsphere.agent.core.advisor.executor.type.ConstructorAdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.type.StaticMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.classloader.ClassLoaderContext;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.executor.type.InstanceMethodAdviceExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Collection<AgentAdvice> advices = advisorConfig.getAdvisors().stream()
                .filter(each -> each.getPointcut().matches(methodDescription)).map(each -> adviceFactory.getAdvice(each.getAdviceClassName())).collect(Collectors.toList());
        if (isConstructor(methodDescription)) {
            return Optional.of(new ConstructorAdviceExecutor(advices.stream().map(each -> (ConstructorAdvice) each).collect(Collectors.toList())));
        }
        if (isStaticMethod(methodDescription)) {
            return Optional.of(new StaticMethodAdviceExecutor(advices.stream().map(each -> (StaticMethodAdvice) each).collect(Collectors.toList())));
        }
        if (isMethod(methodDescription)) {
            return Optional.of(new InstanceMethodAdviceExecutor(advices.stream().map(each -> (InstanceMethodAdvice) each).collect(Collectors.toList())));
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
}
