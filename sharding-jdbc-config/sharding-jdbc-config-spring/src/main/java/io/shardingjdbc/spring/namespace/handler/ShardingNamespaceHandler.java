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

package io.shardingjdbc.spring.namespace.handler;

import io.shardingjdbc.spring.namespace.constants.ShardingJdbcDataSourceBeanDefinitionParserTag;
import io.shardingjdbc.spring.namespace.constants.ShardingJdbcStrategyBeanDefinitionParserTag;
import io.shardingjdbc.spring.namespace.parser.ShardingJdbcDataSourceBeanDefinitionParser;
import io.shardingjdbc.spring.namespace.parser.ShardingJdbcStrategyBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for sharding.
 * 
 * @author caohao
 * @author zhangliang
 */
public final class ShardingNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(ShardingJdbcStrategyBeanDefinitionParserTag.STANDARD_STRATEGY_ROOT_TAG, new ShardingJdbcStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingJdbcStrategyBeanDefinitionParserTag.COMPLEX_STRATEGY_ROOT_TAG, new ShardingJdbcStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingJdbcStrategyBeanDefinitionParserTag.INLINE_STRATEGY_ROOT_TAG, new ShardingJdbcStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingJdbcStrategyBeanDefinitionParserTag.HINT_STRATEGY_ROOT_TAG, new ShardingJdbcStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingJdbcStrategyBeanDefinitionParserTag.NONE_STRATEGY_ROOT_TAG, new ShardingJdbcStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingJdbcDataSourceBeanDefinitionParserTag.ROOT_TAG, new ShardingJdbcDataSourceBeanDefinitionParser());
    }
}
