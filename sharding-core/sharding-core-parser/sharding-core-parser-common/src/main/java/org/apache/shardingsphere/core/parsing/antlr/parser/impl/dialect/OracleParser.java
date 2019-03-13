/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.core.parsing.antlr.autogen.OracleStatementParser;
import org.apache.shardingsphere.core.parsing.antlr.parser.advanced.AdvancedErrorStrategy;
import org.apache.shardingsphere.core.parsing.antlr.parser.advanced.AdvancedMatchHandler;
import org.apache.shardingsphere.core.parsing.antlr.parser.advanced.AdvancedParserATNSimulator;
import org.apache.shardingsphere.core.parsing.api.SQLParser;

/**
 * SQL parser for Oracle.
 * 
 * @author duhongjun
 */
public final class OracleParser extends OracleStatementParser implements SQLParser {
    
    private final AdvancedMatchHandler advancedMatchHandler;
    
    public OracleParser(final TokenStream input) {
        super(input);
        _interp = new AdvancedParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache, ID);
        _errHandler = new AdvancedErrorStrategy(ID);
        advancedMatchHandler = new AdvancedMatchHandler(this, ID);
    }
    
    @Override
    public Token match(final int tokenType) throws RecognitionException {
        if (Token.EOF == tokenType) {
            matchedEOF = true;
        }
        return advancedMatchHandler.getMatchedToken(tokenType);
    }
}
