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

package io.shardingsphere.core.parsing.antlr.parser.dialect;

import io.shardingsphere.core.parsing.antlr.AdvancedErrorStrategy;
import io.shardingsphere.core.parsing.antlr.AdvancedParserATNSimulator;
import io.shardingsphere.core.parsing.antlr.autogen.MySQLStatementParser;
import io.shardingsphere.core.parsing.antlr.util.AntlrUtils;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

/**
 * MySQL statement parser.
 * 
 * @author duhongjun
 */
public final class MySQLStatementAdvancedParser extends MySQLStatementParser {
    
    public MySQLStatementAdvancedParser(final TokenStream input) {
        super(input);
        _interp = new AdvancedParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache, ID);
        this._errHandler = new AdvancedErrorStrategy(ID);
    }
    
    @Override
    public Token match(final int tokenType) throws RecognitionException {
        if (Token.EOF == tokenType) {
            matchedEOF = true;
        }
        return AntlrUtils.getMatchedToken(this, tokenType, ID);
    }
}
