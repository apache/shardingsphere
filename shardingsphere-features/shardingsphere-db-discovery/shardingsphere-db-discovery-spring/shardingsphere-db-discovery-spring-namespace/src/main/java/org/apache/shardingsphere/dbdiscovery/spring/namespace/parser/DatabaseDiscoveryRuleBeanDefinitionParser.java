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

import com.google.common.base.Splitter;
import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.factorybean.DatabaseDiscoveryAlgorithmFactoryBean;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.tag.DatabaseDiscoveryHeartbeatBeanDefinitionTag;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.tag.DatabaseDiscoveryRuleBeanDefinitionTag;
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
import java.util.Properties;

/**
 * Database discovery rule bean definition parser.
 */
public final class DatabaseDiscoveryRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class);
        factory.addPropertyValue("dataSources", parseDatabaseDiscoveryDataSourceRuleConfigurations(element));
        factory.addPropertyValue("discoveryHeartbeats", parseDiscoveryHeartbeatsConfigurations(element, parserContext));
        factory.addPropertyValue("discoveryTypes", ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, DatabaseDiscoveryAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseDatabaseDiscoveryDataSourceRuleConfigurations(final Element element) {
        List<Element> dataSourceElements = DomUtils.getChildElementsByTagName(element, DatabaseDiscoveryRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(dataSourceElements.size());
        for (Element each : dataSourceElements) {
            result.add(parseDatabaseDiscoveryDataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseDatabaseDiscoveryDataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(DatabaseDiscoveryRuleBeanDefinitionTag.DB_DISCOVERY_DATA_SOURCE_ID_ATTRIBUTE));
        factory.addConstructorArgValue(parseDataSources(element));
        factory.addConstructorArgValue(element.getAttribute(DatabaseDiscoveryRuleBeanDefinitionTag.DB_DISCOVERY_HEARTBEAT_NAME_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(DatabaseDiscoveryRuleBeanDefinitionTag.DB_DISCOVERY_TYPE_NAME_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private Collection<String> parseDataSources(final Element element) {
        List<String> dataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(DatabaseDiscoveryRuleBeanDefinitionTag.DB_DISCOVERY_DATASOURCE_NAMES_ATTRIBUTE));
        Collection<String> result = new ManagedList<>(dataSources.size());
        result.addAll(dataSources);
        return result;
    }
    
    private Map<String, BeanDefinition> parseDiscoveryHeartbeatsConfigurations(final Element element, final ParserContext parserContext) {
        List<Element> heartbeatElements = DomUtils.getChildElementsByTagName(element, DatabaseDiscoveryHeartbeatBeanDefinitionTag.ROOT_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(heartbeatElements.size());
        for (Element each : heartbeatElements) {
            result.put(each.getAttribute(DatabaseDiscoveryHeartbeatBeanDefinitionTag.HEARTBEAT_ID_ATTRIBUTE), parseDiscoveryHeartbeatConfiguration(each, parserContext));
        }
        return result;
    }
    
    private BeanDefinition parseDiscoveryHeartbeatConfiguration(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(DatabaseDiscoveryHeartBeatConfiguration.class);
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, DatabaseDiscoveryHeartbeatBeanDefinitionTag.HEARTBEAT_PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
