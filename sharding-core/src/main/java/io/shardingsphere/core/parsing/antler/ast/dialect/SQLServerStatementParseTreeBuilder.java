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

package io.shardingsphere.core.parsing.antler.ast.dialect;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.dialect.SQLServerStatementAdvancedParser;
import io.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

/**
 * SQLServer tree builder.
 * 
 * @author duhongjun
 */
public final class SQLServerStatementParseTreeBuilder extends AbstractParseTreeBuilder {
    
    @Override
    protected Lexer newLexer(final CharStream charStream) {
        return new SQLServerStatementLexer(charStream);
    }
    
    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new SQLServerStatementAdvancedParser(tokenStream);
    }
    
    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        SQLServerStatementAdvancedParser parse = (SQLServerStatementAdvancedParser) parser;
        return parse.execute();
    }
}
