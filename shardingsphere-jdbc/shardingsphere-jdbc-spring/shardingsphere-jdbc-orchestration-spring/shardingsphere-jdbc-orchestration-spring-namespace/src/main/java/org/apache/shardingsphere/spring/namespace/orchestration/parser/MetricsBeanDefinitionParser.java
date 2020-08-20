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
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.spring.namespace.orchestration.constants.MetricsBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Properties;

/**
 * Metrics bean definition parser.
 */
public final class MetricsBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    private static final String PROPERTY_NAME = "metricsName";
    
    private static final String PROPERTY_HOST = "host";
    
    private static final String PROPERTY_PORT = "port";
    
    private static final String PROPERTY_ASYNC = "async";
    
    private static final String PROPERTY_ENABLE = "enable";
    
    private static final String PROPERTY_THREAD_COUNT = "threadCount";
    
    private static final String PROPERTY_PROPS = "props";
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MetricsConfiguration.class);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.NAME_TAG, PROPERTY_NAME, element, factory);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.HOST_TAG, PROPERTY_HOST, element, factory);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.PORT_TAG, PROPERTY_PORT, element, factory);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.ASYNC_TAG, PROPERTY_ASYNC, element, factory);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.ENABLE_TAG, PROPERTY_ENABLE, element, factory);
        addPropertyValueIfNotEmpty(MetricsBeanDefinitionTag.THREAD_COUNT_TAG, PROPERTY_THREAD_COUNT, element, factory);
        factory.addPropertyValue(PROPERTY_PROPS, parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, MetricsBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
