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

package io.shardingsphere.core.parsing;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.LexerEngineFactory;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.Assist;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.DescribeStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowColumnsStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowOtherStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dcl.DCLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLStatement;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;

/**
 * SQL judge engine.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    /**
     * judge SQL Type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, sql);
        lexerEngine.nextToken();
        while (true) {
            TokenType tokenType = lexerEngine.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (isDQL(tokenType)) {
                    return getDQLStatement();
                }
                if (isDML(tokenType)) {
                    return getDMLStatement(tokenType);
                }
                if (isTCL(tokenType)) {
                    return getTCLStatement();
                }
                if (isDAL(tokenType)) {
                    return getDALStatement(tokenType, lexerEngine);
                }
                lexerEngine.nextToken();
                TokenType secondaryTokenType = lexerEngine.getCurrentToken().getType();
                if (isDCL(tokenType, secondaryTokenType)) {
                    return getDCLStatement();
                }
                if (isDDL(tokenType, secondaryTokenType)) {
                    return getDDLStatement();
                }
            } else {
                lexerEngine.nextToken();
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
        }
    }
    
    private boolean isDQL(final TokenType tokenType) {
        return DefaultKeyword.SELECT == tokenType;
    }
    
    private boolean isDML(final TokenType tokenType) {
        return DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType;
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
    
    private boolean isTCL(final TokenType tokenType) {
        return DefaultKeyword.SET == tokenType || DefaultKeyword.COMMIT == tokenType || DefaultKeyword.ROLLBACK == tokenType
                || DefaultKeyword.SAVEPOINT == tokenType || DefaultKeyword.BEGIN == tokenType;
    }
    
    private boolean isDAL(final TokenType tokenType) {
        return DefaultKeyword.USE == tokenType || DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType || MySQLKeyword.SHOW == tokenType;
    }
    
    private SQLStatement getDQLStatement() {
        return new SelectStatement();
    }
    
    private SQLStatement getDMLStatement(final TokenType tokenType) {
        if (DefaultKeyword.INSERT == tokenType) {
            return new InsertStatement();
        }
        return new DMLStatement();
    }
    
    private SQLStatement getDDLStatement() {
        return new DDLStatement();
    }
    
    private SQLStatement getDCLStatement() {
        return new DCLStatement();
    }
    
    private SQLStatement getTCLStatement() {
        return new TCLStatement();
    }
    
    private SQLStatement getDALStatement(final TokenType tokenType, final LexerEngine lexerEngine) {
        if (DefaultKeyword.USE == tokenType) {
            return new UseStatement();
        }
        if (DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType) {
            return new DescribeStatement();
        }
        return getShowStatement(lexerEngine);
    }
    
    private SQLStatement getShowStatement(final LexerEngine lexerEngine) {
        lexerEngine.nextToken();
        if (MySQLKeyword.DATABASES == lexerEngine.getCurrentToken().getType()) {
            return new ShowDatabasesStatement();
        }
        if (MySQLKeyword.TABLES == lexerEngine.getCurrentToken().getType()) {
            return new ShowTablesStatement();
        }
        if (MySQLKeyword.COLUMNS == lexerEngine.getCurrentToken().getType()) {
            return new ShowColumnsStatement();
        }
        return new ShowOtherStatement();
    }
}
