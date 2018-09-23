package io.shardingsphere.core.parsing.antler.ast.oracle;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import io.shardingsphere.core.parsing.antler.ast.AbstractParseTreeBuilder;
import io.shardingsphere.core.parsing.antler.parser.oracle.OracleAdvancedTruncateTableParser;
import io.shardingsphere.parser.antlr.oracle.OracleTruncateTableLexer;

public class OracleTruncateTableParseTreeBuilder extends AbstractParseTreeBuilder {

    @Override
    protected Lexer newLexer(final CharStream stream) {
        return new OracleTruncateTableLexer(stream);
    }

    @Override
    protected Parser newParser(final TokenStream tokenStream) {
        return new OracleAdvancedTruncateTableParser(tokenStream);
    }

    @Override
    protected ParserRuleContext getParserTree(final Parser parser) {
        OracleAdvancedTruncateTableParser truncateTableParser = (OracleAdvancedTruncateTableParser) parser;
        return truncateTableParser.truncateTable();
    }

}
