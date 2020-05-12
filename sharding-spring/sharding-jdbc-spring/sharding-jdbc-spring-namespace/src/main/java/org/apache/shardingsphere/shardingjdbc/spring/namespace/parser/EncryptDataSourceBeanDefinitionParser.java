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

import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Encrypt data source parser for spring namespace.
 */
public final class EncryptDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingSphereDataSource.class);
        factory.addConstructorArgValue(parseDataSources(element));
        factory.addConstructorArgValue(parseRuleConfigurations(element));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        factory.setDestroyMethodName("close");
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final Element element) {
        String dataSource = element.getAttribute(EncryptDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAME_TAG);
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(1);
        result.put(DefaultSchema.LOGIC_NAME, new RuntimeBeanReference(dataSource));
        return result;
    }
    
    private Collection<BeanDefinition> parseRuleConfigurations(final Element element) {
        Collection<BeanDefinition> result = new ManagedList<>(1);
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_TAG);
        result.add(EncryptRuleBeanDefinitionParser.parseEncryptRuleElement(encryptRuleElement));
        return result;
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
