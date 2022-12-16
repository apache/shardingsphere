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

package org.apache.shardingsphere.agent.core.transformer.build.builder.type;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.build.advise.AdviceFactory;
import org.apache.shardingsphere.agent.core.transformer.build.builder.MethodAdvisorBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Static method advisor builder.
 */
@RequiredArgsConstructor
public final class StaticMethodAdvisorBuilder implements MethodAdvisorBuilder {
    
    private final AdviceFactory adviceFactory;
    
    @Override
    public Builder<?> create(final Builder<?> builder, final MethodAdvisor methodAdvisor) {
        return builder.method(ElementMatchers.is(methodAdvisor.getPointcut())).intercept(MethodDelegation.withDefaultConfiguration().to(methodAdvisor.getAdvice()));
    }
    
    @Override
    public boolean isMatchedMethod(final InDefinedShape methodDescription) {
        return methodDescription.isStatic() && !(methodDescription.isAbstract() || methodDescription.isSynthetic());
    }
    
    @Override
    public MethodAdvisor getSingleMethodAdvisor(final InDefinedShape methodDescription, final MethodAdvisorConfiguration advisorConfig) {
        return new MethodAdvisor(methodDescription, new StaticMethodAroundInterceptor(adviceFactory.getAdvice(advisorConfig.getAdviceClassName())));
    }
    
    @Override
    public MethodAdvisor getComposedMethodAdvisor(final InDefinedShape methodDescription, final List<MethodAdvisorConfiguration> advisorConfigs) {
        Collection<StaticMethodAroundAdvice> advices = advisorConfigs.stream().<StaticMethodAroundAdvice>map(each -> adviceFactory.getAdvice(each.getAdviceClassName())).collect(Collectors.toList());
        return new MethodAdvisor(methodDescription, new ComposedStaticMethodAroundInterceptor(advices));
    }
}
