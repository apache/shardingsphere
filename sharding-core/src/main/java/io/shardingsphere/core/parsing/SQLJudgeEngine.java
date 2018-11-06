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
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.DescribeStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.SetStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowColumnsStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowCreateTableStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowIndexStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowOtherStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTableStatusStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowTablesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dcl.DCLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.sql.tcl.TCLStatement;
import io.shardingsphere.core.parsing.parser.token.SchemaToken;
import lombok.RequiredArgsConstructor;

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
     * Judge SQL type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, sql);
        lexerEngine.nextToken();
        while (true) {
            TokenType tokenType = lexerEngine.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (DQLStatement.isDQL(tokenType)) {
                    return getDQLStatement();
                }
                if (DMLStatement.isDML(tokenType)) {
                    return getDMLStatement(tokenType);
                }
                if (TCLStatement.isTCL(tokenType)) {
                    return getTCLStatement(tokenType);
                }
                if (DALStatement.isDAL(tokenType)) {
                    return getDALStatement(tokenType, lexerEngine);
                }
                lexerEngine.nextToken();
                TokenType secondaryTokenType = lexerEngine.getCurrentToken().getType();
                if (DDLStatement.isDDL(tokenType, secondaryTokenType)) {
                    return getDDLStatement();
                }
                if (DCLStatement.isDCL(tokenType, secondaryTokenType)) {
                    return getDCLStatement();
                }
            } else {
                lexerEngine.nextToken();
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
        }
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
    
    private SQLStatement getTCLStatement(final TokenType tokenType) {
        if (DefaultKeyword.SET == tokenType) {
            return new SetStatement();
        }
        return new TCLStatement();
    }
    
    private SQLStatement getDALStatement(final TokenType tokenType, final LexerEngine lexerEngine) {
        if (DefaultKeyword.USE == tokenType) {
            lexerEngine.nextToken();
            return new UseStatement(lexerEngine.getCurrentToken().getLiterals());
        }
        if (DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType) {
            return new DescribeStatement();
        }
        return getShowStatement(lexerEngine);
    }
    
    private SQLStatement getShowStatement(final LexerEngine lexerEngine) {
        lexerEngine.nextToken();
        lexerEngine.skipIfEqual(DefaultKeyword.FULL);
        if (lexerEngine.equalAny(MySQLKeyword.DATABASES)) {
            return new ShowDatabasesStatement();
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.TABLE, MySQLKeyword.STATUS)) {
            return parseShowTableStatus(lexerEngine);
        }
        if (lexerEngine.skipIfEqual(MySQLKeyword.TABLES)) {
            return parseShowTables(lexerEngine);
        }
        if (lexerEngine.skipIfEqual(MySQLKeyword.COLUMNS, MySQLKeyword.FIELDS)) {
            return parseShowColumnsFields(lexerEngine);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.CREATE) && lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            return parseShowCreateTable(lexerEngine);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.INDEX, MySQLKeyword.INDEXES, MySQLKeyword.KEYS)) {
            return parseShowIndex(lexerEngine);
        }
        return new ShowOtherStatement();
    }
    
    private DALStatement parseShowTableStatus(final LexerEngine lexerEngine) {
        DALStatement result = new ShowTableStatusStatement();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN)) {
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            result.addSQLToken(new SchemaToken(beginPosition, lexerEngine.getCurrentToken().getLiterals(), null));
        }
        return result;
    }
    
    private DALStatement parseShowTables(final LexerEngine lexerEngine) {
        DALStatement result = new ShowTablesStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN)) {
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            result.addSQLToken(new SchemaToken(beginPosition, lexerEngine.getCurrentToken().getLiterals(), null));
        }
        return result;
    }
    
    private DALStatement parseShowColumnsFields(final LexerEngine lexerEngine) {
        DALStatement result = new ShowColumnsStatement();
        lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN);
        parseSingleTableWithSchema(lexerEngine, result);
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN)) {
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            result.addSQLToken(new SchemaToken(beginPosition, lexerEngine.getCurrentToken().getLiterals(), null));
        }
        return result;
    }
    
    private DALStatement parseShowCreateTable(final LexerEngine lexerEngine) {
        DALStatement result = new ShowCreateTableStatement();
        parseSingleTableWithSchema(lexerEngine, result);
        return result;
    }
    
    private DALStatement parseShowIndex(final LexerEngine lexerEngine) {
        DALStatement result = new ShowIndexStatement();
        lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN);
        parseSingleTableWithSchema(lexerEngine, result);
        if (lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN)) {
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            result.addSQLToken(new SchemaToken(beginPosition, lexerEngine.getCurrentToken().getLiterals(), null));
        }
        return result;
    }
    
    private void parseSingleTableWithSchema(final LexerEngine lexerEngine, final SQLStatement sqlStatement) {
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            sqlStatement.addSQLToken(new SchemaToken(beginPosition, literals, null));
            lexerEngine.nextToken();
        }
    }
}
