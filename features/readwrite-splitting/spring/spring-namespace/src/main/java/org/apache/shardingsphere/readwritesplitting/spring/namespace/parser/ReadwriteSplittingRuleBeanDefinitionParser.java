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

package org.apache.shardingsphere.readwritesplitting.spring.namespace.parser;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.factorybean.ReplicaLoadBalanceAlgorithmFactoryBean;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.tag.ReadwriteSplittingRuleBeanDefinitionTag;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.tag.strategy.DynamicReadwriteSplittingBeanDefinitionTag;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.tag.strategy.StaticReadwriteSplittingBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.registry.ShardingSphereAlgorithmBeanRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Readwrite-splitting rule bean definition parser.
 */
public final class ReadwriteSplittingRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class);
        factory.addConstructorArgValue(parseReadwriteSplittingDataSourceRuleConfigurations(element));
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ReplicaLoadBalanceAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseReadwriteSplittingDataSourceRuleConfigurations(final Element element) {
        List<Element> dataSourceElements = DomUtils.getChildElementsByTagName(element, ReadwriteSplittingRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(dataSourceElements.size());
        for (Element each : dataSourceElements) {
            result.add(parseReadwriteSplittingDataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseReadwriteSplittingDataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReadwriteSplittingDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ReadwriteSplittingRuleBeanDefinitionTag.READWRITE_SPLITTING_DATA_SOURCE_ID_ATTRIBUTE));
        parseReadwriteSplittingStrategyConfiguration(element, factory);
        factory.addConstructorArgValue(element.getAttribute(ReadwriteSplittingRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private void parseReadwriteSplittingStrategyConfiguration(final Element element, final BeanDefinitionBuilder factory) {
        Element strategyElement = DomUtils.getChildElementByTagName(element, StaticReadwriteSplittingBeanDefinitionTag.STATIC_STRATEGY_ROOT_TAG);
        if (null == strategyElement) {
            factory.addConstructorArgValue(null);
            factory.addConstructorArgValue(
                    getDynamicReadwriteSplittingStrategyConfigBeanDefinition(DomUtils.getChildElementByTagName(element, DynamicReadwriteSplittingBeanDefinitionTag.DYNAMIC_STRATEGY_ROOT_TAG)));
        } else {
            factory.addConstructorArgValue(
                    getStaticReadwriteSplittingStrategyConfigBeanDefinition(DomUtils.getChildElementByTagName(element, StaticReadwriteSplittingBeanDefinitionTag.STATIC_STRATEGY_ROOT_TAG)));
            factory.addConstructorArgValue(null);
        }
    }
    
    private AbstractBeanDefinition getStaticReadwriteSplittingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(StaticReadwriteSplittingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(StaticReadwriteSplittingBeanDefinitionTag.WRITE_DATA_SOURCE_NAME));
        factory.addConstructorArgValue(Splitter.on(',').trimResults().splitToList(element.getAttribute(StaticReadwriteSplittingBeanDefinitionTag.READ_DATA_SOURCE_NAMES)));
        return factory.getBeanDefinition();
    }
    
    private AbstractBeanDefinition getDynamicReadwriteSplittingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(DynamicReadwriteSplittingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(DynamicReadwriteSplittingBeanDefinitionTag.AUTO_AWARE_DATA_SOURCE_NAME));
        factory.addConstructorArgValue(element.getAttribute(DynamicReadwriteSplittingBeanDefinitionTag.WRITE_DATA_SOURCE_QUERY_ENABLED));
        return factory.getBeanDefinition();
    }
}
