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

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
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
import org.apache.shardingsphere.agent.core.transformer.builder.advise.AdviceFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Instance method advisor builder.
 */
public final class InstanceMethodAdvisorBuilder extends MethodAdvisorBuilder<InstanceMethodAdvisorConfiguration> {
    
    private final AdviceFactory adviceFactory;
    
    public InstanceMethodAdvisorBuilder(final Map<String, PluginConfiguration> pluginConfigs, final Collection<InstanceMethodAdvisorConfiguration> advisorConfigs,
                                        final boolean isEnhancedForProxy, final TypeDescription typePointcut, final ClassLoader classLoader) {
        super(advisorConfigs, typePointcut);
        adviceFactory = new AdviceFactory(classLoader, pluginConfigs, isEnhancedForProxy);
    }
    
    @Override
    protected Builder<?> create(Builder<?> builder, final MethodAdvisor methodAdvisor) {
        if (methodAdvisor.getAdvice() instanceof InstanceMethodInterceptorArgsOverride) {
            return builder.method(ElementMatchers.is(methodAdvisor.getPointcut()))
                    .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(methodAdvisor.getAdvice()));
        }
        return builder.method(ElementMatchers.is(methodAdvisor.getPointcut())).intercept(MethodDelegation.withDefaultConfiguration().to(methodAdvisor.getAdvice()));
    }
    
    @Override
    protected boolean isMatchedMethod(final InDefinedShape methodPointcut) {
        return !(methodPointcut.isAbstract() || methodPointcut.isSynthetic());
    }
    
    @Override
    protected MethodAdvisor getSingleMethodAdvisor(final InDefinedShape methodPointcut, final InstanceMethodAdvisorConfiguration advisorConfig) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = adviceFactory.getAdvice(advisorConfig.getAdviceClassName());
        Object advice = advisorConfig.isOverrideArgs() ? new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice) : new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice);
        return new MethodAdvisor(methodPointcut, advice);
    }
    
    @Override
    protected MethodAdvisor getComposedMethodAdvisor(final InDefinedShape methodPointcut, final List<InstanceMethodAdvisorConfiguration> advisorConfigs) {
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
        return new MethodAdvisor(methodPointcut, advice);
    }
}
