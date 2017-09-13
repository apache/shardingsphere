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

package com.dangdang.ddframe.rdb.sharding.spring.namespace.parser;

import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.spring.namespace.constants.ShardingJdbcStrategyBeanDefinitionParserTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Sharding strategy parser for spring namespace.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShardingJdbcStrategyBeanDefinition {
    
    static AbstractBeanDefinition getBeanDefinitionByElement(final Element element) {
        String type = element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.TYPE);
        switch (type) {
            case "standard":
                return getStandardShardingStrategyConfigBeanDefinition(element);
            case "complex":
                return getComplexShardingStrategyConfigBeanDefinition(element);
            case "hint":
                return getHintShardingStrategyConfigBeanDefinition(element);
            case "inline":
                return getInlineShardingStrategyConfigBeanDefinition(element);
            case "none":
                return getNoneShardingStrategyConfigBeanDefinition();
            default:
                throw new ShardingJdbcException("Cannot support type: %s", type);
        }
    }
    
    private static AbstractBeanDefinition getStandardShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(StandardShardingStrategyConfig.class);
        factory.addPropertyValue("shardingColumn", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.SHARDING_COLUMNS_ATTRIBUTE));
        factory.addPropertyValue("preciseAlgorithmClassName", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_ATTRIBUTE));
        // TODO rangeAlgorithmClassName
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getComplexShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ComplexShardingStrategyConfig.class);
        factory.addPropertyValue("shardingColumns", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.SHARDING_COLUMNS_ATTRIBUTE));
        factory.addPropertyValue("algorithmClassName", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getHintShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(HintShardingStrategyConfig.class);
        factory.addPropertyValue("algorithmClassName", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getInlineShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(InlineShardingStrategyConfig.class);
        factory.addPropertyValue("shardingColumn", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.SHARDING_COLUMNS_ATTRIBUTE));
        factory.addPropertyValue("algorithmInlineExpression", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_EXPRESSION_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getNoneShardingStrategyConfigBeanDefinition() {
        return BeanDefinitionBuilder.rootBeanDefinition(NoneShardingStrategyConfig.class).getBeanDefinition();
    }
}
