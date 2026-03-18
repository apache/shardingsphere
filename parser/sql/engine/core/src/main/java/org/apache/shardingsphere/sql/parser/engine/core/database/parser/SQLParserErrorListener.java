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

package org.apache.shardingsphere.sql.parser.engine.core.database.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;

/**
 * SQL parser error listener.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserErrorListener extends BaseErrorListener {
    
    private static final SQLParserErrorListener INSTANCE = new SQLParserErrorListener();
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static SQLParserErrorListener getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine,
                            final String message, final RecognitionException e) {
        throw new SQLParsingException(message, offendingSymbol, line);
    }
}
