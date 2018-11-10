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

package io.shardingsphere.core.parsing.antlr.ast.advanced;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

/**
 * Advanced match handler.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class AdvancedMatchHandler {
    
    private final Parser parser;
    
    private final int identifierTokenIndex;
    
    /**
     * Get matched token by token type.
     *
     * @param tokenType token type
     * @return matched token
     * @throws RecognitionException mismatch throw exception
     */
    public Token getMatchedToken(final int tokenType) throws RecognitionException {
        Token result = parser.getCurrentToken();
        boolean isIdentifierCompatible = false;
        if (identifierTokenIndex == tokenType && identifierTokenIndex > result.getType()) {
            isIdentifierCompatible = true;
        }
        if (result.getType() == tokenType || isIdentifierCompatible) {
            if (Token.EOF != tokenType && isIdentifierCompatible && result instanceof CommonToken) {
                ((CommonToken) result).setType(identifierTokenIndex);
            }
            parser.getErrorHandler().reportMatch(parser);
            parser.consume();
        } else {
            result = parser.getErrorHandler().recoverInline(parser);
            if (parser.getBuildParseTree() && -1 == result.getTokenIndex()) {
                parser.getContext().addErrorNode(parser.createErrorNode(parser.getContext(), result));
            }
        }
        return result;
    }
}
