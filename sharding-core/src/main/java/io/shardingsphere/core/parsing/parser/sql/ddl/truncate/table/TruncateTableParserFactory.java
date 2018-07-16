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

package io.shardingsphere.core.parsing.parser.sql.ddl.truncate.table;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.sql.MySQLTruncateTableParser;
import io.shardingsphere.core.parsing.parser.dialect.oracle.sql.OracleTruncateTableParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.sql.PostgreSQLTruncateTableParser;
import io.shardingsphere.core.parsing.parser.dialect.sqlserver.sql.SQLServerTruncateTableParser;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Truncate parser factory.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TruncateTableParserFactory {
    
    /**
     * Create truncate parser instance.
     *
     * @param dbType database type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine.
     * @return truncate parser instance
     */
    public static AbstractTruncateTableParser newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLTruncateTableParser(shardingRule, lexerEngine);
            case Oracle:
                return new OracleTruncateTableParser(shardingRule, lexerEngine);
            case SQLServer:
                return new SQLServerTruncateTableParser(shardingRule, lexerEngine);
            case PostgreSQL:
                return new PostgreSQLTruncateTableParser(shardingRule, lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
