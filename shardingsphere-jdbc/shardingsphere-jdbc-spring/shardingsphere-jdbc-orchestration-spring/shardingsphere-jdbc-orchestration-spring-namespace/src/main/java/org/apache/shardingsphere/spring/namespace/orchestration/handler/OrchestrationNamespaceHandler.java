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

package org.apache.shardingsphere.spring.namespace.orchestration.handler;

import org.apache.shardingsphere.spring.namespace.orchestration.constants.DataSourceBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.orchestration.constants.OrchestrationCenterConfigurationBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.orchestration.parser.DataSourceBeanDefinitionParser;
import org.apache.shardingsphere.spring.namespace.orchestration.parser.OrchestrationCenterConfigurationBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Orchestration spring namespace handler for sharding.
 */
public final class OrchestrationNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(OrchestrationCenterConfigurationBeanDefinitionTag.REG_CENTER_ROOT_TAG, new OrchestrationCenterConfigurationBeanDefinitionParser());
        registerBeanDefinitionParser(OrchestrationCenterConfigurationBeanDefinitionTag.CONFIG_CENTER_ROOT_TAG, new OrchestrationCenterConfigurationBeanDefinitionParser());
        registerBeanDefinitionParser(DataSourceBeanDefinitionTag.ROOT_TAG, new DataSourceBeanDefinitionParser());
    }
}
