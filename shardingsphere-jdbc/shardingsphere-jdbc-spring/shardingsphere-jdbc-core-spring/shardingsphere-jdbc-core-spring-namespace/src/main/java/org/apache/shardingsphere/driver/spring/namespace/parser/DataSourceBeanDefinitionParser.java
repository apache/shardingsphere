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

package org.apache.shardingsphere.driver.spring.namespace.parser;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.driver.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.driver.spring.namespace.parser.rule.EncryptRuleBeanDefinitionParser;
import org.apache.shardingsphere.driver.spring.namespace.parser.rule.MasterSlaveDataSourceConfigurationBeanDefinition;
import org.apache.shardingsphere.driver.spring.namespace.parser.rule.ShardingDataSourceBeanDefinitionParser;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * ShardingSphere data source parser for spring namespace.
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
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
        List<String> dataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAMES_TAG));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(dataSources.size());
        for (String each : dataSources) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private Collection<BeanDefinition> parseRuleConfigurations(final Element element) {
        Collection<BeanDefinition> result = new ManagedList<>(3);
        parseShardingRuleConfiguration(element).ifPresent(result::add);
        parseMasterSlaveRuleConfiguration(element).ifPresent(result::add);
        parseEncryptRuleConfiguration(element).ifPresent(result::add);
        return result;
    }
    
    private Optional<BeanDefinition> parseShardingRuleConfiguration(final Element element) {
        return ShardingDataSourceBeanDefinitionParser.parseShardingRuleConfiguration(element);
    }
    
    private Optional<BeanDefinition> parseMasterSlaveRuleConfiguration(final Element element) {
        Element masterSlaveRuleElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.MASTER_SLAVE_RULE_TAG);
        if (null == masterSlaveRuleElement) {
            return Optional.empty();
        }
        List<Element> masterSlaveDataSourceElements = DomUtils.getChildElementsByTagName(masterSlaveRuleElement, ShardingDataSourceBeanDefinitionParserTag.MASTER_SLAVE_DATA_SOURCE_TAG);
        List<BeanDefinition> masterSlaveDataSources = new ManagedList<>(masterSlaveDataSourceElements.size());
        for (Element each : masterSlaveDataSourceElements) {
            masterSlaveDataSources.add(new MasterSlaveDataSourceConfigurationBeanDefinition(each).getBeanDefinition());
        }
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveRuleConfiguration.class);
        factory.addConstructorArgValue(masterSlaveDataSources);
        return Optional.of(factory.getBeanDefinition());
    }
    
    private Optional<BeanDefinition> parseEncryptRuleConfiguration(final Element element) {
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_TAG);
        return null == encryptRuleElement ? Optional.empty() : Optional.of(EncryptRuleBeanDefinitionParser.parseEncryptRuleElement(encryptRuleElement));
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
