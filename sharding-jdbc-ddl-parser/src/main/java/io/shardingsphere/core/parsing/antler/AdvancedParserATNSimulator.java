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
    
    public AdvancedParserATNSimulator(Parser parser, ATN atn, DFA[] decisionToDFA,
            PredictionContextCache sharedContextCache, int id) {
        super(parser, atn, decisionToDFA, sharedContextCache);
        this.id = id;
    }
    
    protected int execATN(DFA dfa, DFAState s0, TokenStream input, int startIndex, ParserRuleContext outerContext) {
        try {
            return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (NoViableAltException e) {
            return tryExecByID(dfa, s0, input, startIndex, outerContext, e);
        }
    }
    
    private int tryExecByID(DFA dfa, DFAState s0, TokenStream input, int startIndex, ParserRuleContext outerContext, NoViableAltException e) {
        Token token = e.getOffendingToken();
        CommonToken commonToken = castCommonToken(token);
        if(null == commonToken) {
            throw e;
        }
        
        int previousType = commonToken.getType();
        if(previousType > id || Token.EOF == token.getType()) {
            throw e;
        }
        
        commonToken.setType(id);
        try {
           return super.execATN(dfa, s0, input, startIndex, outerContext);
        } catch (NoViableAltException e1) {
            if(e.getOffendingToken() == e1.getOffendingToken()) {
                throw e;
            }
            
            return tryExecByID(dfa,  s0, input, startIndex,  outerContext, e1);
        } catch (Exception e1) {
            commonToken.setType(previousType);
            throw e;
        }
        
    }
    
    private CommonToken castCommonToken(Token token) {
        if(token instanceof CommonToken) {
            return (CommonToken)token;
        }
        return null;
    }

}
