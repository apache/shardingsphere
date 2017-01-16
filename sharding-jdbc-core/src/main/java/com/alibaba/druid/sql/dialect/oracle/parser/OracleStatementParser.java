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
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class OracleStatementParser extends SQLStatementParser {
    
    public OracleStatementParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new OracleExprParser(sql));
    }
    
    @Override
    protected OracleSelectParser createSQLSelectParser() {
        return new OracleSelectParser(getExprParser());
    }
    
    @Override
    public SQLStatement parseStatement() {
        if (getLexer().equalToken(Token.SEMI)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.SELECT)) {
            return new SQLSelectStatement(new OracleSelectParser(getExprParser()).select(), JdbcConstants.ORACLE);
        }
        if (getLexer().equalToken(Token.INSERT)) {
            return new OracleInsertParser(getExprParser()).parse();
        }
        if (getLexer().equalToken(Token.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(getShardingRule(), getParameters(), getExprParser(), JdbcConstants.ORACLE).parse();
        }
        if (getLexer().equalToken(Token.DELETE)) {
            return parseDeleteStatement();
        }
        if (getLexer().equalToken(Token.SLASH)) {
            getLexer().nextToken();
            return new OraclePLSQLCommitStatement();
        }
        if (getLexer().equalToken(Token.WITH)) {
            return new SQLSelectStatement(new OracleSelectParser(getExprParser()).select());
        }
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            return new OracleExprStatement(getExprParser().expr());
        }
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            int currentPosition = getLexer().getCurrentPosition();
            getLexer().nextToken();
            if (getLexer().equalToken(Token.SELECT)) {
                getLexer().setCurrentPosition(currentPosition);
                getLexer().setToken(Token.LEFT_PAREN);
                return parseSelect();
            }
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        if (getLexer().equalToken(Token.COMMENT)) {
            return parseComment();
        }
        if (getLexer().equalToken(Token.DOUBLE_LT)) {
            getLexer().nextToken();
            OracleLabelStatement result = new OracleLabelStatement(getExprParser().name());
            accept(Token.DOUBLE_GT);
            return result;
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

    private OracleReturningClause parseReturningClause() {
        OracleReturningClause clause = null;

        if (getLexer().equalToken(Token.RETURNING)) {
            getLexer().nextToken();
            clause = new OracleReturningClause();

            while (true) {
                clause.getItems().add(getExprParser().expr());
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
            accept(Token.INTO);
            while (true) {
                clause.getValues().add(getExprParser().expr());
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
                deleteStatement.setTableName(getExprParser().name());
                accept(Token.RIGHT_PAREN);
            } else {
                deleteStatement.setTableName(getExprParser().name());
            }

            deleteStatement.setAlias(as());
        }

        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            deleteStatement.setWhere(getExprParser().expr());
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
