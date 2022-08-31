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

package org.apache.shardingsphere.sql.parser.core.database.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * sql parser error listener.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParserErrorListener extends BaseErrorListener {
    
    private static final ParserErrorListener INSTANCE = new ParserErrorListener();
    
    /**
     * get ParserErrorListener instance.
     * @return ParserErrorListener instance
     */
    public static ParserErrorListener getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine,
                            final String msg, final RecognitionException e) throws ParseCancellationException {
        StringBuilder sb = new StringBuilder("syntax error at line ");
        sb.append(line).append(", position ").append(charPositionInLine).append(", near ").append(offendingSymbol).append(", ").append(msg);
        throw new ParseCancellationException(sb.toString());
    }
    
}
