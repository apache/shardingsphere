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

import org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.encrypt.spring.namespace.tag.EncryptRuleBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Encrypt rule bean definition parser.
 */
public final class EncryptRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptRuleConfiguration.class);
        factory.addConstructorArgValue(parseEncryptorConfigurations(element, parserContext));
        factory.addConstructorArgValue(parseEncryptTableRuleConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private static Map<String, BeanDefinition> parseEncryptorConfigurations(final Element element, final ParserContext parserContext) {
        Element encryptorsRuleElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.ENCRYPTORS_CONFIG_TAG);
        List<Element> encryptorElements = DomUtils.getChildElementsByTagName(encryptorsRuleElement, EncryptRuleBeanDefinitionTag.ENCRYPTOR_CONFIG_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(encryptorElements.size());
        for (Element each : encryptorElements) {
            result.put(each.getAttribute(BeanDefinitionParserDelegate.ID_ATTRIBUTE), parseEncryptorConfiguration(each, parserContext));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptorConfiguration(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptorConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.ENCRYPTOR_TYPE_ATTRIBUTE));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private static Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.ENCRYPTOR_PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
    
    private static Collection<BeanDefinition> parseEncryptTableRuleConfigurations(final Element element) {
        Element encryptTablesElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.TABLES_CONFIG_TAG);
        List<Element> encryptTableElements = DomUtils.getChildElementsByTagName(encryptTablesElement, EncryptRuleBeanDefinitionTag.TABLE_CONFIG_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptTableElements.size());
        for (Element each : encryptTableElements) {
            result.add(parseEncryptTableConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptTableConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptTableRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseEncryptColumnConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private static Map<String, BeanDefinition> parseEncryptColumnConfigurations(final Element element) {
        List<Element> encryptColumnElements = DomUtils.getChildElementsByTagName(element, EncryptRuleBeanDefinitionTag.COLUMN_CONFIG_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(encryptColumnElements.size());
        for (Element each : encryptColumnElements) {
            result.put(each.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_LOGIC_COLUMN_ATTRIBUTE), parseEncryptColumnConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptColumnConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptColumnConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_PLAIN_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_CIPHER_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_ASSISTED_QUERY_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_ENCRYPTOR_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
}
