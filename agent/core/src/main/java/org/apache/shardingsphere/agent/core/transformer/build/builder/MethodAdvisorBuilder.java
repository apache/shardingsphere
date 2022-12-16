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

package org.apache.shardingsphere.agent.core.transformer.build.builder;

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.config.advisor.method.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;

import java.util.List;

/**
 * Method advisor builder.
 * 
 * @param <T> type of method advisor configuration
 */
public interface MethodAdvisorBuilder<T extends MethodAdvisorConfiguration> {
    
    /**
     * Create method advisor builder.
     * 
     * @param builder original builder
     * @param methodAdvisor method advisor
     * @return created builder
     */
    Builder<?> create(Builder<?> builder, MethodAdvisor methodAdvisor);
    
    /**
     * Judge whether method matched.
     * 
     * @param methodDescription method description
     * @return matched or not
     */
    boolean isMatchedMethod(InDefinedShape methodDescription);
    
    /**
     * Get single method advisor.
     * 
     * @param methodDescription method description
     * @param advisorConfig advisor configuration
     * @return got method advisor
     */
    MethodAdvisor getSingleMethodAdvisor(InDefinedShape methodDescription, T advisorConfig);
    
    /**
     * Get composed method advisor.
     * 
     * @param methodDescription method description
     * @param advisorConfigs advisor configurations
     * @return got method advisor
     */
    MethodAdvisor getComposedMethodAdvisor(InDefinedShape methodDescription, List<T> advisorConfigs);
}
