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

package io.shardingsphere.core.parsing.antler.parser;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.AdvancedErrorStrategy;
import io.shardingsphere.core.parsing.antler.AdvancedParserATNSimulator;
import io.shardingsphere.parser.antlr.OracleStatementParser;

public class OracleStatementAdvancedParser extends OracleStatementParser {

    public OracleStatementAdvancedParser(final TokenStream input) {
        super(input);
        _interp = new AdvancedParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache, ID);
        this._errHandler = new AdvancedErrorStrategy(ID);
    }

    /**
     * Match token by token type.
     *
     * @param ttype token type
     * @return current matched token
     * @throws RecognitionException mismatch throw exception
     */
    public Token match(final int ttype) throws RecognitionException {
        Token t = getCurrentToken();

        boolean compatID = false;
        if (ID == ttype && ID > t.getType()) {
            compatID = true;
        }

        if (t.getType() == ttype || compatID) {
            if (ttype == Token.EOF) {
                matchedEOF = true;
            } else if (compatID && (t instanceof CommonToken)) {
                CommonToken commonToken = (CommonToken) t;
                commonToken.setType(ID);
            }
            _errHandler.reportMatch(this);
            consume();
        } else {
            t = _errHandler.recoverInline(this);
            if (_buildParseTrees && t.getTokenIndex() == -1) {
                // we must have conjured up a new token during single token insertion
                // if it's not the current symbol
                _ctx.addErrorNode(createErrorNode(_ctx, t));
            }
        }
        return t;
    }
}
