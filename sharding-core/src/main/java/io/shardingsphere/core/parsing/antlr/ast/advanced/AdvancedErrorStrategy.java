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
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Advanced error strategy.
 * 
 * <p>Override sync method,when failed matching, try again with ID.</p>
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class AdvancedErrorStrategy extends DefaultErrorStrategy {
    
    private final int identifierTokenIndex;
    
    @Override
    public void sync(final Parser recognizer) throws RecognitionException {
        if (inErrorRecoveryMode(recognizer)) {
            return;
        }
        try {
            TokenStream tokens = recognizer.getInputStream();
            Token token = tokens.LT(1);
            ATNState state = recognizer.getInterpreter().atn.states.get(recognizer.getState());
            IntervalSet nextTokens = recognizer.getATN().nextTokens(state);
            if (nextTokens.contains(token.getType())) {
                nextTokensContext = null;
                nextTokensState = ATNState.INVALID_STATE_NUMBER;
                return;
            }
            if (nextTokens.contains(Token.EPSILON)) {
                if (null == nextTokensContext) {
                    nextTokensContext = recognizer.getContext();
                    nextTokensState = recognizer.getState();
                }
                return;
            }
            if (nextTokens.contains(identifierTokenIndex)) {
                ((CommonToken) token).setType(identifierTokenIndex);
            }
            super.sync(recognizer);
        } catch (InputMismatchException ex) {
            tryToExecuteByID(recognizer, ex);
        }
    }
    
    private void tryToExecuteByID(final Parser recognizer, final InputMismatchException cause) {
        Token token = cause.getOffendingToken();
        CommonToken commonToken;
        if (token instanceof CommonToken) {
            commonToken = (CommonToken) token;
        } else {
            throw cause;
        }
        int previousType = commonToken.getType();
        if (previousType > identifierTokenIndex) {
            return;
        }
        commonToken.setType(identifierTokenIndex);
        try {
            super.sync(recognizer);
        } catch (InputMismatchException ex) {
            if (cause.getOffendingToken() == ex.getOffendingToken()) {
                commonToken.setType(previousType);
                throw cause;
            }
            tryToExecuteByID(recognizer, ex);
        } catch (final Exception ex) {
            commonToken.setType(previousType);
            throw cause;
        }
    }
}
