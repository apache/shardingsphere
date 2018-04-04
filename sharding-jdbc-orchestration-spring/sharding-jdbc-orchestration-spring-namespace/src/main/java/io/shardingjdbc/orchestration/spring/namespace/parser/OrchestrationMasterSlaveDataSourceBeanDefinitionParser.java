/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationMasterSlaveDataSource;
import io.shardingjdbc.orchestration.spring.datasource.OrchestrationMasterSlaveDataSourceFacoryBean;
import io.shardingjdbc.orchestration.spring.datasource.SpringMasterSlaveDataSource;
import io.shardingjdbc.orchestration.spring.namespace.constants.MasterSlaveDataSourceBeanDefinitionParserTag;
import io.shardingjdbc.orchestration.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Orchestration master-slave data source parser for spring namespace.
 *
 * @author caohao
 * @author zhangliang
 */
public class OrchestrationMasterSlaveDataSourceBeanDefinitionParser extends AbstractOrchestrationBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        String regCenter = parseRegistryCenterRef(element);
        if (Strings.isNullOrEmpty(regCenter)) {
            return getSpringMasterSlaveDataSourceBean(element);
        }
        return getOrchestrationSpringMasterSlaveDataSourceBean(element, parserContext);
    }
    
    private AbstractBeanDefinition getSpringMasterSlaveDataSourceBean(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringMasterSlaveDataSource.class);
        factory.addConstructorArgValue(parseDataSources(element));
        factory.addConstructorArgValue(parseId(element));
        factory.addConstructorArgValue(parseMasterDataSourceRef(element));
        factory.addConstructorArgValue(parseSlaveDataSourcesRef(element));
        String strategyRef = parseStrategyRef(element);
        if (!Strings.isNullOrEmpty(strategyRef)) {
            factory.addConstructorArgReference(strategyRef);
        } else {
            factory.addConstructorArgValue(parseStrategyType(element));
        }
        return factory.getBeanDefinition();
    }
    
    private AbstractBeanDefinition getOrchestrationSpringMasterSlaveDataSourceBean(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationMasterSlaveDataSourceFacoryBean.class);
        String masterDataSourceRef = parseMasterDataSourceRef(element);
        if (!Strings.isNullOrEmpty(masterDataSourceRef)) {
            factory.addConstructorArgValue(parseDataSources(element));
            factory.addConstructorArgValue(parseMasterSlaveRuleConfig(element));
            factory.addConstructorArgValue(parseConfigMap(element, parserContext, factory.getBeanDefinition()));
        }
        factory.addConstructorArgValue(parseOrchestrationConfiguration(element, OrchestrationConfiguration.MASTER_SLAVE));
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final Element element) {
        String masterDataSource = parseMasterDataSourceRef(element);
        Map<String, RuntimeBeanReference> result = new ManagedMap<>();
        result.put(masterDataSource, new RuntimeBeanReference(masterDataSource));
        for (String each : parseSlaveDataSources(element)) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private String parseId(final Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
    
    private BeanDefinition parseMasterSlaveRuleConfig(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveRuleConfiguration.class);
        factory.addConstructorArgValue(parseId(element));
        factory.addConstructorArgValue(parseMasterDataSourceRef(element));
        factory.addConstructorArgValue(parseSlaveDataSources(element));
        String strategyRef = parseStrategyRef(element);
        MasterSlaveLoadBalanceAlgorithmType strategyType = parseStrategyType(element);
        if (!Strings.isNullOrEmpty(strategyRef)) {
            factory.addConstructorArgReference(strategyRef);
        } else if (null != strategyType) {
            factory.addConstructorArgValue(strategyType.getAlgorithm());
        }
        return factory.getBeanDefinition();
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
    
    private List<String> parseSlaveDataSources(final Element element) {
        return Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE));
    }
    
    private String parseStrategyRef(final Element element) {
        return element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.STRATEGY_REF_ATTRIBUTE);
    }
    
    private MasterSlaveLoadBalanceAlgorithmType parseStrategyType(final Element element) {
        String result = element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.STRATEGY_TYPE_ATTRIBUTE);
        return Strings.isNullOrEmpty(result) ? null : MasterSlaveLoadBalanceAlgorithmType.valueOf(result);
    }
    
    private Map parseConfigMap(final Element element, final ParserContext parserContext, final BeanDefinition beanDefinition) {
        Element dataElement = DomUtils.getChildElementByTagName(element, ShardingDataSourceBeanDefinitionParserTag.CONFIG_MAP_TAG);
        return null == dataElement ? Collections.<String, Class<?>>emptyMap() : parserContext.getDelegate().parseMapElement(dataElement, beanDefinition);
    }
}
