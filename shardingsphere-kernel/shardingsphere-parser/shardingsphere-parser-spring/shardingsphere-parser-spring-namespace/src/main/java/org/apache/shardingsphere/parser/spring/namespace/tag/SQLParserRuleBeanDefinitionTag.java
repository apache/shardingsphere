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

package org.apache.shardingsphere.parser.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SQL parser rule bean definition tag constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserRuleBeanDefinitionTag {
    
    /**
     * Root tag.
     */
    public static final String ROOT_TAG = "rule";
    
    /**
     * SQL comment parse enable attribute.
     */
    public static final String SQL_COMMENT_PARSER_ENABLE_ATTRIBUTE = "sql-comment-parse-enable";
    
    /**
     * SQL statement cache ref attribute.
     */
    public static final String SQL_STATEMENT_CACHE_REF_ATTRIBUTE = "sql-statement-cache-ref";
    
    /**
     * Parse tree cache ref attribute.
     */
    public static final String PARSE_TREE_CACHE_REF_ATTRIBUTE = "parse-tree-cache-ref";
    
    /**
     * Initial capacity attribute.
     */
    public static final String INITIAL_CAPACITY_ATTRIBUTE = "initial-capacity";
    
    /**
     * Maximum size attribute.
     */
    public static final String MAXIMUM_SIZE_ATTRIBUTE = "maximum-size";
    
    /**
     * Cache option attribute.
     */
    public static final String CACHE_OPTION_ATTRIBUTE = "cache-option";
}
