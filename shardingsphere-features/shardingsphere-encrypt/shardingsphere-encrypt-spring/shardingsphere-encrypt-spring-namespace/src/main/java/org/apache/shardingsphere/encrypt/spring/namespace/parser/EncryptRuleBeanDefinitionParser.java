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

package org.apache.shardingsphere.encrypt.spring.namespace.parser;

import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spring.namespace.factorybean.EncryptAlgorithmFactoryBean;
import org.apache.shardingsphere.encrypt.spring.namespace.tag.EncryptRuleBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.registry.ShardingSphereAlgorithmBeanRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

/**
 * Encrypt rule bean definition parser.
 */
public final class EncryptRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedEncryptRuleConfiguration.class);
        factory.addConstructorArgValue(parseEncryptTableRuleConfigurations(element));
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, EncryptAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private static Collection<BeanDefinition> parseEncryptTableRuleConfigurations(final Element element) {
        List<Element> encryptTableElements = DomUtils.getChildElementsByTagName(element, EncryptRuleBeanDefinitionTag.TABLE_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptTableElements.size());
        for (Element each : encryptTableElements) {
            result.add(parseEncryptTableRuleConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptTableRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptTableRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseEncryptColumnRuleConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private static Collection<BeanDefinition> parseEncryptColumnRuleConfigurations(final Element element) {
        List<Element> encryptColumnElements = DomUtils.getChildElementsByTagName(element, EncryptRuleBeanDefinitionTag.COLUMN_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptColumnElements.size());
        for (Element each : encryptColumnElements) {
            result.add(parseEncryptColumnRuleConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptColumnRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptColumnRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.LOGIC_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.CIPHER_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.ASSISTED_QUERY_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.PLAIN_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.ENCRYPT_ALGORITHM_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
}
