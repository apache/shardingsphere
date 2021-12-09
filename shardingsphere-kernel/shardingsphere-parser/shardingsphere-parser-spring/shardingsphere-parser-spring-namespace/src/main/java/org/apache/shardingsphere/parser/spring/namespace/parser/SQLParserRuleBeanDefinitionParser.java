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

package org.apache.shardingsphere.parser.spring.namespace.parser;

import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.spring.namespace.tag.SQLParserRuleBeanDefinitionTag;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class SQLParserRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SQLParserRuleConfiguration.class);
        factory.addPropertyValue("sqlCommentParseEnabled", parseSQLCommentParserEnableConfiguration(element));
        factory.addPropertyValue("sqlStatementCacheOption", parseSQLStatementCacheConfiguration(element));
        factory.addPropertyValue("parseTreeCacheOption", parserTreeCacheConfiguration(element));
        return factory.getBeanDefinition();
    }
    
    private boolean parseSQLCommentParserEnableConfiguration(final Element element) {
        Element sqlCommentParserEnable = DomUtils.getChildElementByTagName(element, SQLParserRuleBeanDefinitionTag.SQL_COMMENT_PARSER_ENABLE);
        return Boolean.parseBoolean(sqlCommentParserEnable.getAttribute(SQLParserRuleBeanDefinitionTag.VALUE));
    }
    
    private BeanDefinition parseSQLStatementCacheConfiguration(final Element element) {
        Element sqlStatementCacheElement = DomUtils.getChildElementByTagName(element, SQLParserRuleBeanDefinitionTag.SQL_STATEMENT_CACHE);
        if (null == sqlStatementCacheElement) {
            return null;
        }
        return parserCacheOption(sqlStatementCacheElement);
    }
    
    private BeanDefinition parserTreeCacheConfiguration(final Element element) {
        Element parserTreeCacheElement = DomUtils.getChildElementByTagName(element, SQLParserRuleBeanDefinitionTag.PARSER_TREE_CACHE);
        if (null == parserTreeCacheElement) {
            return null;
        }
        return parserCacheOption(parserTreeCacheElement);
    }
    
    private BeanDefinition parserCacheOption(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(CacheOption.class);
        Element cacheElement = DomUtils.getChildElementByTagName(element, SQLParserRuleBeanDefinitionTag.CACHE_OPTION);
        if (null == cacheElement) {
            return null;
        }
        factory.addPropertyValue("initialCapacity", cacheElement.getAttribute(SQLParserRuleBeanDefinitionTag.INITIAL_CAPACITY));
        factory.addPropertyValue("maximumSize", cacheElement.getAttribute(SQLParserRuleBeanDefinitionTag.MAXIMUM_SIZE));
        factory.addPropertyValue("concurrencyLevel", cacheElement.getAttribute(SQLParserRuleBeanDefinitionTag.CONCURRENCY_LEVEL));
        return factory.getBeanDefinition();
    }

}
