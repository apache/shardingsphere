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

package io.shardingjdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import io.shardingjdbc.orchestration.spring.datasource.SpringMasterSlaveDataSource;
import io.shardingjdbc.orchestration.spring.namespace.constants.MasterSlaveDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * Orchestration master-slave data source parser for spring namespace.
 *
 * @author caohao
 */
public class OrchestrationMasterSlaveDataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    //CHECKSTYLE:OFF
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
    //CHECKSTYLE:ON
        String regCenter = parseRegistryCenterRef(element);
        if (Strings.isNullOrEmpty(regCenter)) {
            return getSpringMasterSlaveDataSourceBean(element, parserContext);
        }
        return getOrchestrationSpringMasterSlaveDataSourceBean(element, parserContext, regCenter);
    }
    
    private AbstractBeanDefinition getSpringMasterSlaveDataSourceBean(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SpringMasterSlaveDataSource.class);
        factory.addConstructorArgValue(parseId(element));
        String masterDataSourceName = parseMasterDataSourceRef(element);
        factory.addConstructorArgValue(masterDataSourceName);
        factory.addConstructorArgReference(masterDataSourceName);
        factory.addConstructorArgValue(parseSlaveDataSourceBeans(element, parserContext));
        String strategyRef = parseStrategyRef(element);
        if (!Strings.isNullOrEmpty(strategyRef)) {
            factory.addConstructorArgReference(strategyRef);
        } else {
            factory.addConstructorArgValue(parseStrategyType(element));
        }
        return factory.getBeanDefinition();
    }
    
    private AbstractBeanDefinition getOrchestrationSpringMasterSlaveDataSourceBean(final Element element, final ParserContext parserContext, final String regCenter) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringMasterSlaveDataSource.class);
        factory.addConstructorArgValue(parseId(element));
        factory.addConstructorArgValue(parseOverwrite(element));
        factory.addConstructorArgReference(regCenter);
        factory.addConstructorArgValue(parseDataSources(element, parserContext));
        factory.addConstructorArgValue(parseMasterSlaveRuleConfig(element, parserContext));
        factory.setInitMethodName("init");
        return factory.getBeanDefinition();
    }
    
    private String parseId(final Element element) {
        return element.getAttribute(ID_ATTRIBUTE);
    }
    
    private boolean parseOverwrite(final Element element) {
        return Boolean.parseBoolean(element.getAttribute("overwrite"));
    }
    
    private String parseRegistryCenterRef(final Element element) {
        return element.getAttribute("registry-center-ref");
    }
    
    private Map<String, BeanDefinition> parseDataSources(final Element element, final ParserContext parserContext) {
        String masterDataSource = parseMasterDataSourceRef(element);
        Map<String, BeanDefinition> result = new ManagedMap<>();
        result.put(masterDataSource, parserContext.getRegistry().getBeanDefinition(masterDataSource));
        for (String each : parseSlaveDataSources(element)) {
            result.put(each, parserContext.getRegistry().getBeanDefinition(each));
        }
        return result;
    }
    
    private BeanDefinition parseMasterSlaveRuleConfig(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveRuleConfiguration.class);
        factory.addPropertyValue("name", parseId(element));
        factory.addPropertyValue("masterDataSourceName", parseMasterDataSourceRef(element));
        factory.addPropertyValue("slaveDataSourceNames", parseSlaveDataSources(element));
        String strategyRef = parseStrategyRef(element);
        if (!Strings.isNullOrEmpty(strategyRef)) {
            factory.addPropertyValue("loadBalanceAlgorithmClassName", parserContext.getRegistry().getBeanDefinition(strategyRef).getBeanClassName());
        } else {
            factory.addPropertyValue("loadBalanceAlgorithmType", parseStrategyType(element));
        }
        return factory.getBeanDefinition();
    }
    
    private String parseMasterDataSourceRef(final Element element) {
        return element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.MASTER_DATA_SOURCE_NAME_ATTRIBUTE);
    }
    
    private Map<String, BeanDefinition> parseSlaveDataSourceBeans(final Element element, final ParserContext parserContext) {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(MasterSlaveDataSourceBeanDefinitionParserTag.SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE));
        Map<String, BeanDefinition> result = new ManagedMap<>(slaveDataSources.size());
        for (String each : slaveDataSources) {
            result.put(each, parserContext.getRegistry().getBeanDefinition(each));
        }
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
        return Strings.isNullOrEmpty(result) ? MasterSlaveLoadBalanceAlgorithmType.getDefaultAlgorithmType() : MasterSlaveLoadBalanceAlgorithmType.valueOf(result);
    }
}
