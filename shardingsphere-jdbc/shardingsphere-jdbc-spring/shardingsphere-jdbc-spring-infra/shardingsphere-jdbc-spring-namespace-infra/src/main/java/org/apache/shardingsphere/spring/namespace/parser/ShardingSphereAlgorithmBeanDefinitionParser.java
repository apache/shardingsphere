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
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.spring.namespace.tag.ShardingSphereAlgorithmBeanDefinitionTag;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.FactoryBean;
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
    
    private final Class<? extends FactoryBean<?>> beanClass;
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        factory.addConstructorArgValue(element.getAttribute(ShardingSphereAlgorithmBeanDefinitionTag.TYPE_ATTRIBUTE));
        factory.addConstructorArgValue(parsePropsElement(element, parserContext));
        filledPropToRuleConfig(parserContext, element);
        return factory.getBeanDefinition();
    }
    
    private void filledPropToRuleConfig(final ParserContext parserContext, final Element element) {
        switch (element.getTagName()) {
            case "database-discovery:discovery-type":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class, "discoveryTypes");
                break;
            case "encrypt:encrypt-algorithm":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedEncryptRuleConfiguration.class, "encryptors");
                break;
            case "readwrite-splitting:load-balance-algorithm":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedReadwriteSplittingRuleConfiguration.class, "loadBalanceAlgorithms");
                break;
            case "shadow:shadow-algorithm":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedShadowRuleConfiguration.class, "shadowAlgorithms");
                break;
            case "sharding:sharding-algorithm":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedShardingRuleConfiguration.class, "shardingAlgorithms");
                break;
            case "sharding:key-generate-algorithm":
                setPropertyValue(parserContext, element.getAttribute(ID_ATTRIBUTE), AlgorithmProvidedShardingRuleConfiguration.class, "keyGenerators");
                break;
            default:
                break;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setPropertyValue(final ParserContext parserContext, final String elementId, final Class<? extends DatabaseRuleConfiguration> ruleConfigClass, final String propertyName) {
        String[] beanDefinitionNames = parserContext.getRegistry().getBeanDefinitionNames();
        for (String each : beanDefinitionNames) {
            BeanDefinition beanDefinition = parserContext.getRegistry().getBeanDefinition(each);
            if (null != beanDefinition.getBeanClassName() && beanDefinition.getBeanClassName().equals(ruleConfigClass.getName())) {
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                PropertyValue propertyValue = propertyValues.getPropertyValue(propertyName);
                if (null != propertyValue && propertyValue.getValue() instanceof ManagedMap) {
                    ((ManagedMap<String, RuntimeBeanReference>) propertyValue.getValue()).put(elementId, new RuntimeBeanReference(elementId));
                }
            }
        }
    }
    
    private Properties parsePropsElement(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingSphereAlgorithmBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
