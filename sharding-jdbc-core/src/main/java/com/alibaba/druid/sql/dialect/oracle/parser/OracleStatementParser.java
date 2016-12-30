/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */
package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.clause.OracleReturningClause;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleDeleteStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleExprStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleLabelStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OraclePLSQLCommitStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.SQLUpdateParserFactory;
import com.alibaba.druid.util.JdbcConstants;

import java.util.ArrayList;
import java.util.List;

public class OracleStatementParser extends SQLStatementParser {
    
    public OracleStatementParser(final String sql) {
        super(new OracleExprParser(sql));
    }
    
    @Override
    protected OracleSelectParser createSQLSelectParser() {
        return new OracleSelectParser(exprParser);
    }
    
    @Override
    public OracleExprParser getExprParser() {
        return (OracleExprParser) exprParser;
    }
    
    @Override
    protected List<SQLStatement> parseStatementList(final int max) {
        List<SQLStatement> result = new ArrayList<>(-1 == max ? 16 : max);
        while (true) {
            if (-1 != max && result.size() >= max) {
                return result;
            }
            if (getLexer().isEndToken()) {
                return result;
            }
            if (getLexer().equalToken(Token.ELSE)) {
                return result;
            }
            if (getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
                continue;
            }
            if (getLexer().equalToken(Token.SELECT)) {
                SQLSelectStatement stmt = new SQLSelectStatement(new OracleSelectParser(this.exprParser).select(), JdbcConstants.ORACLE);
                result.add(stmt);
                continue;
            }
            if (getLexer().equalToken(Token.INSERT)) {
                result.add(new OracleInsertParser(exprParser).parse());
                continue;
            }
            if (getLexer().equalToken(Token.UPDATE)) {
                result.add(SQLUpdateParserFactory.newInstance(exprParser, JdbcConstants.ORACLE).parse());
                continue;
            }
            if (getLexer().equalToken(Token.DELETE)) {
                result.add(parseDeleteStatement());
                continue;
            }
            if (getLexer().equalToken(Token.SLASH)) {
                getLexer().nextToken();
                result.add(new OraclePLSQLCommitStatement());
                continue;
            }
            if (getLexer().equalToken(Token.WITH)) {
                result.add(new SQLSelectStatement(new OracleSelectParser(this.exprParser).select()));
                continue;
            }
            if (getLexer().equalToken(Token.IDENTIFIER)) {
                SQLExpr expr = exprParser.expr();
                OracleExprStatement stmt = new OracleExprStatement(expr);
                result.add(stmt);
                continue;
            }
    
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
        
                if (getLexer().equalToken(Token.SELECT)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.LEFT_PAREN);
                    result.add(this.parseSelect());
                    continue;
                }
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            if (getLexer().equalToken(Token.COMMENT)) {
                result.add(this.parseComment());
                continue;
            }
            if (getLexer().equalToken(Token.DOUBLE_LT)) {
                getLexer().nextToken();
                SQLName label = this.exprParser.name();
                OracleLabelStatement stmt = new OracleLabelStatement(label);
                accept(Token.DOUBLE_GT);
                result.add(stmt);
                continue;
            }
    
            if (getLexer().equalToken(Token.CREATE) || getLexer().equalToken(Token.ALTER) || getLexer().equalToken(Token.MERGE) || getLexer().equalToken(Token.BEGIN)
                    || getLexer().equalToken(Token.DECLARE) || getLexer().equalToken(Token.LOCK) || getLexer().equalToken(Token.EXCEPTION) || getLexer().identifierEquals("EXIT")
                    || getLexer().equalToken(Token.VARIANT) || getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL")
                    || getLexer().equalToken(Token.FETCH) || getLexer().identifierEquals("FETCH") || getLexer().equalToken(Token.EXPLAIN) || getLexer().equalToken(Token.SET)
                    || getLexer().equalToken(Token.GRANT) || getLexer().equalToken(Token.REVOKE) || getLexer().equalToken(Token.FOR) || getLexer().equalToken(Token.LOOP)
                    || getLexer().equalToken(Token.IF) || getLexer().equalToken(Token.GOTO) || getLexer().equalToken(Token.COMMIT) || getLexer().equalToken(Token.SAVEPOINT)
                    || getLexer().identifierEquals("ROLLBACK") || getLexer().identifierEquals("SAVEPOINT") || getLexer().identifierEquals("ROLLBACK") || getLexer().identifierEquals("COMMIT")
                    || getLexer().equalToken(Token.EXPLAIN) || getLexer().equalToken(Token.DROP) || getLexer().equalToken(Token.NULL) || getLexer().equalToken(Token.OPEN)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    private OracleReturningClause parseReturningClause() {
        OracleReturningClause clause = null;

        if (getLexer().equalToken(Token.RETURNING)) {
            getLexer().nextToken();
            clause = new OracleReturningClause();

            while (true) {
                SQLExpr item = exprParser.expr();
                clause.getItems().add(item);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
            accept(Token.INTO);
            while (true) {
                SQLExpr item = exprParser.expr();
                clause.getValues().add(item);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
        }
        return clause;
    }

    public OracleDeleteStatement parseDeleteStatement() {
        OracleDeleteStatement deleteStatement = new OracleDeleteStatement();

        if (getLexer().equalToken(Token.DELETE)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            deleteStatement.getHints().addAll(getExprParser().parseHints());
            if (getLexer().equalToken(Token.FROM)) {
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("ONLY")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);

                SQLName tableName = exprParser.name();
                deleteStatement.setTableName(tableName);

                accept(Token.RIGHT_PAREN);
            } else {
                SQLName tableName = exprParser.name();
                deleteStatement.setTableName(tableName);
            }

            deleteStatement.setAlias(as());
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            deleteStatement.setWhere(this.exprParser.expr());
        }

        if (getLexer().equalToken(Token.RETURNING)) {
            OracleReturningClause clause = this.parseReturningClause();
            deleteStatement.setReturning(clause);
        }
        if (getLexer().identifierEquals("RETURN") || getLexer().identifierEquals("RETURNING")) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().identifierEquals("LOG")) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        return deleteStatement;
    }
}
