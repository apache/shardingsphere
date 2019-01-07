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

package io.shardingsphere.core.parsing.parser.exception;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.parsing.lexer.token.TokenType;

/**
 * Throw exception when SQL not supported.
 * 
 * @author zhangliang 
 */
public final class SQLParsingUnsupportedException extends ShardingException {
    
    private static final long serialVersionUID = -4968036951399076811L;
    
    private static final String MESSAGE = "Not supported token '%s'.";
    
    public SQLParsingUnsupportedException(final TokenType tokenType) {
        super(String.format(MESSAGE, tokenType.toString()));
    }

    public SQLParsingUnsupportedException(final String message) {
        super(message);
    }
}
