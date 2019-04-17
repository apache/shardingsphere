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

package org.apache.shardingsphere.shardingjdbc.spring.namespace.handler;

import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.constants.ShardingStrategyBeanDefinitionParserTag;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.parser.KeyGeneratorBeanDefinitionParser;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.parser.ShardingDataSourceBeanDefinitionParser;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.parser.ShardingStrategyBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for sharding.
 * 
 * @author caohao
 * @author zhangliang
 * @author panjuan
 */
public final class ShardingNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionParserTag.STANDARD_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionParserTag.COMPLEX_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionParserTag.INLINE_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionParserTag.HINT_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionParserTag.NONE_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingDataSourceBeanDefinitionParserTag.KEY_GENERATOR_REF_TAG, new KeyGeneratorBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingDataSourceBeanDefinitionParserTag.ROOT_TAG, new ShardingDataSourceBeanDefinitionParser());
    }
}
