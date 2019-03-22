/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.parser.exception;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.lexer.Lexer;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.TokenType;

/**
 * Throw exception when SQL parsing error.
 * 
 * @author zhangliang 
 */
public final class SQLParsingException extends ShardingException {
    
    private static final long serialVersionUID = -6408790652103666096L;
    
    private static final String UNMATCH_MESSAGE = "SQL syntax error, expected token is '%s', actual token is '%s', literals is '%s'.";
    
    private static final String TOKEN_ERROR_MESSAGE = "SQL syntax error, token is '%s', literals is '%s'.";
    
    public SQLParsingException(final String message, final Object... args) {
        super(message, args);
    }
    
    public SQLParsingException(final Lexer lexer, final TokenType expectedTokenType) {
        super(String.format(UNMATCH_MESSAGE, expectedTokenType, lexer.getCurrentToken().getType(), lexer.getCurrentToken().getLiterals()));
    }
    
    public SQLParsingException(final LexerEngine lexerEngine) {
        super(String.format(TOKEN_ERROR_MESSAGE, lexerEngine.getCurrentToken().getType(), lexerEngine.getCurrentToken().getLiterals()));
    }
}
