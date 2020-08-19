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

package org.apache.shardingsphere.spring.namespace.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spring.namespace.factorybean.ShardingSphereAlgorithmFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;

import java.util.Map;

/**
 * ShardingSphere algorithm bean registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereAlgorithmBeanRegistry {
    
    /**
     * Get algorithm bean references.
     * 
     * @param parserContext parser context for spring application context
     * @param algorithmFactoryBeanClass ShardingSphere algorithm factory bean class
     * @return ShardingSphere algorithm bean references
     */
    public static Map<String, RuntimeBeanReference> getAlgorithmBeanReferences(final ParserContext parserContext, 
                                                                               final Class<? extends ShardingSphereAlgorithmFactoryBean<?>> algorithmFactoryBeanClass) {
        String[] beanDefinitionNames = parserContext.getRegistry().getBeanDefinitionNames();
        String algorithmFactoryBeanClassName = algorithmFactoryBeanClass.getName();
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(beanDefinitionNames.length);
        for (String each : beanDefinitionNames) {
            if (parserContext.getRegistry().getBeanDefinition(each).getBeanClassName().equals(algorithmFactoryBeanClassName)) {
                result.put(each, new RuntimeBeanReference(each));
            }
        }
        return result;
    }
}
