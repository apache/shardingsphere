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

package io.shardingsphere.core.parsing.antlr.ast;

import io.shardingsphere.core.exception.ShardingException;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

/**
 * Sharding AST builder.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
public class ShardingASTBuilder {
    
    private final Class<? extends Lexer> lexerClass;
    
    private final Class<? extends SQLStatementParser> parserClass;
    
    /** 
     * Parse SQL to AST.
     * 
     * @param sql SQL
     * @return parsed AST
     */
    public ParserRuleContext parse(final String sql) {
        Lexer lexer = newLexer(CharStreams.fromString(sql));
        SQLStatementParser parser = newParser(new CommonTokenStream(lexer));
        ParserRuleContext rootContext = parser.execute();
        return null == rootContext ? null : (ParserRuleContext) rootContext.getChild(0);
    }
    
    private Lexer newLexer(final CharStream charStream) {
        try {
            return lexerClass.getConstructor(CharStream.class).newInstance(charStream);
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private SQLStatementParser newParser(final TokenStream tokenStream) {
        try {
            return parserClass.getConstructor(TokenStream.class).newInstance(tokenStream);
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException(ex);
        }
    }
}
