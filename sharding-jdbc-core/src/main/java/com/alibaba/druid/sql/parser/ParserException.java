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

public class ParserException extends RuntimeException {
    
    private static final long serialVersionUID = -6408790652103666096L;
    
    private static final String UNMATCH_MESSAGE = "SQL syntax error, expected token is '%s', actual token is '%s', literals is '%s'.";
    
    private static final String TOKEN_ERROR_MESSAGE = "SQL syntax error, token is '%s', literals is '%s'.";
    
    public ParserException(final Lexer lexer, final Token expectedToken) {
        super(String.format(UNMATCH_MESSAGE, expectedToken, lexer.getToken(), lexer.getLiterals()));
    }
    
    public ParserException(final Lexer lexer) {
        super(String.format(TOKEN_ERROR_MESSAGE, lexer.getToken(), lexer.getLiterals()));
    }
    
    public ParserException(final String message) {
        super(message);
    }
}
