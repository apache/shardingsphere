package io.shardingsphere.core.parsing.antler.ast.postgre;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.postgre.PostgreAdvancedAlterTableParser;
import io.shardingsphere.parser.antlr.postgre.PostgreAlterTableLexer;

public class PostgreAlterTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new PostgreAlterTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new PostgreAdvancedAlterTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        PostgreAdvancedAlterTableParser alterTableParser = (PostgreAdvancedAlterTableParser) parser;
        return alterTableParser.alterTable();
    }

}
