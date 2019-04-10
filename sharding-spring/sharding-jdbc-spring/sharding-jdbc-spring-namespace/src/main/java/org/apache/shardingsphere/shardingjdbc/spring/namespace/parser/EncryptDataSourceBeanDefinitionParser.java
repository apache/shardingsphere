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

package org.apache.shardingsphere.shardingjdbc.spring.namespace.parser;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.spring.datasource.SpringEncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * Encrypt data source parser for spring namespace.
 * 
 * @author panjuan
 */
public final class EncryptDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringEncryptDataSource.class);
        factory.addConstructorArgValue(parseDataSource(element));
        factory.addConstructorArgValue(parseEncryptRuleConfiguration(element));
        factory.setDestroyMethodName("close");
        return factory.getBeanDefinition();
    }
    
    private RuntimeBeanReference parseDataSource(final Element element) {
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_CONFIG_TAG);
        String dataSource = encryptRuleElement.getAttribute(EncryptDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAME_TAG);
        return new RuntimeBeanReference(dataSource);
    }
    
    private BeanDefinition parseEncryptRuleConfiguration(final Element element) {
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_CONFIG_TAG);
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptRuleConfiguration.class);
        factory.addConstructorArgValue(parseEncryptorRulesConfiguration(encryptRuleElement));
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseEncryptorRulesConfiguration(final Element element) {
        List<Element> encryptorRulesElement = DomUtils.getChildElementsByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPTOR_RULE_CONFIG_TAG);
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(encryptorRulesElement.size());
        for (Element each : encryptorRulesElement) {
            result.put(each.getAttribute(ID_ATTRIBUTE), new RuntimeBeanReference(each.getAttribute(ID_ATTRIBUTE)));
        }
        return result;
    }
}
