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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Strings;

import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringEncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.EncryptDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Data source parser for spring namespace.
 * 
 * @author panjuan
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(getOrchestrationDataSourceClass(element.getLocalName()));
        configureFactory(element, factory);
        return factory.getBeanDefinition();
    }
    
    private Class<?> getOrchestrationDataSourceClass(final String localName) {
        switch (localName) { 
            case ShardingDataSourceBeanDefinitionParserTag.ROOT_TAG:
                return OrchestrationSpringShardingDataSource.class;
            case EncryptDataSourceBeanDefinitionParserTag.ROOT_TAG:
                return OrchestrationSpringEncryptDataSource.class;
            default:
                return OrchestrationSpringMasterSlaveDataSource.class;
        }
    }
    
    private void configureFactory(final Element element, final BeanDefinitionBuilder factory) {
        String dataSourceName = element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATA_SOURCE_REF_TAG);
        if (!Strings.isNullOrEmpty(dataSourceName)) {
            factory.addConstructorArgReference(dataSourceName);
        }
        factory.addConstructorArgValue(getOrchestrationConfiguration(element));
    }
    
    private BeanDefinition getOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        factory.addConstructorArgReference(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.REG_REF_TAG));
        factory.addConstructorArgValue(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.OVERWRITE_TAG));
        return factory.getBeanDefinition();
    }
}

