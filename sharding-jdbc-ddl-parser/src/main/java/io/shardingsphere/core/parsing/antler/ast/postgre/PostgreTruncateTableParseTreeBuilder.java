package io.shardingsphere.core.parsing.antler.ast.postgre;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.postgre.PostgreAdvancedTruncateTableParser;
import io.shardingsphere.parser.antlr.postgre.PostgreTruncateTableLexer;

public class PostgreTruncateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new PostgreTruncateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new PostgreAdvancedTruncateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        PostgreAdvancedTruncateTableParser truncateTableParser = (PostgreAdvancedTruncateTableParser) parser;
        return truncateTableParser.truncateTable();
    }

}
