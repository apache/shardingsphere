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

package io.shardingsphere.core.parsing.antler.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

/**
 * Antlr utils.
 *
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AntlrUtils {

    /**
     * Match token by token type.
     *
     * @param parser antlr parser
     * @param tokenType token type
     * @param id id token index num
     * @return Token
     * @throws RecognitionException mismatch throw exception
     */
    public static Token match(final Parser parser, final int tokenType, final int id) throws RecognitionException {
        Token token = parser.getCurrentToken();
        boolean compatID = false;
        if (id == tokenType && id > token.getType()) {
            compatID = true;
        }
        if (token.getType() == tokenType || compatID) {
            if (Token.EOF != tokenType && compatID && (token instanceof CommonToken)) {
                CommonToken commonToken = (CommonToken) token;
                commonToken.setType(id);
            }
            parser.getErrorHandler().reportMatch(parser);
            parser.consume();
        } else {
            token = parser.getErrorHandler().recoverInline(parser);
            if (parser.getBuildParseTree() && token.getTokenIndex() == -1) {
                // we must have conjured up a new token during single token insertion
                // if it's not the current symbol
                parser.getContext().addErrorNode(parser.createErrorNode(parser.getContext(), token));
            }
        }
        return token;
    }
    
    /**
     * Cast Token to CommonToken.
     *
     * @param token lexical token
     * @return token is CommonToken, return CommonToken else return null
     */
    public static CommonToken castCommonToken(final Token token) {
        return token instanceof CommonToken ? (CommonToken) token : null;
    }
}
