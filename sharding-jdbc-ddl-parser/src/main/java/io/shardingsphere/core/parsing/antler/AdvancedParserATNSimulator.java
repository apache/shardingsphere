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
