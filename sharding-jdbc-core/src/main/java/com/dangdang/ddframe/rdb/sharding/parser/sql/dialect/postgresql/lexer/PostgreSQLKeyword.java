/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License,Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Keyword;

/**
 * PostgreSQL关键词.
 * 
 * @author zhangliang 
 */
public enum PostgreSQLKeyword implements Keyword {
    
    SHOW,
    OF,
    IF,
    ONLY,
    TRUE,
    FALSE,
    ARRAY,
    FIRST,
    NEXT,
    LAST,
    ROW,
    LIMIT,
    OFFSET,
    SIBLINGS,
    RESTART,
    RECURSIVE,
    CASCADE,
    CURRENT,
    RESTRICT,
    NOWAIT,
    TYPE,
    UNLOGGED,
    CONTINUE,
    RETURNING,
    ROWS,
    SHARE,
    TEMP,
    TEMPORARY,
    IDENTITY,
    WINDOW
}
