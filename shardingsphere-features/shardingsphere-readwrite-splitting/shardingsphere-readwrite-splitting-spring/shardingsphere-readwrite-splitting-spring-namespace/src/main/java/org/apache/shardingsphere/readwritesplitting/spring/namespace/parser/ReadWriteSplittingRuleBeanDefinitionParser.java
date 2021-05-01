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
import org.apache.shardingsphere.readwritesplitting.common.algorithm.config.AlgorithmProvidedReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.factorybean.ReplicaLoadBalanceAlgorithmFactoryBean;
import org.apache.shardingsphere.readwritesplitting.spring.namespace.tag.ReadWriteSplittingRuleBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.registry.ShardingSphereAlgorithmBeanRegistry;
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
 * Read write splitting rule bean definition parser.
 */
public final class ReadWriteSplittingRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedReadWriteSplittingRuleConfiguration.class);
        factory.addConstructorArgValue(parseReadWriteSplittingDataSourceRuleConfigurations(element));
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ReplicaLoadBalanceAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseReadWriteSplittingDataSourceRuleConfigurations(final Element element) {
        List<Element> dataSourceElements = DomUtils.getChildElementsByTagName(element, ReadWriteSplittingRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(dataSourceElements.size());
        for (Element each : dataSourceElements) {
            result.add(parseReadWriteSplittingDataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseReadWriteSplittingDataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReadWriteSplittingDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ReadWriteSplittingRuleBeanDefinitionTag.READ_WRITE_SPLITTING_DATA_SOURCE_ID_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ReadWriteSplittingRuleBeanDefinitionTag.AUTO_AWARE_DATA_SOURCE_NAME));
        factory.addConstructorArgValue(element.getAttribute(ReadWriteSplittingRuleBeanDefinitionTag.WRITE_DATA_SOURCE_NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseReplicaDataSourcesRef(element));
        factory.addConstructorArgValue(element.getAttribute(ReadWriteSplittingRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private Collection<String> parseReplicaDataSourcesRef(final Element element) {
        List<String> replicaDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(ReadWriteSplittingRuleBeanDefinitionTag.READ_DATA_SOURCE_NAMES_ATTRIBUTE));
        Collection<String> result = new ManagedList<>(replicaDataSources.size());
        result.addAll(replicaDataSources);
        return result;
    }
}
