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

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.misc.IntervalSet;

public class AdvancedErrorStrategy extends DefaultErrorStrategy {
    private int id;

    public AdvancedErrorStrategy(final int id) {
        super();
        this.id = id;
    }

    /** The default implementation of {@link ANTLRErrorStrategy#sync} makes sure
     * that the current lookahead symbol is consistent with what were expecting
     * if failed to match keyword,use ID try again.
     * @param recognizer the parser instance
     * @throws RecognitionException the recognition exception
     */
    @Override
    public void sync(final Parser recognizer) throws RecognitionException {
        if (inErrorRecoveryMode(recognizer)) {
            return;
        }

        try {
            TokenStream tokens = recognizer.getInputStream();
            Token token = tokens.LT(1);

            ATNState s = recognizer.getInterpreter().atn.states.get(recognizer.getState());
            IntervalSet nextTokens = recognizer.getATN().nextTokens(s);
            if (nextTokens.contains(token.getType())) {
                nextTokensContext = null;
                nextTokensState = ATNState.INVALID_STATE_NUMBER;
                return;
            }

            if (nextTokens.contains(Token.EPSILON)) {
                if (nextTokensContext == null) {
                    // It's possible the next token won't match; information tracked
                    // by sync is restricted for performance.
                    nextTokensContext = recognizer.getContext();
                    nextTokensState = recognizer.getState();
                }
                return;
            }

            if (nextTokens.contains(id)) {
                CommonToken commonToken = castCommonToken(token);
                commonToken.setType(id);
            }
            super.sync(recognizer);
        } catch (InputMismatchException e) {
            tryExecByID(recognizer, e);
        }
    }

    private void tryExecByID(final Parser recognizer, final InputMismatchException e) {
        Token token = e.getOffendingToken();
        CommonToken commonToken = castCommonToken(token);
        if (null == commonToken) {
            throw e;
        }

        int previousType = commonToken.getType();
        if (previousType > id) {
            return;
        }

        commonToken.setType(id);
        try {
            super.sync(recognizer);
        } catch (InputMismatchException ex) {
            if (e.getOffendingToken() == ex.getOffendingToken()) {
                commonToken.setType(previousType);
                throw e;
            }
            tryExecByID(recognizer, ex);
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
