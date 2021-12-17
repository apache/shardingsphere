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
    
    public static final String ROOT_TAG = "rule";
    
    public static final String SQL_COMMENT_PARSER_ENABLE = "sql-comment-parse-enable";
    
    public static final String SQL_STATEMENT_CACHE_REF = "sql-statement-cache-ref";
    
    public static final String PARSER_TREE_CACHE_REF = "parser-tree-cache-ref";
    
    public static final String INITIAL_CAPACITY = "initial-capacity";
    
    public static final String MAXIMUM_SIZE = "maximum-size";
    
    public static final String CONCURRENCY_LEVEL = "concurrency-level";
    
    public static final String CACHE_OPTION = "cache-option";
}
