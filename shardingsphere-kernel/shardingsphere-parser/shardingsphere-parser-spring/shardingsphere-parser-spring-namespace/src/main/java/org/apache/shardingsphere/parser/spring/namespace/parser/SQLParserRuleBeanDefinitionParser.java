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

import com.google.common.base.Strings;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.spring.namespace.tag.SQLParserRuleBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.Optional;

/**
 * SQL parser bean parser for spring namespace.
 */
public class SQLParserRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SQLParserRuleConfiguration.class);
        factory.addPropertyValue("sqlCommentParseEnabled", parseSQLCommentParserEnableConfiguration(element));
        setSQLStatementCacheOption(element, factory);
        setParseTreeCacheOption(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void setSQLStatementCacheOption(final Element element, final BeanDefinitionBuilder factory) {
        Optional<String> cacheRef = parseCacheRef(element, SQLParserRuleBeanDefinitionTag.SQL_STATEMENT_CACHE_REF);
        cacheRef.ifPresent(optional -> factory.addPropertyReference("sqlStatementCache", optional));
    }
    
    private void setParseTreeCacheOption(final Element element, final BeanDefinitionBuilder factory) {
        Optional<String> cacheRef = parseCacheRef(element, SQLParserRuleBeanDefinitionTag.PARSER_TREE_CACHE_REF);
        cacheRef.ifPresent(optional -> factory.addPropertyReference("parseTreeCache", optional));
    }
    
    private boolean parseSQLCommentParserEnableConfiguration(final Element element) {
        String sqlCommentParserEnable = element.getAttribute(SQLParserRuleBeanDefinitionTag.SQL_COMMENT_PARSER_ENABLE);
        return Boolean.parseBoolean(sqlCommentParserEnable);
    }
    
    private Optional<String> parseCacheRef(final Element element, final String tagName) {
        String result = element.getAttribute(tagName);
        return Strings.isNullOrEmpty(result) ? Optional.empty() : Optional.of(result);
    }
}
