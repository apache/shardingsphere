package io.shardingsphere.core.parsing.antler.ast.mysql;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.mysql.MySQLAdvancedCreateTableParser;
import io.shardingsphere.parser.antlr.mysql.MySQLCreateTableLexer;

public class MySQLCreateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new MySQLCreateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new MySQLAdvancedCreateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        MySQLAdvancedCreateTableParser createTableParser = (MySQLAdvancedCreateTableParser) parser;
        return createTableParser.createTable();
    }

}
