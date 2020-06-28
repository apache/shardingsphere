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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.spring.namespace.orchestration.OrchestrationSpringShardingSphereDataSource;
import org.apache.shardingsphere.spring.namespace.orchestration.constants.DataSourceBeanDefinitionTag;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

/**
 * Data source parser for spring namespace.
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringShardingSphereDataSource.class);
        configureFactory(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void configureFactory(final Element element, final BeanDefinitionBuilder factory) {
        String dataSourceName = element.getAttribute(DataSourceBeanDefinitionTag.DATA_SOURCE_REF_TAG);
        if (!Strings.isNullOrEmpty(dataSourceName)) {
            factory.addConstructorArgReference(dataSourceName);
        }
        factory.addConstructorArgValue(getOrchestrationConfiguration(element));
        String cluster = element.getAttribute(DataSourceBeanDefinitionTag.CLUSTER_REF_TAG);
        if (!Strings.isNullOrEmpty(cluster)) {
            factory.addConstructorArgReference(cluster);
        }
    }
    
    private BeanDefinition getOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(parseInstances(element));
        return factory.getBeanDefinition();
    }
    
    private Map<String, RuntimeBeanReference> parseInstances(final Element element) {
        List<String> instances = Splitter.on(",").trimResults().splitToList(element.getAttribute(DataSourceBeanDefinitionTag.INSTANCE_REF_TAG));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(instances.size());
        for (String each : instances) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
}

