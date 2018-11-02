/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.orchestration.spring.namespace.handler;

import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.MasterSlaveDataSourceBeanDefinitionParserTag;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.RegistryCenterBeanDefinitionParserTag;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.parser.DataSourceBeanDefinitionParser;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.parser.RegBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Orchestration spring namespace handler for sharding.
 * 
 * @author panjuan
 */
public final class OrchestrationNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(RegistryCenterBeanDefinitionParserTag.ROOT_TAG, new RegBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingDataSourceBeanDefinitionParserTag.ROOT_TAG, new DataSourceBeanDefinitionParser());
        registerBeanDefinitionParser(MasterSlaveDataSourceBeanDefinitionParserTag.ROOT_TAG, new DataSourceBeanDefinitionParser());
    }
}
