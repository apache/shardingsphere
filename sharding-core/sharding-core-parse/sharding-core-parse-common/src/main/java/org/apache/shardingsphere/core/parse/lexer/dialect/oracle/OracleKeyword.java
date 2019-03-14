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

package org.apache.shardingsphere.core.parse.lexer.dialect.oracle;

import org.apache.shardingsphere.core.parse.lexer.token.Keyword;

/**
 * Oracle keyword.
 * 
 * @author zhangliang 
 */
public enum OracleKeyword implements Keyword {
    
    LOCKED,
    CREATION,
    UPDATED,
    UPSERT,
    CONNECT_BY_ROOT,
    STORE,
    MERGE,
    PURGE,
    ROWS,
    GOTO,
    ONLY,
    AUTOMATIC,
    MAIN,
    PCTINCREASE,
    CHUNK,
    LIMIT,
    GROUPING,
    ROLLUP,
    CUBE,
    UNLIMITED,
    SIBLINGS,
    INCLUDE,
    EXCLUDE,
    PIVOT,
    UNPIVOT,
    EXCEPTION,
    EXCEPTIONS,
    ERRORS,
    DEFERRED,
    CONNECT,
    EXCLUSIVE,
    NAV,
    VERSIONS,
    WAIT,
    NOWAIT,
    SAMPLE,
    CONTINUE,
    SYSDATE,
    TIMESTAMP,
    SEGMENT,
    PARTITION,
    SUBPARTITION,
    RETURN,
    RETURNING,
    REJECT,
    MAXTRANS,
    MINEXTENTS,
    MAXEXTENTS,
    MATCHED,
    LOB,
    DIMENSION,
    FORCE,
    MODEL,
    FIRST,
    NEXT,
    LAST,
    SHARE,
    EXTRACT,
    NOCOMPRESS,
    MODE,
    RULES,
    INITIALLY,
    KEEP,
    KEEP_DUPLICATES,
    REFERENCE,
    SEED,
    SESSION,
    IGNORE,
    MEASURES,
    LOGGING,
    MAXSIZE,
    FLASH_CACHE,
    CELL_FLASH_CACHE,
    SKIP,
    NONE,
    NULLS,
    SINGLE,
    SCN,
    INITRANS,
    BLOCK,
    IMMEDIATE,
    SEQUENTIAL,
    PCTFREE,
    BINARY,
    INSENSITIVE,
    SCROLL,
    PRIOR,
    XML,
    INCREMENT,
    MINVALUE,
    MAXVALUE,
    START,
    CACHE,
    NOCACHE,
    CYCLE,
    NOCYCLE,
}
