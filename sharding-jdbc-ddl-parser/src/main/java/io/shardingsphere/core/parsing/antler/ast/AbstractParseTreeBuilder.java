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

package io.shardingsphere.core.parsing.antler.ast;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

public abstract class AbstractParseTreeBuilder implements ParseTreeBuilder {

    @Override
    public ParserRuleContext parse(final String input) {
        CharStream stream = CharStreams.fromString(input);
        Lexer lexer = newLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        Parser parser = newParser(tokenStream);
        return getParserTree(parser);
    }

    protected abstract Lexer newLexer(final CharStream stream);

    protected abstract Parser newParser(final TokenStream tokenStream);

    protected abstract ParserRuleContext getParserTree(final Parser parser);

}
