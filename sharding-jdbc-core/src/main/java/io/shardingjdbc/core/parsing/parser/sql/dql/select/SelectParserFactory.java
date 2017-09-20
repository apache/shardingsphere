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

package io.shardingjdbc.core.parsing.parser.sql.dql.select;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.sql.MySQLSelectParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.sql.OracleSelectParser;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.sql.PostgreSQLSelectParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.sql.SQLServerSelectParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Select parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectParserFactory {
    
    /**
     * Create select parser instance.
     *
     * @param dbType database type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine.
     * @return select parser instance
     */
    public static AbstractSelectParser newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLSelectParser(shardingRule, lexerEngine);
            case Oracle:
                return new OracleSelectParser(shardingRule, lexerEngine);
            case SQLServer:
                return new SQLServerSelectParser(shardingRule, lexerEngine);
            case PostgreSQL:
                return new PostgreSQLSelectParser(shardingRule, lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
