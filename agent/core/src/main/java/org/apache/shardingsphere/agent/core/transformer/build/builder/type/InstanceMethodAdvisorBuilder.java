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

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.method.type.InstanceMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.core.plugin.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedInstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedInstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.build.advise.AdviceFactory;
import org.apache.shardingsphere.agent.core.transformer.build.builder.MethodAdvisorBuilder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Instance method advisor builder.
 */
public final class InstanceMethodAdvisorBuilder implements MethodAdvisorBuilder<InstanceMethodAdvisorConfiguration> {
    
    private final AdviceFactory adviceFactory;
    
    public InstanceMethodAdvisorBuilder(final Map<String, PluginConfiguration> pluginConfigs, final boolean isEnhancedForProxy, final ClassLoader classLoader) {
        adviceFactory = new AdviceFactory(classLoader, pluginConfigs, isEnhancedForProxy);
    }
    
    @Override
    public Builder<?> create(final Builder<?> builder, final MethodAdvisor methodAdvisor) {
        if (methodAdvisor.getAdvice() instanceof InstanceMethodInterceptorArgsOverride) {
            return builder.method(ElementMatchers.is(methodAdvisor.getPointcut()))
                    .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(methodAdvisor.getAdvice()));
        }
        return builder.method(ElementMatchers.is(methodAdvisor.getPointcut())).intercept(MethodDelegation.withDefaultConfiguration().to(methodAdvisor.getAdvice()));
    }
    
    @Override
    public boolean isMatchedMethod(final InDefinedShape methodDescription) {
        return !(methodDescription.isAbstract() || methodDescription.isSynthetic());
    }
    
    @Override
    public MethodAdvisor getSingleMethodAdvisor(final InDefinedShape methodDescription, final InstanceMethodAdvisorConfiguration advisorConfig) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = adviceFactory.getAdvice(advisorConfig.getAdviceClassName());
        Object advice = advisorConfig.isOverrideArgs() ? new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice) : new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice);
        return new MethodAdvisor(methodDescription, advice);
    }
    
    @Override
    public MethodAdvisor getComposedMethodAdvisor(final InDefinedShape methodDescription, final List<InstanceMethodAdvisorConfiguration> advisorConfigs) {
        Collection<InstanceMethodAroundAdvice> instanceMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (InstanceMethodAdvisorConfiguration each : advisorConfigs) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                instanceMethodAroundAdvices.add(adviceFactory.getAdvice(each.getAdviceClassName()));
            }
        }
        Object advice = isArgsOverride ? new ComposedInstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvices) : new ComposedInstanceMethodAroundInterceptor(instanceMethodAroundAdvices);
        return new MethodAdvisor(methodDescription, advice);
    }
}
