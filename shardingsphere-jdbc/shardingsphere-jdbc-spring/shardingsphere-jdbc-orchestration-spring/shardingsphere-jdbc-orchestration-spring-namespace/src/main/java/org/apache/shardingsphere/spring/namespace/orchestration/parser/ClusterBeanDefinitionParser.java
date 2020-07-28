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
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.spring.namespace.orchestration.constants.ClusterBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Cluster bean definition parser.
 */
public final class ClusterBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    private static final String PROPERTY_SQL = "sql";
    
    private static final String PROPERTY_THREAD_COUNT = "threadCount";
    
    private static final String PROPERTY_INTERVAL = "interval";
    
    private static final String PROPERTY_RETRY_ENABLE = "retryEnable";
    
    private static final String PROPERTY_RETRY_MAXIMUM = "retryMaximum";
    
    private static final String PROPERTY_RETRY_INTERVAL = "retryInterval";
    
    private static final String PROPERTY_HEARTBEAT = "heartbeat";
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ClusterConfiguration.class);
        factory.addPropertyValue(PROPERTY_HEARTBEAT, getHeartbeatConfiguration(element));
        return factory.getBeanDefinition();
    }
    
    private BeanDefinition getHeartbeatConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(HeartbeatConfiguration.class);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.SQL_ATTRIBUTE, PROPERTY_SQL, element, factory);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.THREAD_COUNT_ATTRIBUTE, PROPERTY_THREAD_COUNT, element, factory);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.INTERVAL_ATTRIBUTE, PROPERTY_INTERVAL, element, factory);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.RETRY_ENABLE_ATTRIBUTE, PROPERTY_RETRY_ENABLE, element, factory);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.RETRY_MAXIMUM_ATTRIBUTE, PROPERTY_RETRY_MAXIMUM, element, factory);
        addPropertyValueIfNotEmpty(ClusterBeanDefinitionTag.RETRY_INTERVAL_ATTRIBUTE, PROPERTY_RETRY_INTERVAL, element, factory);
        return factory.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}
