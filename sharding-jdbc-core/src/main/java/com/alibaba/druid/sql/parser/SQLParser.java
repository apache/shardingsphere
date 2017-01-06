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

import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
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
    
    @Getter
    private String dbType;
    
    public SQLParser(final Lexer lexer) {
        this(lexer, null);
    }
    
    public SQLParser(final Lexer lexer, final String dbType) {
        this.lexer = lexer;
        this.dbType = dbType;
    }
    
    protected final void accept(final Token token) {
        if (lexer.equalToken(token)) {
            lexer.nextToken();
            return;
        }
        throw new ParserException(lexer, token);
    }
    
    protected final void accept(final String text) {
        if (lexer.identifierEquals(text)) {
            lexer.nextToken();
            return;
        }
        throw new ParserException(lexer);
    }
    
    protected String as() {
        String result = null;
        if (lexer.equalToken(Token.AS)) {
            lexer.nextToken();
            if (lexer.equalToken(Token.LITERAL_ALIAS) || lexer.equalToken(Token.LITERAL_CHARS)) {
                result = '"' + lexer.getLiterals() + '"';
                lexer.nextToken();
            } else if (lexer.equalToken(Token.IDENTIFIER)) {
                result = lexer.getLiterals();
                lexer.nextToken();
            } else {
                switch (lexer.getToken()) {
                    case KEY:
                    case INDEX:
                    case CASE:
                    case MODEL:
                    case PCTFREE:
                    case INITRANS:
                    case MAXTRANS:
                    case SEGMENT:
                    case CREATION:
                    case IMMEDIATE:
                    case DEFERRED:
                    case STORAGE:
                    case NEXT:
                    case MINEXTENTS:
                    case MAXEXTENTS:
                    case MAXSIZE:
                    case PCTINCREASE:
                    case FLASH_CACHE:
                    case CELL_FLASH_CACHE:
                    case KEEP:
                    case NONE:
                    case LOB:
                    case STORE:
                    case ROW:
                    case CHUNK:
                    case CACHE:
                    case NOCACHE:
                    case LOGGING:
                    case NOCOMPRESS:
                    case KEEP_DUPLICATES:
                    case EXCEPTIONS:
                    case PURGE:
                    case INITIALLY:
                    case END:
                    case COMMENT:
                    case ENABLE:
                    case DISABLE:
                    case SEQUENCE:
                    case USER:
                    case ANALYZE:
                    case OPTIMIZE:
                    case GRANT:
                    case REVOKE:
                    case FULL:
                    case TO:
                    case NEW:
                    case INTERVAL:
                    case LOCK:
                    case LIMIT:
                    case IDENTIFIED:
                    case PASSWORD:
                    case BINARY:
                    case WINDOW:
                    case OFFSET:
                    case SHARE:
                    case START:
                    case CONNECT:
                    case MATCHED:
                    case ERRORS:
                    case REJECT:
                    case UNLIMITED:
                    case BEGIN:
                    case EXCLUSIVE:
                    case MODE:
                    case ADVISE:
                    case TYPE:
                        result = lexer.getLiterals();
                        lexer.nextToken();
                        return result;
                    case QUESTION:
                        result = Token.QUESTION.getName();
                        lexer.nextToken();
                    default:
                        break;
                }
            }
            if (null != result) {
                while (lexer.equalToken(Token.DOT)) {
                    lexer.nextToken();
                    result += Token.DOT.getName() + lexer.getToken().name();
                    lexer.nextToken();
                }
                return result;
            }
            if (lexer.equalToken(Token.LEFT_PAREN)) {
                return null;
            }
            throw new ParserException(lexer);
        }
        if (lexer.equalToken(Token.IDENTIFIER) || lexer.equalToken(Token.USER) || lexer.equalToken(Token.END)) {
            result = lexer.getLiterals();
            lexer.nextToken();
        } else if (lexer.equalToken(Token.LITERAL_ALIAS)) {
            result = '"' + lexer.getLiterals() + '"';
            lexer.nextToken();
        } else if (lexer.equalToken(Token.LITERAL_CHARS)) {
            result = "'" + lexer.getLiterals() + "'";
            lexer.nextToken();
        } else if (lexer.equalToken(Token.CASE)) {
            result = lexer.getToken().name();
            lexer.nextToken();
        }
        if (lexer.equalToken(Token.KEY) || lexer.equalToken(Token.INTERVAL) || lexer.equalToken(Token.CONSTRAINT)) {
            result = lexer.getToken().name();
            lexer.nextToken();
        }
        return result;
    }
}
