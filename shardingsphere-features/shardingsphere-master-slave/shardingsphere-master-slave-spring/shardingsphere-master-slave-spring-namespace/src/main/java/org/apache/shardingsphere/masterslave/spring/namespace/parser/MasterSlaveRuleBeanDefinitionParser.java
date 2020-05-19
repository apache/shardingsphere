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

package org.apache.shardingsphere.masterslave.spring.namespace.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.spring.namespace.tag.MasterSlaveRuleBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

/**
 * Master-slave rule bean definition parser.
 */
public final class MasterSlaveRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveRuleConfiguration.class);
        List<Element> masterSlaveDataSourceElements = DomUtils.getChildElementsByTagName(element, MasterSlaveRuleBeanDefinitionTag.MASTER_SLAVE_DATA_SOURCE_TAG);
        List<BeanDefinition> masterSlaveDataSources = new ManagedList<>(masterSlaveDataSourceElements.size());
        for (Element each : masterSlaveDataSourceElements) {
            masterSlaveDataSources.add(parseMasterSlaveDataSourceConfiguration(each));
        }
        factory.addConstructorArgValue(masterSlaveDataSources);
        return factory.getBeanDefinition();
    }
    
    private BeanDefinition parseMasterSlaveDataSourceConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveDataSourceConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.MASTER_SLAVE_DATA_SOURCE_ID_TAG));
        factory.addConstructorArgValue(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.MASTER_DATA_SOURCE_NAME_TAG));
        factory.addConstructorArgValue(parseSlaveDataSourcesRef(element));
        parseMasterSlaveRuleLoadBalanceConfiguration(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void parseMasterSlaveRuleLoadBalanceConfiguration(final Element element, final BeanDefinitionBuilder factory) {
        String loadBalanceStrategyConfiguration = element.getAttribute(MasterSlaveRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_TAG);
        if (!Strings.isNullOrEmpty(loadBalanceStrategyConfiguration)) {
            factory.addConstructorArgReference(loadBalanceStrategyConfiguration);
        }
    }
    
    private Collection<String> parseSlaveDataSourcesRef(final Element element) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.SLAVE_DATA_SOURCE_NAMES_TAG));
        Collection<String> result = new ManagedList<>(slaveDataSources.size());
        result.addAll(slaveDataSources);
        return result;
    }
}
