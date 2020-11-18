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

package org.apache.shardingsphere.replicaquery.spring.namespace.parser;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.replicaquery.algorithm.config.AlgorithmProvidedReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.spring.namespace.factorybean.ReplicaLoadBalanceAlgorithmFactoryBean;
import org.apache.shardingsphere.replicaquery.spring.namespace.tag.ReplicaQueryRuleBeanDefinitionTag;
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
 * Replica query rule bean definition parser.
 */
public final class ReplicaQueryRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(AlgorithmProvidedReplicaQueryRuleConfiguration.class);
        factory.addConstructorArgValue(parseReplicaQueryDataSourceRuleConfigurations(element));
        factory.addConstructorArgValue(ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ReplicaLoadBalanceAlgorithmFactoryBean.class));
        return factory.getBeanDefinition();
    }
    
    private List<BeanDefinition> parseReplicaQueryDataSourceRuleConfigurations(final Element element) {
        List<Element> dataSourceElements = DomUtils.getChildElementsByTagName(element, ReplicaQueryRuleBeanDefinitionTag.DATA_SOURCE_TAG);
        List<BeanDefinition> result = new ManagedList<>(dataSourceElements.size());
        for (Element each : dataSourceElements) {
            result.add(parseReplicaQueryDataSourceRuleConfiguration(each));
        }
        return result;
    }
    
    private BeanDefinition parseReplicaQueryDataSourceRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReplicaQueryDataSourceRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ReplicaQueryRuleBeanDefinitionTag.REPLICA_QUERY_DATA_SOURCE_ID_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ReplicaQueryRuleBeanDefinitionTag.PRIMARY_DATA_SOURCE_NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseReplicaDataSourcesRef(element));
        factory.addConstructorArgValue(element.getAttribute(ReplicaQueryRuleBeanDefinitionTag.LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private Collection<String> parseReplicaDataSourcesRef(final Element element) {
        List<String> replicaDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(ReplicaQueryRuleBeanDefinitionTag.REPLICA_DATA_SOURCE_NAMES_ATTRIBUTE));
        Collection<String> result = new ManagedList<>(replicaDataSources.size());
        result.addAll(replicaDataSources);
        return result;
    }
}
