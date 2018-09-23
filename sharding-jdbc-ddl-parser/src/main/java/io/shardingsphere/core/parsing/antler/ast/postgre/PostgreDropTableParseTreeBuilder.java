package io.shardingsphere.core.parsing.antler.ast.postgre;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.postgre.PostgreAdvancedDropTableParser;
import io.shardingsphere.parser.antlr.postgre.PostgreDropTableLexer;

public class PostgreDropTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new PostgreDropTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new PostgreAdvancedDropTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        PostgreAdvancedDropTableParser dropTableParser = (PostgreAdvancedDropTableParser) parser;
        return dropTableParser.dropTable();
    }

}
