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

package io.shardingsphere.core.parsing.antlr.parser.advanced;

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

/**
 * Advanced Parser ATN simulator.
 * 
 * <p>Failed calculating alt, try again with ID.</p>
 * 
 * @author duhongjun
 */
public final class AdvancedParserATNSimulator extends ParserATNSimulator {
    
    private int identifierTokenIndex;
    
    public AdvancedParserATNSimulator(final Parser parser, final ATN atn, final DFA[] decisionToDFA, final PredictionContextCache sharedContextCache, final int identifierTokenIndex) {
        super(parser, atn, decisionToDFA, sharedContextCache);
        this.identifierTokenIndex = identifierTokenIndex;
    }
    
    @Override
    protected int execATN(final DFA dfa, final DFAState s0, final TokenStream input, final int startIndex, final ParserRuleContext outerContext) {
        try {
            return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (final NoViableAltException ex) {
            return tryToExecuteByID(dfa, s0, input, startIndex, outerContext, ex);
        }
    }
    
    private int tryToExecuteByID(final DFA dfa, final DFAState s0, final TokenStream input, final int startIndex, final ParserRuleContext outerContext, final NoViableAltException cause) {
        Token token = cause.getOffendingToken();
        CommonToken commonToken;
        if (token instanceof CommonToken) {
            commonToken = (CommonToken) token;
        } else {
            throw cause;
        }
        int previousType = commonToken.getType();
        if (previousType > identifierTokenIndex || Token.EOF == token.getType()) {
            throw cause;
        }
        commonToken.setType(identifierTokenIndex);
        try {
            return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (NoViableAltException ex) {
            if (cause.getOffendingToken() == ex.getOffendingToken()) {
                throw cause;
            }
            return tryToExecuteByID(dfa, s0, input, startIndex, outerContext, ex);
        } catch (final Exception ex) {
            commonToken.setType(previousType);
            throw cause;
        }
    }
}
