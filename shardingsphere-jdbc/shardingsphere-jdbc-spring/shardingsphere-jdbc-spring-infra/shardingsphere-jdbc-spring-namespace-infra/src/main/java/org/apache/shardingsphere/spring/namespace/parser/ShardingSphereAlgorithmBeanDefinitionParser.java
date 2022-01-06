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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.spring.namespace.factorybean.ShardingSphereAlgorithmFactoryBean;
import org.apache.shardingsphere.spring.namespace.tag.ShardingSphereAlgorithmBeanDefinitionTag;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Properties;

/**
 * ShardingSphere algorithm bean parser for spring namespace.
 */
@RequiredArgsConstructor
public final class ShardingSphereAlgorithmBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    private final Class<? extends ShardingSphereAlgorithmFactoryBean<?>> beanClass;
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        factory.addConstructorArgValue(element.getAttribute(ShardingSphereAlgorithmBeanDefinitionTag.TYPE_ATTRIBUTE));
        factory.addConstructorArgValue(parsePropsElement(element, parserContext));
        filledDiscoveryTypePropToRuleConfig(parserContext, element.getAttribute(ShardingSphereAlgorithmBeanDefinitionTag.ID_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private void filledDiscoveryTypePropToRuleConfig(final ParserContext parserContext, final String elementId) {
        String[] beanDefinitionNames = parserContext.getRegistry().getBeanDefinitionNames();
        for (String each : beanDefinitionNames) {
            BeanDefinition beanDefinition = parserContext.getRegistry().getBeanDefinition(each);
            if (null != beanDefinition.getBeanClassName() && beanDefinition.getBeanClassName().equals(AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class.getName())) {
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                ManagedMap<String, RuntimeBeanReference> managedMap = new ManagedMap<>();
                managedMap.put(elementId, new RuntimeBeanReference(elementId));
                propertyValues.setPropertyValueAt(new PropertyValue("discoveryTypes", managedMap), 2);
            }
        }
    }
    
    private Properties parsePropsElement(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingSphereAlgorithmBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
