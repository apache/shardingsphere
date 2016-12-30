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
package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.lexer.Token;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public class SQLStatementParser extends SQLParser {
    
    protected final SQLExprParser exprParser;
    
    public SQLStatementParser(final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
    }
    
    
    
    public SQLDeleteStatement parseDeleteStatement() {
        getLexer().nextToken();
        SQLDeleteStatement result = createSQLDeleteStatement();
        while (getIdentifiersBetweenDeleteAndFrom().contains(getLexer().getLiterals())) {
            result.getIdentifiersBetweenDeleteAndFrom().add(getLexer().getLiterals());
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
        }
        SQLTableSource tableSource = createSQLSelectParser().parseTableSource();
        if (tableSource instanceof SQLJoinTableSource) {
            throw new UnsupportedOperationException("Cannot support delete Multiple-Table.");
        }
        result.setTableSource(tableSource);
        parseCustomizedParserBetweenTableAndNextIdentifier(result);
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            result.setWhere(exprParser.expr());
        }
        parseCustomizedParserAfterWhere(result);
        return result;
    }
    
    protected SQLDeleteStatement createSQLDeleteStatement() {
        return new SQLDeleteStatement(getDbType());
    }
    
    protected Set<String> getIdentifiersBetweenDeleteAndFrom() {
        return Collections.emptySet();
    }
    
    protected void parseCustomizedParserBetweenTableAndNextIdentifier(final SQLDeleteStatement deleteStatement) {
    }
    
    protected void parseCustomizedParserAfterWhere(final SQLDeleteStatement deleteStatement) {
    }
    
    /**
     * 解析SQL.
     * 
     * @return SQL解析对象
     */
    public SQLStatement parseStatement() {
        return parseStatementList(1).get(0);
    }
    
    protected List<SQLStatement> parseStatementList(final int max) {
        List<SQLStatement> result = new ArrayList<>(-1 == max ? 16 : max);
        while (true) {
            if (-1 != max && result.size() >= max) {
                return result;
            }
            if (getLexer().isEndToken()) {
                return result;
            }
            if (getLexer().equalToken(Token.SEMI)) {
                getLexer().nextToken();
                continue;
            }
            if (getLexer().equalToken(Token.SELECT)) {
                result.add(parseSelect());
                continue;
            }
            if (getLexer().equalToken(Token.INSERT)) {
                result.add(SQLInsertParserFactory.newInstance(exprParser, getDbType()).parse());
                continue;
            }
            if (getLexer().equalToken(Token.UPDATE)) {
                result.add(SQLUpdateParserFactory.newInstance(exprParser, getDbType()).parse());
                continue;
            }
            if (getLexer().equalToken(Token.DELETE)) {
                result.add(parseDeleteStatement());
                continue;
            }
            if (getLexer().equalToken(Token.CREATE) || getLexer().equalToken(Token.EXPLAIN) || getLexer().equalToken(Token.SET) || getLexer().equalToken(Token.ALTER) 
                    || getLexer().equalToken(Token.DROP) || getLexer().equalToken(Token.TRUNCATE) || getLexer().equalToken(Token.USE) || getLexer().equalToken(Token.GRANT)
                    || getLexer().equalToken(Token.REVOKE) || getLexer().equalToken(Token.LEFT_BRACE) || getLexer().identifierEquals("CALL") || getLexer().identifierEquals("RENAME")
                    || getLexer().identifierEquals("RELEASE") || getLexer().identifierEquals("SAVEPOINT") || getLexer().identifierEquals("ROLLBACK") || getLexer().identifierEquals("COMMIT") 
                    || getLexer().equalToken(Token.SHOW)) {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                int currentPosition = getLexer().getCurrentPosition();
                getLexer().nextToken();
                if (getLexer().equalToken(Token.SELECT)) {
                    getLexer().setCurrentPosition(currentPosition);
                    getLexer().setToken(Token.LEFT_PAREN);
                    result.add(parseSelect());
                    continue;
                }
            }
            if (parseStatementListDialect(result)) {
                continue;
            }
            if (getLexer().equalToken(Token.COMMENT)) {
                result.add(this.parseComment());
                continue;
            }
            throw new ParserException(getLexer());
        }
    }
    
    protected SQLSelectStatement parseSelect() {
        return new SQLSelectStatement(createSQLSelectParser().select());
    }
    
    protected SQLSelectParser createSQLSelectParser() {
        return new SQLSelectParser(exprParser);
    }
    

    public boolean parseStatementListDialect(final List<SQLStatement> statementList) {
        return false;
    }
    
    public SQLCommentStatement parseComment() {
        accept(Token.COMMENT);
        SQLCommentStatement stmt = new SQLCommentStatement();

        accept(Token.ON);

        if (getLexer().equalToken(Token.TABLE)) {
            stmt.setType(SQLCommentStatement.Type.TABLE);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.COLUMN)) {
            stmt.setType(SQLCommentStatement.Type.COLUMN);
            getLexer().nextToken();
        }

        stmt.setOn(this.exprParser.name());

        accept(Token.IS);
        stmt.setComment(this.exprParser.expr());

        return stmt;
    }
}
