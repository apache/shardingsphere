package io.shardingsphere.core.parsing.antler.ast.sqlserver;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.sqlserver.SQLServerAdvancedTruncateTableParser;
import io.shardingsphere.parser.antlr.sqlserver.SQLServerTruncateTableLexer;

public class SQLServerTruncateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new SQLServerTruncateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new SQLServerAdvancedTruncateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        SQLServerAdvancedTruncateTableParser truncateTableParser = (SQLServerAdvancedTruncateTableParser) parser;
        return truncateTableParser.truncateTable();
    }

}
