package io.shardingsphere.core.parsing.antler.ast.sqlserver;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.sqlserver.SQLServerAdvancedCreateTableParser;
import io.shardingsphere.parser.antlr.sqlserver.SQLServerCreateTableLexer;

public class SQLServerCreateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new SQLServerCreateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new SQLServerAdvancedCreateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        SQLServerAdvancedCreateTableParser createTableParser = (SQLServerAdvancedCreateTableParser) parser;
        return createTableParser.createTable();
    }

}
