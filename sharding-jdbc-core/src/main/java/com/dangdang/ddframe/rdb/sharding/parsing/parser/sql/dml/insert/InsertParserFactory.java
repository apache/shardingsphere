/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.insert;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.sql.MySQLInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.sql.OracleInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.sql.PostgreSQLInsertParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.sql.SQLServerInsertParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Insert parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertParserFactory {
    
    /**
     * Create insert parser instance.
     *
     * @param dbType database type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine.
     * @return insert parser instance
     */
    public static AbstractInsertParser newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLInsertParser(shardingRule, lexerEngine);
            case Oracle:
                return new OracleInsertParser(shardingRule, lexerEngine);
            case SQLServer:
                return new SQLServerInsertParser(shardingRule, lexerEngine);
            case PostgreSQL:
                return new PostgreSQLInsertParser(shardingRule, lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
