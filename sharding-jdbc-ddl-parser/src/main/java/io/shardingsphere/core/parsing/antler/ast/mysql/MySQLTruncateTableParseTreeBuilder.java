package io.shardingsphere.core.parsing.antler.ast.mysql;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.mysql.MySQLAdvancedTruncateTableParser;
import io.shardingsphere.parser.antlr.mysql.MySQLTruncateTableLexer;

public class MySQLTruncateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new MySQLTruncateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new MySQLAdvancedTruncateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        MySQLAdvancedTruncateTableParser truncateTableParser = (MySQLAdvancedTruncateTableParser) parser;
        return truncateTableParser.truncateTable();
    }

}
