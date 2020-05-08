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
import org.apache.shardingsphere.shardingjdbc.spring.datasource.SpringShardingDataSource;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Master-slave data source parser for spring namespace.
 */
public final class MasterSlaveDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringShardingDataSource.class);
        MasterSlaveRuleConfigurationBeanDefinition configurationBeanDefinition = new MasterSlaveRuleConfigurationBeanDefinition(element);
        factory.addConstructorArgValue(parseDataSources(configurationBeanDefinition));
        factory.addConstructorArgValue(parseRuleConfigurations(configurationBeanDefinition));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final MasterSlaveRuleConfigurationBeanDefinition configurationBeanDefinition) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(configurationBeanDefinition.getSlaveDataSourceNames());
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(slaveDataSources.size());
        for (String each : slaveDataSources) {
            result.put(each, new RuntimeBeanReference(each));
        }
        String masterDataSourceName = configurationBeanDefinition.getMasterDataSourceName();
        result.put(masterDataSourceName, new RuntimeBeanReference(masterDataSourceName));
        return result;
    }
    
    private Collection<BeanDefinition> parseRuleConfigurations(final MasterSlaveRuleConfigurationBeanDefinition configurationBeanDefinition) {
        Collection<BeanDefinition> result = new ManagedList<>(1);
        result.add(configurationBeanDefinition.getBeanDefinition());
        return result;
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
