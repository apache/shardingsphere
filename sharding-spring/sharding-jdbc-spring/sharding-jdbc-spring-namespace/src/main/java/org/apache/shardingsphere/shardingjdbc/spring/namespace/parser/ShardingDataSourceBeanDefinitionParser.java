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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.spring.datasource.SpringShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Sharding data source parser for spring namespace.
 */
public final class ShardingDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringShardingDataSource.class);
        factory.addConstructorArgValue(parseDataSources(element));
        factory.addConstructorArgValue(parseRuleConfigurations(element));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        factory.setDestroyMethodName("close");
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final Element element) {
        Element shardingRuleElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.SHARDING_RULE_CONFIG_TAG);
        List<String> dataSources = Splitter.on(",").trimResults().splitToList(shardingRuleElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAMES_TAG));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(dataSources.size());
        for (String each : dataSources) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private Collection<BeanDefinition> parseRuleConfigurations(final Element element) {
        Element shardingRuleElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.SHARDING_RULE_CONFIG_TAG);
        BeanDefinition shardingRuleConfiguration = parseShardingRuleConfiguration(shardingRuleElement);
        List<BeanDefinition> masterSlaveRulesConfigurations = parseMasterSlaveRulesConfiguration(shardingRuleElement);
        Optional<BeanDefinition> encryptRuleConfiguration = parseEncryptRuleConfiguration(shardingRuleElement);
        Collection<BeanDefinition> result = new ManagedList<>(masterSlaveRulesConfigurations.size() + 2);
        result.add(shardingRuleConfiguration);
        result.addAll(masterSlaveRulesConfigurations);
        encryptRuleConfiguration.ifPresent(result::add);
        return result;
    }
    
    private BeanDefinition parseShardingRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingRuleConfiguration.class);
        parseDefaultDatabaseShardingStrategy(factory, element);
        parseDefaultTableShardingStrategy(factory, element);
        factory.addPropertyValue("tableRuleConfigs", parseTableRulesConfiguration(element));
        factory.addPropertyValue("bindingTableGroups", parseBindingTablesConfiguration(element));
        factory.addPropertyValue("broadcastTables", parseBroadcastTables(element));
        parseDefaultKeyGenerator(factory, element);
        return factory.getBeanDefinition();
    }
    
    private void parseDefaultKeyGenerator(final BeanDefinitionBuilder factory, final Element element) {
        String defaultKeyGeneratorConfig = element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DEFAULT_KEY_GENERATOR_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(defaultKeyGeneratorConfig)) {
            factory.addPropertyReference("defaultKeyGeneratorConfig", defaultKeyGeneratorConfig);
        }
    }
    
    private void parseDefaultDatabaseShardingStrategy(final BeanDefinitionBuilder factory, final Element element) {
        String defaultDatabaseShardingStrategy = element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DEFAULT_DATABASE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(defaultDatabaseShardingStrategy)) {
            factory.addPropertyReference("defaultDatabaseShardingStrategyConfig", defaultDatabaseShardingStrategy);
        }
    }
    
    private void parseDefaultTableShardingStrategy(final BeanDefinitionBuilder factory, final Element element) {
        String defaultTableShardingStrategy = element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DEFAULT_TABLE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(defaultTableShardingStrategy)) {
            factory.addPropertyReference("defaultTableShardingStrategyConfig", defaultTableShardingStrategy);
        }
    }
    
    private List<BeanDefinition> parseMasterSlaveRulesConfiguration(final Element element) {
        Element masterSlaveRulesElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.MASTER_SLAVE_RULES_TAG);
        if (null == masterSlaveRulesElement) {
            return new LinkedList<>();
        }
        List<Element> masterSlaveRuleElements = DomUtils.getChildElementsByTagName(masterSlaveRulesElement, ShardingDataSourceBeanDefinitionParserTag.MASTER_SLAVE_RULE_TAG);
        List<BeanDefinition> result = new ManagedList<>(masterSlaveRuleElements.size());
        for (Element each : masterSlaveRuleElements) {
            result.add(new MasterSlaveRuleConfigurationBeanDefinition(each).getBeanDefinition());
        }
        return result;
    }
    
    private Optional<BeanDefinition> parseEncryptRuleConfiguration(final Element element) {
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_TAG);
        return null == encryptRuleElement ? Optional.empty() : Optional.of(EncryptRuleBeanDefinitionParser.parseEncryptRuleElement(encryptRuleElement));
    }
    
    private List<BeanDefinition> parseTableRulesConfiguration(final Element element) {
        Element tableRulesElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.TABLE_RULES_TAG);
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(tableRulesElement, ShardingDataSourceBeanDefinitionParserTag.TABLE_RULE_TAG);
        List<BeanDefinition> result = new ManagedList<>(tableRuleElements.size());
        for (Element each : tableRuleElements) {
            result.add(parseTableRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseTableRuleConfiguration(final Element tableElement) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TableRuleConfiguration.class);
        factory.addConstructorArgValue(tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.LOGIC_TABLE_ATTRIBUTE));
        parseActualDataNodes(tableElement, factory);
        parseDatabaseShardingStrategyConfiguration(tableElement, factory);
        parseTableShardingStrategyConfiguration(tableElement, factory);
        parseKeyGeneratorConfiguration(tableElement, factory);
        parseLogicIndex(tableElement, factory);
        return factory.getBeanDefinition();
    }
    
    private void parseActualDataNodes(final Element tableElement, final BeanDefinitionBuilder factory) {
        String actualDataNodes = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.ACTUAL_DATA_NODES_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(actualDataNodes)) {
            factory.addConstructorArgValue(actualDataNodes);
        }
    }
    
    private void parseDatabaseShardingStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String databaseStrategy = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATABASE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(databaseStrategy)) {
            factory.addPropertyReference("databaseShardingStrategyConfig", databaseStrategy);
        }
    }
    
    private void parseTableShardingStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String tableStrategy = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.TABLE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(tableStrategy)) {
            factory.addPropertyReference("tableShardingStrategyConfig", tableStrategy);
        }
    }
    
    private void parseKeyGeneratorConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String keyGenerator = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.KEY_GENERATOR_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(keyGenerator)) {
            factory.addPropertyReference("keyGeneratorConfig", keyGenerator);
        }
    }
    
    private void parseLogicIndex(final Element tableElement, final BeanDefinitionBuilder factory) {
        String logicIndex = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.LOGIC_INDEX);
        if (!Strings.isNullOrEmpty(logicIndex)) {
            factory.addPropertyValue("logicIndex", logicIndex);
        }
    }
    
    private List<String> parseBindingTablesConfiguration(final Element element) {
        Element bindingTableRulesElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.BINDING_TABLE_RULES_TAG);
        if (null == bindingTableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> bindingTableRuleElements = DomUtils.getChildElementsByTagName(bindingTableRulesElement, ShardingDataSourceBeanDefinitionParserTag.BINDING_TABLE_RULE_TAG);
        List<String> result = new LinkedList<>();
        for (Element each : bindingTableRuleElements) {
            result.add(each.getAttribute(ShardingDataSourceBeanDefinitionParserTag.LOGIC_TABLES_ATTRIBUTE));
        }
        return result;
    }
    
    private List<String> parseBroadcastTables(final Element element) {
        Element broadcastTableRulesElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.BROADCAST_TABLE_RULES_TAG);
        if (null == broadcastTableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> broadcastTableRuleElements = DomUtils.getChildElementsByTagName(broadcastTableRulesElement, ShardingDataSourceBeanDefinitionParserTag.BROADCAST_TABLE_RULE_TAG);
        List<String> result = new LinkedList<>();
        for (Element each : broadcastTableRuleElements) {
            result.add(each.getAttribute(ShardingDataSourceBeanDefinitionParserTag.TABLE_ATTRIBUTE));
        }
        return result;
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
