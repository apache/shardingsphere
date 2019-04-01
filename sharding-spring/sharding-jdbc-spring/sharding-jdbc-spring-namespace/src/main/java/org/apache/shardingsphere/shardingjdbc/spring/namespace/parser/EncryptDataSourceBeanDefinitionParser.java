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

import com.google.common.base.Strings;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.spring.datasource.SpringEncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
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
import java.util.Properties;

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
        Element shardingRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_CONFIG_TAG);
        String dataSource = shardingRuleElement.getAttribute(EncryptDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAME_TAG);
        return new RuntimeBeanReference(dataSource);
    }
    
    private BeanDefinition parseEncryptRuleConfiguration(final Element element) {
        Element encryptRuleElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.ENCRYPT_RULE_CONFIG_TAG);
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptRuleConfiguration.class);
        parseDefaultEncryptor(factory, encryptRuleElement);
        
        factory.addPropertyValue("tableRuleConfigs", parseTableRulesConfiguration(encryptRuleElement));
        return factory.getBeanDefinition();
    }
    
    private void parseDefaultEncryptor(final BeanDefinitionBuilder factory, final Element element) {
        String defaultEncryptorConfig = element.getAttribute(EncryptDataSourceBeanDefinitionParserTag.DEFAULT_ENCRYPTOR_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(defaultEncryptorConfig)) {
            factory.addPropertyReference("defaultEncryptorConfig", defaultEncryptorConfig);
        }
    }
    
    private List<BeanDefinition> parseTableRulesConfiguration(final Element element) {
        Element tableRulesElement = DomUtils.getChildElementByTagName(element, EncryptDataSourceBeanDefinitionParserTag.TABLE_RULES_TAG);
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(tableRulesElement, EncryptDataSourceBeanDefinitionParserTag.TABLE_RULE_TAG);
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
        parseEncryptorConfiguration(tableElement, factory);
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
    
    private void parseEncryptorConfiguration(final Element tableElement, final BeanDefinitionBuilder factory) {
        String encryptor = tableElement.getAttribute(ShardingDataSourceBeanDefinitionParserTag.ENCRYPTOR_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(encryptor)) {
            factory.addPropertyReference("encryptorConfig", encryptor);
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
