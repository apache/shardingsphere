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

package org.apache.shardingsphere.sharding.spring.namespace.parser.rule;

import com.google.common.base.Strings;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.spring.namespace.factorybean.KeyGenerateAlgorithmFactoryBean;
import org.apache.shardingsphere.sharding.spring.namespace.factorybean.ShardingAlgorithmFactoryBean;
import org.apache.shardingsphere.sharding.spring.namespace.tag.rule.ShardingRuleBeanDefinitionTag;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.spring.namespace.registry.ShardingSphereAlgorithmBeanRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding rule parser for spring namespace.
 */
public final class ShardingRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedShardingRuleConfiguration.class);
        factory.addConstructorArgValue(parseTableRulesConfiguration(element));
        factory.addConstructorArgValue(parseAutoTableRulesConfiguration(element));
        factory.addConstructorArgValue(parseBindingTablesConfiguration(element));
        factory.addConstructorArgValue(parseBroadcastTables(element));
        addDefaultDatabaseShardingStrategyRef(element, factory);
        addDefaultTableShardingStrategyRef(element, factory);
        addDefaultKeyGenerateStrategyRef(element, factory);
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ShardingAlgorithmFactoryBean.class));
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, KeyGenerateAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseTableRulesConfiguration(final Element element) {
        Element tableRulesElement = DomUtils.getChildElementByTagName(element, ShardingRuleBeanDefinitionTag.TABLE_RULES_TAG);
        if (null == tableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(tableRulesElement, ShardingRuleBeanDefinitionTag.TABLE_RULE_TAG);
        List<BeanDefinition> result = new ManagedList<>(tableRuleElements.size());
        for (Element each : tableRuleElements) {
            result.add(parseTableRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseTableRuleConfiguration(final Element tableElement) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingTableRuleConfiguration.class);
        factory.addConstructorArgValue(tableElement.getAttribute(ShardingRuleBeanDefinitionTag.LOGIC_TABLE_ATTRIBUTE));
        parseActualDataNodes(tableElement, factory);
        parseDatabaseShardingStrategyConfiguration(tableElement, factory);
        parseTableShardingStrategyConfiguration(tableElement, factory);
        parseKeyGenerateStrategyConfiguration(tableElement, factory);
        return factory.getBeanDefinition();
    }
    
    private void parseActualDataNodes(final Element tableElement, final BeanDefinitionBuilder factory) {
        String actualDataNodes = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.ACTUAL_DATA_NODES_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(actualDataNodes)) {
            factory.addConstructorArgValue(actualDataNodes);
        }
    }
    
    private void parseDatabaseShardingStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String databaseStrategy = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.DATABASE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(databaseStrategy)) {
            factory.addPropertyReference("databaseShardingStrategy", databaseStrategy);
        }
    }
    
    private void parseTableShardingStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String tableStrategy = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.TABLE_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(tableStrategy)) {
            factory.addPropertyReference("tableShardingStrategy", tableStrategy);
        }
    }
    
    private List<BeanDefinition> parseAutoTableRulesConfiguration(final Element element) {
        Element tableRulesElement = DomUtils.getChildElementByTagName(element, ShardingRuleBeanDefinitionTag.AUTO_TABLE_RULES_TAG);
        if (null == tableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(tableRulesElement, ShardingRuleBeanDefinitionTag.AUTO_TABLE_RULE_TAG);
        List<BeanDefinition> result = new ManagedList<>(tableRuleElements.size());
        for (Element each : tableRuleElements) {
            result.add(parseAutoTableRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseAutoTableRuleConfiguration(final Element tableElement) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingAutoTableRuleConfiguration.class);
        factory.addConstructorArgValue(tableElement.getAttribute(ShardingRuleBeanDefinitionTag.LOGIC_TABLE_ATTRIBUTE));
        parseActualDataSources(tableElement, factory);
        parseShardingStrategyConfiguration(tableElement, factory);
        parseKeyGenerateStrategyConfiguration(tableElement, factory);
        return factory.getBeanDefinition();
    }
    
    private void parseActualDataSources(final Element tableElement, final BeanDefinitionBuilder factory) {
        String actualDataNodes = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.ACTUAL_DATA_SOURCES_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(actualDataNodes)) {
            factory.addConstructorArgValue(actualDataNodes);
        }
    }
    
    private void parseShardingStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String databaseStrategy = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.SHARDING_STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(databaseStrategy)) {
            factory.addPropertyReference("shardingStrategy", databaseStrategy);
        }
    }
    
    private void parseKeyGenerateStrategyConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String keyGenerator = tableElement.getAttribute(ShardingRuleBeanDefinitionTag.STRATEGY_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(keyGenerator)) {
            factory.addPropertyReference("keyGenerateStrategy", keyGenerator);
        }
    }
    
    private List<String> parseBindingTablesConfiguration(final Element element) {
        Element bindingTableRulesElement = DomUtils.getChildElementByTagName(element, ShardingRuleBeanDefinitionTag.BINDING_TABLE_RULES_TAG);
        if (null == bindingTableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> bindingTableRuleElements = DomUtils.getChildElementsByTagName(bindingTableRulesElement, ShardingRuleBeanDefinitionTag.BINDING_TABLE_RULE_TAG);
        List<String> result = new LinkedList<>();
        for (Element each : bindingTableRuleElements) {
            result.add(each.getAttribute(ShardingRuleBeanDefinitionTag.LOGIC_TABLES_ATTRIBUTE));
        }
        return result;
    }
    
    private List<String> parseBroadcastTables(final Element element) {
        Element broadcastTableRulesElement = DomUtils.getChildElementByTagName(element, ShardingRuleBeanDefinitionTag.BROADCAST_TABLE_RULES_TAG);
        if (null == broadcastTableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> broadcastTableRuleElements = DomUtils.getChildElementsByTagName(broadcastTableRulesElement, ShardingRuleBeanDefinitionTag.BROADCAST_TABLE_RULE_TAG);
        List<String> result = new LinkedList<>();
        for (Element each : broadcastTableRuleElements) {
            result.add(each.getAttribute(ShardingRuleBeanDefinitionTag.TABLE_ATTRIBUTE));
        }
        return result;
    }
    
    private void addDefaultDatabaseShardingStrategyRef(final Element element, final BeanDefinitionBuilder factory) {
        Optional<String> strategyRef = parseStrategyRef(element, ShardingRuleBeanDefinitionTag.DEFAULT_DATABASE_STRATEGY_REF_ATTRIBUTE);
        if (strategyRef.isPresent()) {
            factory.addConstructorArgReference(strategyRef.get());
        } else {
            factory.addConstructorArgValue(null);
        }
    }
    
    private void addDefaultTableShardingStrategyRef(final Element element, final BeanDefinitionBuilder factory) {
        Optional<String> strategyRef = parseStrategyRef(element, ShardingRuleBeanDefinitionTag.DEFAULT_TABLE_STRATEGY_REF_ATTRIBUTE);
        if (strategyRef.isPresent()) {
            factory.addConstructorArgReference(strategyRef.get());
        } else {
            factory.addConstructorArgValue(null);
        }
    }
    
    private void addDefaultKeyGenerateStrategyRef(final Element element, final BeanDefinitionBuilder factory) {
        Optional<String> strategyRef = parseStrategyRef(element, ShardingRuleBeanDefinitionTag.DEFAULT_KEY_GENERATE_STRATEGY_REF_ATTRIBUTE);
        if (strategyRef.isPresent()) {
            factory.addConstructorArgReference(strategyRef.get());
        } else {
            factory.addConstructorArgValue(null);
        }
    }
    
    private Optional<String> parseStrategyRef(final Element element, final String tagName) {
        String result = element.getAttribute(tagName);
        return Strings.isNullOrEmpty(result) ? Optional.empty() : Optional.of(result);
    }
}
