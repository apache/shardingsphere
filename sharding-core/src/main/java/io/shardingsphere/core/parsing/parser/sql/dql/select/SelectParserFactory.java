/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.parser.sql.dql.select;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.sql.MySQLSelectParser;
import io.shardingsphere.core.parsing.parser.dialect.oracle.sql.OracleSelectParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.sql.PostgreSQLSelectParser;
import io.shardingsphere.core.parsing.parser.dialect.sqlserver.sql.SQLServerSelectParser;
import io.shardingsphere.core.rule.ShardingRule;
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
     * @param shardingTableMetaData sharding metadata.
     * @return select parser instance
     */
    public static AbstractSelectParser newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLSelectParser(shardingRule, lexerEngine, shardingTableMetaData);
            case Oracle:
                return new OracleSelectParser(shardingRule, lexerEngine, shardingTableMetaData);
            case SQLServer:
                return new SQLServerSelectParser(shardingRule, lexerEngine, shardingTableMetaData);
            case PostgreSQL:
                return new PostgreSQLSelectParser(shardingRule, lexerEngine, shardingTableMetaData);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
