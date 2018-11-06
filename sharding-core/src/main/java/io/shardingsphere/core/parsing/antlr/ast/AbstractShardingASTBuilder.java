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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

/**
 * Abstract class for sharding AST builder.
 * 
 * @author duhongjun
 */
public abstract class AbstractShardingASTBuilder implements ShardingASTBuilder {
    
    @Override
    public final ParserRuleContext parse(final String sql) {
        Lexer lexer = newLexer(CharStreams.fromString(sql));
        Parser parser = newParser(new CommonTokenStream(lexer));
        ParserRuleContext rootContext = parse(parser);
        return null == rootContext ? null : (ParserRuleContext) rootContext.getChild(0);
    }
    
    protected abstract ParserRuleContext parse(Parser parser);
    
    protected abstract Lexer newLexer(CharStream charStream);
    
    protected abstract Parser newParser(TokenStream tokenStream);
}
