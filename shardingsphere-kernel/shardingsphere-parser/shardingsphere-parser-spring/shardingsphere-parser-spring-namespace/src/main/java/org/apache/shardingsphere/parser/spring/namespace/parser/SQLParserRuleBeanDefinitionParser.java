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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * SQL parser bean parser for spring namespace.
 */
public final class SQLParserRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SQLParserRuleConfiguration.class);
        factory.addConstructorArgValue(Boolean.parseBoolean(element.getAttribute(SQLParserRuleBeanDefinitionTag.SQL_COMMENT_PARSER_ENABLE)));
        factory.addConstructorArgReference(element.getAttribute(SQLParserRuleBeanDefinitionTag.PARSE_TREE_CACHE_REF));
        factory.addConstructorArgReference(element.getAttribute(SQLParserRuleBeanDefinitionTag.SQL_STATEMENT_CACHE_REF));
        return factory.getBeanDefinition();
    }
}
