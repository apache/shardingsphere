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

package org.apache.shardingsphere.spring.namespace.orchestration.parser;

import com.google.common.base.Strings;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.spring.namespace.orchestration.constants.DataSourceBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Data source parser for spring namespace.
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationShardingSphereDataSource.class);
        configureFactory(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void configureFactory(final Element element, final BeanDefinitionBuilder factory) {
        String dataSourceName = element.getAttribute(DataSourceBeanDefinitionTag.DATA_SOURCE_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(dataSourceName)) {
            factory.addConstructorArgReference(dataSourceName);
        }
        factory.addConstructorArgValue(getOrchestrationConfiguration(element));
        String cluster = element.getAttribute(DataSourceBeanDefinitionTag.CLUSTER_REF_ATTRIBUTE);
        String metrics = element.getAttribute(DataSourceBeanDefinitionTag.METRICS_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(cluster)) {
            factory.addConstructorArgReference(cluster);
        }
        if (!Strings.isNullOrEmpty(metrics)) {
            factory.addConstructorArgReference(metrics);
        }
    }
    
    private BeanDefinition getOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(DataSourceBeanDefinitionTag.ID_ATTRIBUTE));
        factory.addConstructorArgReference(element.getAttribute(DataSourceBeanDefinitionTag.REG_CENTER_REF_ATTRIBUTE));
        if (!Strings.isNullOrEmpty(element.getAttribute(DataSourceBeanDefinitionTag.CONFIG_CENTER_REF_ATTRIBUTE))) {
            factory.addConstructorArgReference(element.getAttribute(DataSourceBeanDefinitionTag.CONFIG_CENTER_REF_ATTRIBUTE));
        }
        factory.addConstructorArgValue(element.getAttribute(DataSourceBeanDefinitionTag.OVERWRITE_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
}
