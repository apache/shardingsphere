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

package io.shardingjdbc.core.parsing.parser.sql;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.sql.MySQLDescParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.sql.MySQLShowParser;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingjdbc.core.parsing.parser.sql.ddl.alter.AlterParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.ddl.create.CreateParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.ddl.drop.DropParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.ddl.truncate.TruncateParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.dml.delete.DeleteParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.dml.update.UpdateParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectParserFactory;
import io.shardingjdbc.core.parsing.parser.sql.ignore.IgnoreParser;
import io.shardingjdbc.core.parsing.parser.sql.tcl.TCLParserFactory;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SQL parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    /**
     * Create SQL parser.
     *
     * @param dbType database type
     * @param tokenType token type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine
     * @return SQL parser
     */
    public static SQLParser newInstance(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        if (tokenType instanceof DefaultKeyword) {
            Optional<? extends SQLParser> result = getGenericParser(dbType, (DefaultKeyword) tokenType, shardingRule, lexerEngine);
            if (result.isPresent()) {
                return result.get();
            }
        }
        if (DatabaseType.MySQL == dbType) {
            Optional<? extends SQLParser> result = getMySQLParser((Keyword) tokenType, shardingRule, lexerEngine);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new SQLParsingUnsupportedException(tokenType);
    }
    
    private static Optional<? extends SQLParser> getGenericParser(final DatabaseType dbType, final DefaultKeyword tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (tokenType) {
            case SELECT:
                return Optional.of(SelectParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case INSERT:
                return Optional.of(InsertParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case UPDATE:
                return Optional.of(UpdateParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case DELETE:
                return Optional.of(DeleteParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case CREATE:
                return Optional.of(CreateParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case ALTER:
                return Optional.of(AlterParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case DROP:
                return Optional.of(DropParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case TRUNCATE:
                return Optional.of(TruncateParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case SET:
            case COMMIT:
            case ROLLBACK:
            case SAVEPOINT:
            case BEGIN:
                return Optional.of(TCLParserFactory.newInstance(dbType, shardingRule, lexerEngine));
            case USE:
                return Optional.of(new IgnoreParser());
            default:
                return Optional.absent();
        }
    }
    
    private static  Optional<? extends SQLParser> getMySQLParser(final Keyword tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        if (tokenType instanceof DefaultKeyword) {
            switch ((DefaultKeyword) tokenType) {
                case DESC:
                    return Optional.of(new MySQLDescParser(shardingRule, lexerEngine));
                default:
                    return Optional.absent();
            }
        }
        if (tokenType instanceof MySQLKeyword) {
            switch ((MySQLKeyword) tokenType) {
                case DESCRIBE:
                    return Optional.of(new MySQLDescParser(shardingRule, lexerEngine));
                case SHOW:
                    return Optional.of(new MySQLShowParser(shardingRule, lexerEngine));
                default:
                    return Optional.absent();
            }
        }
        return Optional.absent();
    }
}
