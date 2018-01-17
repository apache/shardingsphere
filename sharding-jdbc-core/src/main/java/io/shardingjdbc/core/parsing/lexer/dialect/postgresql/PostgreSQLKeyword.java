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

package io.shardingjdbc.core.parsing.lexer.dialect.postgresql;

import io.shardingjdbc.core.parsing.lexer.token.Keyword;

/**
 * PostgreSQL keyword.
 * 
 * @author zhangliang 
 */
public enum PostgreSQLKeyword implements Keyword {
    
    SHOW,
    ONLY,
    ARRAY,
    FIRST,
    NEXT,
    LAST,
    LIMIT,
    OFFSET,
    RESTART,
    RECURSIVE,
    CURRENT,
    RESTRICT,
    NOWAIT,
    TYPE,
    UNLOGGED,
    CONTINUE,
    RETURNING,
    ROWS,
    SHARE,
    IDENTITY,
    WINDOW,
    STATISTICS,
    RESET,
    PLAIN,
    EXTERNAL,
    EXTENDED,
    MAIN,
    VALID,
    REPLICA,
    ALWAYS,
    RULE,
    CLUSTER,
    OIDS,
    INHERIT,
    OWNER,
    DEFERRABLE,
    INITIALLY,
    DEFERRED,
    IMMEDIATE,
    EXTRACT
}
