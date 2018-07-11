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

package io.shardingsphere.core.parsing.parser.sql;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.dal.describe.DescribeParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dal.show.ShowParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dal.use.UseParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.alter.AlterUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.create.CreateUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.deny.DenyUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.drop.DropUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.grant.GrantUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.rename.RenameUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dcl.revoke.RevokeUserParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.alter.table.AlterTableParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.index.CreateIndexParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.drop.index.DropIndexParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.drop.table.DropTableParserFactory;
import io.shardingsphere.core.parsing.parser.sql.ddl.truncate.table.TruncateTableParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dml.delete.DeleteParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dml.update.UpdateParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectParserFactory;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLParserFactory;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;

/**
 * SQL parser factory.
 *
 * @author zhangliang
 * @author panjuan
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
     * @param shardingMetaData sharding metadata
     * @return SQL parser
     */
    public static SQLParser newInstance(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingMetaData shardingMetaData) {
        if (isDQL(tokenType)) {
            return getDQLParser(dbType, shardingRule, lexerEngine, shardingMetaData);
        }
        if (isDML(tokenType)) {
            return getDMLParser(dbType, tokenType, shardingRule, lexerEngine, shardingMetaData);
        }
        if (isTCL(tokenType)) {
            return getTCLParser(dbType, shardingRule, lexerEngine);
        }
        if (isDAL(tokenType)) {
            return getDALParser(dbType, (Keyword) tokenType, shardingRule, lexerEngine);
        }
        lexerEngine.nextToken();
        TokenType secondaryTokenType = lexerEngine.getCurrentToken().getType();
        if (isDCL(tokenType, secondaryTokenType)) {
            return getDCLParser(dbType, tokenType, shardingRule, lexerEngine);
        }
        if (isDDL(tokenType, secondaryTokenType)) {
            return getDDLParser(dbType, tokenType, shardingRule, lexerEngine);
        }
        throw new SQLParsingUnsupportedException(tokenType);
    }
    
    private static boolean isDQL(final TokenType tokenType) {
        return DefaultKeyword.SELECT == tokenType;
    }
    
    private static boolean isDML(final TokenType tokenType) {
        return DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType;
    }
    
    private static boolean isTCL(final TokenType tokenType) {
        return DefaultKeyword.SET == tokenType || DefaultKeyword.COMMIT == tokenType || DefaultKeyword.ROLLBACK == tokenType
                || DefaultKeyword.SAVEPOINT == tokenType || DefaultKeyword.BEGIN == tokenType;
    }
    
    private static boolean isDAL(final TokenType tokenType) {
        return DefaultKeyword.USE == tokenType || DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType || MySQLKeyword.SHOW == tokenType;
    }
    
    private static boolean isDDL(final TokenType tokenType, final TokenType secondaryTokenType) {
        Collection<DefaultKeyword> primaryTokens = Arrays.asList(DefaultKeyword.CREATE, DefaultKeyword.ALTER, DefaultKeyword.DROP, DefaultKeyword.TRUNCATE);
        Collection<DefaultKeyword> secondaryTokens = Arrays.asList(DefaultKeyword.LOGIN, DefaultKeyword.USER, DefaultKeyword.ROLE);
        return primaryTokens.contains(tokenType) && !secondaryTokens.contains(secondaryTokenType);
    }
    
    private static boolean isDCL(final TokenType tokenType, final TokenType secondaryTokenType) {
        Collection<DefaultKeyword> primaryTokens = Arrays.asList(DefaultKeyword.GRANT, DefaultKeyword.REVOKE, DefaultKeyword.DENY);
        Collection<DefaultKeyword> secondaryTokens = Arrays.asList(DefaultKeyword.LOGIN, DefaultKeyword.USER, DefaultKeyword.ROLE);
        if (primaryTokens.contains(tokenType)) {
            return true;
        }
        primaryTokens = Arrays.asList(DefaultKeyword.CREATE, DefaultKeyword.ALTER, DefaultKeyword.DROP, DefaultKeyword.RENAME);
        return primaryTokens.contains(tokenType) && secondaryTokens.contains(secondaryTokenType);
    }
    
    private static SQLParser getDQLParser(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingMetaData shardingMetaData) {
        return SelectParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingMetaData);
    }
    
    private static SQLParser getDMLParser(
            final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingMetaData shardingMetaData) {
        switch ((DefaultKeyword) tokenType) {
            case INSERT:
                return InsertParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingMetaData);
            case UPDATE:
                return UpdateParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DELETE:
                return DeleteParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
    
    private static SQLParser getDDLParser(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        lexerEngine.skipUntil(DefaultKeyword.INDEX, DefaultKeyword.TABLE);
        if (lexerEngine.isEnd()) {
            throw new SQLParsingUnsupportedException(tokenType);
        }
        return DefaultKeyword.TABLE == lexerEngine.getCurrentToken().getType() ? getTableDDLParser(dbType, tokenType, shardingRule, lexerEngine)
                : getIndexDDLParser(dbType, tokenType, shardingRule, lexerEngine);
    }
    
    private static SQLParser getTableDDLParser(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
            case CREATE:
                return CreateTableParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case ALTER:
                return AlterTableParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DROP:
                return DropTableParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case TRUNCATE:
                return TruncateTableParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
    
    private static SQLParser getIndexDDLParser(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
            case CREATE:
                return CreateIndexParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DROP:
                return DropIndexParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
    
    private static SQLParser getTCLParser(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        return TCLParserFactory.newInstance(dbType, shardingRule, lexerEngine);
    }
    
    private static SQLParser getDALParser(final DatabaseType dbType, final Keyword tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        if (DefaultKeyword.USE == tokenType) {
            return UseParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        if (DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType) {
            return DescribeParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        if (MySQLKeyword.SHOW == tokenType) {
            return ShowParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        throw new SQLParsingUnsupportedException(tokenType);
    }
    
    private static SQLParser getDCLParser(final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch ((DefaultKeyword) tokenType) {
            case CREATE:
                return CreateUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case ALTER:
                return AlterUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DROP:
                return DropUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case RENAME:
                return RenameUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case GRANT:
                return GrantUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case REVOKE:
                return RevokeUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DENY:
                return DenyUserParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
}
