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

import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
// TODO 与SQLStatementParser合并?
public class SQLParser {
    
    @Getter
    private final Lexer lexer;
    
    public SQLParser(final Lexer lexer) {
        this.lexer = lexer;
    }
    
    protected Optional<String> as() {
        if (lexer.skipIfEqual(Token.AS)) {
            // TODO 判断Literals是符号则返回null, 目前仅判断为LEFT_PAREN
            if (lexer.equalToken(Token.LEFT_PAREN)) {
                return Optional.absent();
            }
            String result = lexer.getLiterals();
            lexer.nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (lexer.equalToken(Token.IDENTIFIER, Token.LITERAL_ALIAS, Token.LITERAL_CHARS, Token.USER, Token.END, Token.CASE, Token.KEY, Token.INTERVAL, Token.CONSTRAINT)) {
            String result = lexer.getLiterals();
            lexer.nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    protected final TableContext parseSingleTable(final SQLContext sqlContext) {
        boolean hasParentheses = false;
        if (getLexer().skipIfEqual(Token.LEFT_PAREN)) {
            if (getLexer().equalToken(Token.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        TableContext result;
        int beginPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        String literals = getLexer().getLiterals();
        getLexer().nextToken();
        if (getLexer().skipIfEqual(Token.DOT)) {
            String tableName = getLexer().getLiterals();
            getLexer().nextToken();
            if (hasParentheses) {
                getLexer().accept(Token.RIGHT_PAREN);
            }
            result = new TableContext(tableName, SQLUtil.getExactlyValue(literals), as());
        } else {
            if (hasParentheses) {
                getLexer().accept(Token.RIGHT_PAREN);
            }
            result = new TableContext(literals, SQLUtil.getExactlyValue(literals), as());
        }
        if (isJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlContext.getSqlTokens().add(new TableToken(beginPosition, result.getOriginalLiterals(), result.getName()));
        sqlContext.getTables().add(result);
        return result;
    }
    
    protected final boolean isJoin() {
        if (getLexer().skipIfEqual(Token.LEFT, Token.RIGHT, Token.FULL)) {
            getLexer().skipIfEqual(Token.OUTER);
            getLexer().accept(Token.JOIN);
            return true;
        } else if (getLexer().skipIfEqual(Token.INNER)) {
            getLexer().accept(Token.JOIN);
            return true;
        } else if (getLexer().skipIfEqual(Token.JOIN, Token.COMMA, Token.STRAIGHT_JOIN)) {
            return true;
        } else if (getLexer().skipIfEqual(Token.CROSS)) {
            if (getLexer().skipIfEqual(Token.JOIN, Token.APPLY)) {
                return true;
            }
        } else if (getLexer().skipIfEqual(Token.OUTER)) {
            if (getLexer().skipIfEqual(Token.APPLY)) {
                return true;
            }
        }
        return false;
    }
}
