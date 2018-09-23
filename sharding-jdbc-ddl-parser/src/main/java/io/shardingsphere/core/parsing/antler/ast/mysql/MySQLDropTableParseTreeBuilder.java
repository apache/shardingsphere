package io.shardingsphere.core.parsing.antler.ast.mysql;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.mysql.MySQLAdvancedDropTableParser;
import io.shardingsphere.parser.antlr.mysql.MySQLDropTableLexer;

public class MySQLDropTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new MySQLDropTableLexer(stream);
    }

    protected Parser newParser(final TokenStream tokenStream) {
        return new MySQLAdvancedDropTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        MySQLAdvancedDropTableParser dropTableParser = (MySQLAdvancedDropTableParser) parser;
        return dropTableParser.dropTable();
    }

}
