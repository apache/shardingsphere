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
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.spring.namespace.tag.MasterSlaveRuleBeanDefinitionTag;
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
import java.util.stream.Collectors;

/**
 * Master-slave rule bean definition parser.
 */
public final class MasterSlaveRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedMasterSlaveRuleConfiguration.class);
        factory.addConstructorArgValue(parseMasterSlaveDataSourceRuleConfigurations(element));
        factory.addConstructorArgValue(parseLoadBalanceAlgorithmsConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseMasterSlaveDataSourceRuleConfigurations(final Element element) {
        List<Element> masterSlaveDataSourceElements = DomUtils.getChildElementsByTagName(element, MasterSlaveRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(masterSlaveDataSourceElements.size());
        for (Element each : masterSlaveDataSourceElements) {
            result.add(parseMasterSlaveDataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseMasterSlaveDataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.MASTER_SLAVE_DATA_SOURCE_ID_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.MASTER_DATA_SOURCE_NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseSlaveDataSourcesRef(element));
        factory.addConstructorArgValue(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private Collection<String> parseSlaveDataSourcesRef(final Element element) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveRuleBeanDefinitionTag.SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE));
        Collection<String> result = new ManagedList<>(slaveDataSources.size());
        result.addAll(slaveDataSources);
        return result;
    }
    
    private Map<String, RuntimeBeanReference> parseLoadBalanceAlgorithmsConfigurations(final Element element) {
        Collection<String> loadBalanceAlgorithmRefs = findLoadBalanceAlgorithmRefs(DomUtils.getChildElementsByTagName(element, MasterSlaveRuleBeanDefinitionTag.DATA_SOURCE_TAG));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(loadBalanceAlgorithmRefs.size());
        for (String each : loadBalanceAlgorithmRefs) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private Collection<String> findLoadBalanceAlgorithmRefs(final List<Element> masterSlaveDataSourceElements) {
        return masterSlaveDataSourceElements.stream().filter(each -> !Strings.isNullOrEmpty(each.getAttribute(MasterSlaveRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE)))
                .map(each -> each.getAttribute(MasterSlaveRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE)).collect(Collectors.toSet());
    }
}
