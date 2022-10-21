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

grammar RALStatement;

import Keyword, Literals;

showSQLParserRule
    : SHOW SQL_PARSER RULE
    ;

alterSQLParserRule
    : ALTER SQL_PARSER RULE sqlParserRuleDefinition
    ;

sqlParserRuleDefinition
    : SQL_COMMENT_PARSE_ENABLE EQ sqlCommentParseEnable (COMMA PARSE_TREE_CACHE LP parseTreeCache RP)? (COMMA SQL_STATEMENT_CACHE LP sqlStatementCache RP)?
    ;

sqlCommentParseEnable
    : TRUE | FALSE
    ;

parseTreeCache
    : cacheOption
    ;

sqlStatementCache
    : cacheOption
    ;

cacheOption
    : (INITIAL_CAPACITY EQ initialCapacity)? (COMMA? MAXIMUM_SIZE EQ maximumSize)? (COMMA? CONCURRENCY_LEVEL EQ concurrencyLevel)? (COMMA? PERSISTENT EQ persistent)?
    ;

initialCapacity
    : INT
    ;

maximumSize
    : INT
    ;

persistent
    : TRUE | FALSE
    ;

concurrencyLevel
    : INT
    ;
