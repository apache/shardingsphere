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
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.method.type.ConstructorAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedConstructorInterceptor;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.build.advise.AdviceFactory;
import org.apache.shardingsphere.agent.core.transformer.build.builder.MethodAdvisorBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constructor advisor builder.
 */
public final class ConstructorAdvisorBuilder implements MethodAdvisorBuilder<ConstructorAdvisorConfiguration> {
    
    private final AdviceFactory adviceFactory;
    
    public ConstructorAdvisorBuilder(final Map<String, PluginConfiguration> pluginConfigs, final boolean isEnhancedForProxy, final ClassLoader classLoader) {
        adviceFactory = new AdviceFactory(classLoader, pluginConfigs, isEnhancedForProxy);
    }
    
    @Override
    public Builder<?> create(final Builder<?> builder, final MethodAdvisor methodAdvisor) {
        return builder.constructor(ElementMatchers.is(methodAdvisor.getPointcut()))
                .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(methodAdvisor.getAdvice())));
    }
    
    @Override
    public boolean isMatchedMethod(final InDefinedShape methodDescription) {
        return methodDescription.isConstructor();
    }
    
    @Override
    public MethodAdvisor getSingleMethodAdvisor(final InDefinedShape methodDescription, final ConstructorAdvisorConfiguration advisorConfig) {
        return new MethodAdvisor(methodDescription, new ConstructorInterceptor(adviceFactory.getAdvice(advisorConfig.getAdviceClassName())));
    }
    
    @Override
    public MethodAdvisor getComposedMethodAdvisor(final InDefinedShape methodDescription, final List<ConstructorAdvisorConfiguration> advisorConfigs) {
        Collection<ConstructorAdvice> advices = advisorConfigs
                .stream().map(ConstructorAdvisorConfiguration::getAdviceClassName).map(each -> (ConstructorAdvice) adviceFactory.getAdvice(each)).collect(Collectors.toList());
        return new MethodAdvisor(methodDescription, new ComposedConstructorInterceptor(advices));
    }
}
