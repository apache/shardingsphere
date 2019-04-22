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
import org.apache.shardingsphere.shardingjdbc.spring.datasource.SpringMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.MasterSlaveDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
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
 * 
 * @author zhangliang
 */
public final class MasterSlaveDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringMasterSlaveDataSource.class);
        factory.addConstructorArgValue(parseDataSources(element));
        factory.addConstructorArgValue(parseId(element));
        factory.addConstructorArgValue(parseMasterDataSourceRef(element));
        factory.addConstructorArgValue(parseSlaveDataSourcesRef(element));
        String loadBalanceAlgorithmRefAttributeRef = parseLoadBalanceAlgorithmRefAttributeRef(element);
        if (!Strings.isNullOrEmpty(loadBalanceAlgorithmRefAttributeRef)) {
            factory.addConstructorArgReference(loadBalanceAlgorithmRefAttributeRef);
        } else {
            factory.addConstructorArgValue(null);
        }
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final Element element) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(slaveDataSources.size());
        for (String each : slaveDataSources) {
            result.put(each, new RuntimeBeanReference(each));
        }
        String masterDataSourceName = parseMasterDataSourceRef(element);
        result.put(masterDataSourceName, new RuntimeBeanReference(masterDataSourceName));
        return result;
    }
    
    private String parseId(final Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
    
    private String parseMasterDataSourceRef(final Element element) {
        return element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.MASTER_DATA_SOURCE_NAME_ATTRIBUTE);
    }
    
    private Collection<String> parseSlaveDataSourcesRef(final Element element) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE));
        Collection<String> result = new ManagedList<>(slaveDataSources.size());
        result.addAll(slaveDataSources);
        return result;
    }
    
    private String parseLoadBalanceAlgorithmRefAttributeRef(final Element element) {
        return element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE);
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
