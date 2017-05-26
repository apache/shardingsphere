/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.spring.namespace.parser;

import com.dangdang.ddframe.rdb.sharding.config.common.api.config.GenerateKeyColumnConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.spring.datasource.SpringShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.spring.namespace.constants.ShardingJdbcDataSourceBeanDefinitionParserTag;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 基于Spring命名空间的数据源解析器.
 * 
 * @author caohao
 */
public class ShardingJdbcDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    //CHECKSTYLE:OFF
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
    //CHECKSTYLE:ON
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringShardingDataSource.class);
        factory.addConstructorArgValue(parseShardingRuleConfig(element, parserContext));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private BeanDefinition parseShardingRuleConfig(final Element element, final ParserContext parserContext) {
        Element shardingRuleElement = DomUtils.getChildElementByTagName(element, ShardingJdbcDataSourceBeanDefinitionParserTag.SHARDING_RULE_CONFIG_TAG);
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingRuleConfig.class);
        factory.addPropertyValue("dataSource", parseDataSources(shardingRuleElement, parserContext));
        parseDefaultDataSource(factory, shardingRuleElement);
        factory.addPropertyValue("tables", parseTableRulesConfig(shardingRuleElement));
        factory.addPropertyValue("bindingTables", parseBindingTablesConfig(shardingRuleElement));
        factory.addPropertyValue("defaultDatabaseStrategy", parseDefaultDatabaseStrategyConfig(shardingRuleElement));
        factory.addPropertyValue("defaultTableStrategy", parseDefaultTableStrategyConfig(shardingRuleElement));
        parseKeyGenerator(factory, shardingRuleElement);
        return factory.getBeanDefinition();
    }
    
    private void parseKeyGenerator(final BeanDefinitionBuilder factory, final Element element) {
        String keyGeneratorClass = element.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.KEY_GENERATOR_CLASS);
        if (!Strings.isNullOrEmpty(keyGeneratorClass)) {
            factory.addPropertyValue("keyGeneratorClass", keyGeneratorClass);
        }
    }
    
    private Map<String, BeanDefinition> parseDataSources(final Element element, final ParserContext parserContext) {
        List<String> dataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.DATA_SOURCES_TAG));
        Map<String, BeanDefinition> result = new ManagedMap<>(dataSources.size());
        for (String each : dataSources) {
            result.put(each, parserContext.getRegistry().getBeanDefinition(each));
        }
        return result;
    }
    
    private void parseDefaultDataSource(final BeanDefinitionBuilder factory, final Element element) {
        String defaultDataSource = element.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.DEFAULT_DATA_SOURCE_TAG);
        if (!Strings.isNullOrEmpty(defaultDataSource)) {
            factory.addPropertyValue("defaultDataSourceName", defaultDataSource);
        }
    }
    
    private Map<String, BeanDefinition> parseTableRulesConfig(final Element element) {
        Element tableRulesElement = DomUtils.getChildElementByTagName(element, ShardingJdbcDataSourceBeanDefinitionParserTag.TABLE_RULES_TAG);
        List<Element> tableRuleElements = DomUtils.getChildElementsByTagName(tableRulesElement, ShardingJdbcDataSourceBeanDefinitionParserTag.TABLE_RULE_TAG);
        Map<String, BeanDefinition> result = new ManagedMap<>(tableRuleElements.size());
        for (Element each : tableRuleElements) {
            result.put(each.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.LOGIC_TABLE_ATTRIBUTE), parseTableRuleConfig(each));
        }
        return result;
    }
    
    private BeanDefinition parseTableRuleConfig(final Element tableElement) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(TableRuleConfig.class);
        String dynamic = tableElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.DYNAMIC_TABLE_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(dynamic)) {
            factory.addPropertyValue("dynamic", dynamic);
        }
        String actualTables = tableElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.ACTUAL_TABLES_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(actualTables)) {
            factory.addPropertyValue("actualTables", actualTables);
        }
        String dataSourceNames = tableElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.DATA_SOURCE_NAMES_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(dataSourceNames)) {
            factory.addPropertyValue("dataSourceNames", dataSourceNames);
        }
        String databaseStrategy = tableElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.DATABASE_STRATEGY_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(databaseStrategy)) {
            factory.addPropertyReference("databaseStrategy", databaseStrategy);    
        }
        String tableStrategy = tableElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.TABLE_STRATEGY_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(tableStrategy)) {
            factory.addPropertyReference("tableStrategy", tableStrategy);
        }
        List<Element> generateKeyColumns = DomUtils.getChildElementsByTagName(tableElement, ShardingJdbcDataSourceBeanDefinitionParserTag.GENERATE_KEY_COLUMN);
        if (null == generateKeyColumns || generateKeyColumns.isEmpty()) {
            return factory.getBeanDefinition();
        }
        factory.addPropertyValue("generateKeyColumns", Lists.transform(generateKeyColumns, new Function<Element, GenerateKeyColumnConfig>() {
            @Override
            public GenerateKeyColumnConfig apply(final Element input) {
                GenerateKeyColumnConfig result = new GenerateKeyColumnConfig();
                result.setColumnName(input.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.COLUMN_NAME));
                result.setColumnKeyGeneratorClass(input.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.COLUMN_KEY_GENERATOR_CLASS));
                return result;
            }
        }));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseBindingTablesConfig(final Element element) {
        Element bindingTableRulesElement = DomUtils.getChildElementByTagName(element, ShardingJdbcDataSourceBeanDefinitionParserTag.BINDING_TABLE_RULES_TAG);
        if (null == bindingTableRulesElement) {
            return Collections.emptyList();
        }
        List<Element> bindingTableRuleElements = DomUtils.getChildElementsByTagName(bindingTableRulesElement, ShardingJdbcDataSourceBeanDefinitionParserTag.BINDING_TABLE_RULE_TAG);
        BeanDefinitionBuilder bindingTableRuleFactory = BeanDefinitionBuilder.rootBeanDefinition(BindingTableRuleConfig.class);
        List<BeanDefinition> result = new ManagedList<>(bindingTableRuleElements.size());
        for (Element bindingTableRuleElement : bindingTableRuleElements) {
            bindingTableRuleFactory.addPropertyValue("tableNames", bindingTableRuleElement.getAttribute(ShardingJdbcDataSourceBeanDefinitionParserTag.LOGIC_TABLES_ATTRIBUTE));
            result.add(bindingTableRuleFactory.getBeanDefinition());
        }
        return result;
    }
    
    private BeanDefinition parseDefaultDatabaseStrategyConfig(final Element element) {
        return parseDefaultStrategyConfig(element, ShardingJdbcDataSourceBeanDefinitionParserTag.DEFAULT_DATABASE_STRATEGY_ATTRIBUTE);
    }
    
    private BeanDefinition parseDefaultTableStrategyConfig(final Element element) {
        return parseDefaultStrategyConfig(element, ShardingJdbcDataSourceBeanDefinitionParserTag.DEFAULT_TABLE_STRATEGY_ATTRIBUTE);
    }
    
    private BeanDefinition parseDefaultStrategyConfig(final Element element, final String attr) {
        Element strategyElement = DomUtils.getChildElementByTagName(element, attr);
        return null == strategyElement ? null : ShardingJdbcStrategyBeanDefinition.getBeanDefinitionByElement(strategyElement);
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingJdbcDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
