/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.lexer.dialect.mysql;

import io.shardingjdbc.core.parsing.lexer.token.Keyword;

/**
 * MySQL keyword.
 * 
 * @author zhangliang 
 */
public enum MySQLKeyword implements Keyword {
    
    SHOW, 
    DUAL, 
    LIMIT, 
    OFFSET, 
    VALUE, 
    BEGIN, 
    FORCE, 
    PARTITION, 
    DISTINCTROW, 
    KILL, 
    QUICK, 
    BINARY, 
    CACHE, 
    SQL_CACHE, 
    SQL_NO_CACHE, 
    SQL_SMALL_RESULT, 
    SQL_BIG_RESULT, 
    SQL_BUFFER_RESULT, 
    SQL_CALC_FOUND_ROWS, 
    LOW_PRIORITY, 
    HIGH_PRIORITY, 
    OPTIMIZE, 
    ANALYZE, 
    IGNORE, 
    CHANGE,
    FIRST,
    FULLTEXT,
    SPATIAL,
    ALGORITHM,
    CHARACTER,
    COLLATE, 
    DISCARD,
    IMPORT,
    VALIDATION, 
    REORGANIZE,
    EXCHANGE,
    REBUILD,
    REPAIR,
    REMOVE,
    UPGRADE,
    KEY_BLOCK_SIZE,
    AUTO_INCREMENT,
    AVG_ROW_LENGTH,
    CHECKSUM,
    COMPRESSION,
    CONNECTION,
    DIRECTORY,
    DELAY_KEY_WRITE,
    ENCRYPTION,
    ENGINE,
    INSERT_METHOD,
    MAX_ROWS,
    MIN_ROWS,
    PACK_KEYS,
    ROW_FORMAT,
    DYNAMIC,
    FIXED,
    COMPRESSED,
    REDUNDANT,
    COMPACT,
    STATS_AUTO_RECALC,
    STATS_PERSISTENT,
    STATS_SAMPLE_PAGES, 
    DISK,
    MEMORY,
    ROLLUP,
    RESTRICT,
    STRAIGHT_JOIN, 
    REGEXP,
    KEY
}
