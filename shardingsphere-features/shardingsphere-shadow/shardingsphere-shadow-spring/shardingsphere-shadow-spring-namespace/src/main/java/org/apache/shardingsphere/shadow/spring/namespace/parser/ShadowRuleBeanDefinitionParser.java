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

package org.apache.shardingsphere.shadow.spring.namespace.parser;

import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spring.namespace.factorybean.ShadowAlgorithmFactoryBean;
import org.apache.shardingsphere.shadow.spring.namespace.tag.ShadowRuleBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.registry.ShardingSphereAlgorithmBeanRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Shadow rule parser for spring namespace.
 */
public final class ShadowRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    /**
     * Parse shadow rule element.
     *
     * @param element element
     * @return bean definition of shadow rule
     */
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedShadowRuleConfiguration.class);
        addConstructorArgValue(element, factory);
        factory.addPropertyValue("dataSources", parseDataSourcesConfiguration(element));
        factory.addPropertyValue("shadowTables", parseShadowTablesConfiguration(element));
        factory.addPropertyValue("shadowAlgorithms", ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ShadowAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private Map<String, BeanDefinition> parseShadowTablesConfiguration(final Element element) {
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(element, ShadowRuleBeanDefinitionTag.SHADOW_TABLE_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(tableRuleElements.size());
        for (Element each : tableRuleElements) {
            result.put(each.getAttribute(ShadowRuleBeanDefinitionTag.SHADOW_TABLE_NAME_ATTRIBUTE), parseShadowTableConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseShadowTableConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShadowTableConfiguration.class);
        factory.addConstructorArgValue(parseShadowAlgorithmNames(element));
        return factory.getBeanDefinition();
    }
    
    private Collection<String> parseShadowAlgorithmNames(final Element element) {
        List<Element> shadowAlgorithmElements = DomUtils.getChildElementsByTagName(element, ShadowRuleBeanDefinitionTag.SHADOW_TABLE_ALGORITHM_TAG);
        Collection<String> result = new ManagedList<>(shadowAlgorithmElements.size());
        for (Element each : shadowAlgorithmElements) {
            result.add(each.getAttribute(ShadowRuleBeanDefinitionTag.SHADOW_TABLE_ALGORITHM_REF_ATTRIBUTE));
        }
        return result;
    }
    
    private Map<String, BeanDefinition> parseDataSourcesConfiguration(final Element element) {
        List<Element> dataSourcesElements = DomUtils.getChildElementsByTagName(element, ShadowRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(dataSourcesElements.size());
        for (Element each : dataSourcesElements) {
            result.put(each.getAttribute(ShadowRuleBeanDefinitionTag.DATA_SOURCE_ID_ATTRIBUTE), parseDataSourceConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseDataSourceConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShadowDataSourceConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShadowRuleBeanDefinitionTag.SOURCE_DATA_SOURCE_NAME_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ShadowRuleBeanDefinitionTag.SHADOW_DATA_SOURCE_NAME_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    // fixme remove method when the api refactoring is complete
    private void addConstructorArgValue(final Element element, final BeanDefinitionBuilder factory) {
        factory.addConstructorArgValue(element.getAttribute(ShadowRuleBeanDefinitionTag.COLUMN_CONFIG_TAG));
        factory.addConstructorArgValue(element.getAttribute(ShadowRuleBeanDefinitionTag.SOURCE_DATASOURCE_NAMES_TAG).split(","));
        factory.addConstructorArgValue(element.getAttribute(ShadowRuleBeanDefinitionTag.SHADOW_DATASOURCE_NAMES_TAG).split(","));
    }
}
