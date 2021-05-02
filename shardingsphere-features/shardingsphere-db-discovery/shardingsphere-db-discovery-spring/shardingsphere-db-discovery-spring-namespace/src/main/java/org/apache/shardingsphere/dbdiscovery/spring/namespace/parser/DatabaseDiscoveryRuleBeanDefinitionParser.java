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

package org.apache.shardingsphere.dbdiscovery.spring.namespace.parser;

import org.apache.shardingsphere.dbdiscovery.common.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.tag.DatabaseDiscoveryRuleBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Database discovery rule bean definition parser.
 */
public final class DatabaseDiscoveryRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class);
        factory.addConstructorArgValue(parseHADataSourceRuleConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseHADataSourceRuleConfigurations(final Element element) {
        List<Element> dataSourceElements = DomUtils.getChildElementsByTagName(element, DatabaseDiscoveryRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(dataSourceElements.size());
        for (Element each : dataSourceElements) {
            result.add(parseHADataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseHADataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(DatabaseDiscoveryRuleBeanDefinitionTag.DB_DISCOVERY_DATA_SOURCE_ID_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
}
