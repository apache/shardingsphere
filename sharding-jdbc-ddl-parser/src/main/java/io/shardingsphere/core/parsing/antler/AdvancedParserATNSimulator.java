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

package io.shardingsphere.core.parsing.antler;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.dfa.DFAState;

public class AdvancedParserATNSimulator extends ParserATNSimulator {
    private int id;

    public AdvancedParserATNSimulator(final Parser parser, final ATN atn, final DFA[] decisionToDFA,
            final PredictionContextCache sharedContextCache, final int id) {
        super(parser, atn, decisionToDFA, sharedContextCache);
        this.id = id;
    }

    /**
     * Performs ATN simulation to compute a predicted alternative based
     *  upon the remaining input, but also updates the DFA cache to avoid
     *  having to traverse the ATN again for the same input sequence.
     * @param dfa antlr dfa instance
     * @param s0 start state
     * @param input input token stream
     * @param startIndex  start index
     * @param outerContext outer context
     * @return alternative path number
     */
    protected int execATN(final DFA dfa, final DFAState s0, final TokenStream input, final int startIndex, final ParserRuleContext outerContext) {
        try {
            return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (NoViableAltException e) {
            return tryExecByID(dfa, s0, input, startIndex, outerContext, e);
        }
    }

    private int tryExecByID(final DFA dfa, final DFAState s0, final TokenStream input, final int startIndex, final ParserRuleContext outerContext,
            final NoViableAltException e) {
        Token token = e.getOffendingToken();
        CommonToken commonToken = castCommonToken(token);
        if (null == commonToken) {
            throw e;
        }

        int previousType = commonToken.getType();
        if (previousType > id || Token.EOF == token.getType()) {
            throw e;
        }

        commonToken.setType(id);
        try {
            return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (NoViableAltException ex) {
            if (e.getOffendingToken() == ex.getOffendingToken()) {
                throw e;
            }

            return tryExecByID(dfa, s0, input, startIndex, outerContext, ex);
        } catch (Exception ex) {
            commonToken.setType(previousType);
            throw e;
        }

    }

    private CommonToken castCommonToken(final Token token) {
        if (token instanceof CommonToken) {
            return (CommonToken) token;
        }
        return null;
    }

}
